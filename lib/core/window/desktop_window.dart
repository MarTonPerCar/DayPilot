import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:launch_at_startup/launch_at_startup.dart';
import 'package:tray_manager/tray_manager.dart';
import 'package:window_manager/window_manager.dart';

import '../data/notification_l10n.dart';
import '../logging/app_logger.dart';
import '../../l10n/locale_notifier.dart';

const Size mobileWindowSize = Size(390, 844);
const Duration _popDuration = Duration(milliseconds: 220);
const Curve _popInCurve = Curves.easeOutBack;
const Curve _popOutCurve = Curves.easeIn;

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// True while a native OS dialog is open, so onWindowBlur doesn't close the flyout.
final isPickingFileNotifier = ValueNotifier<bool>(false);

Future<void> _logWindowState(String label) async {
  try {
    final size = await windowManager.getSize();
    final position = await windowManager.getPosition();
    final visible = await windowManager.isVisible();
    AppLogger.log(
      '[$label] size=${size.width}x${size.height} '
      'position=(${position.dx}, ${position.dy}) visible=$visible',
    );
  } catch (e, st) {
    AppLogger.logError('_logWindowState($label)', e, st);
  }
}

Future<void> initDesktopWindow() async {
  if (!isDesktopPlatform) {
    AppLogger.log('initDesktopWindow: not a desktop platform, skipping');
    return;
  }

  launchAtStartup.setup(appName: 'DayPilot', appPath: Platform.resolvedExecutable);
  AppLogger.log('launchAtStartup configured');

  await windowManager.ensureInitialized();
  AppLogger.log('windowManager.ensureInitialized() done');
  await _logWindowState('right after ensureInitialized');

  // The native Windows runner shows its window immediately on creation
  // (standard win32_window.cpp behavior) — independent of window_manager's
  // waitUntilReadyToShow, which assumes the window starts hidden. Force it
  // hidden explicitly so the raw/unpositioned window never flashes visible
  // before we've had a chance to size, position, and style it.
  await windowManager.hide();
  AppLogger.log('windowManager.hide() called explicitly');
  await _logWindowState('after explicit hide()');

  await windowManager.waitUntilReadyToShow(
    WindowOptions(
      size: mobileWindowSize,
      minimumSize: mobileWindowSize,
      maximumSize: mobileWindowSize,
      skipTaskbar: true,
      titleBarStyle: TitleBarStyle.hidden,
      // True per-pixel transparency isn't reliably compositied on Windows —
      // it can leave stale/garbled content from whatever's behind the
      // window instead of blending properly. Linux/macOS handle it fine.
      backgroundColor: Platform.isWindows ? const Color(0xFF4A7C59) : const Color(0x00000000),
      title: 'DayPilot',
    ),
    () async {
      AppLogger.log('waitUntilReadyToShow callback entered');
      await _logWindowState('after waitUntilReadyToShow, before frame changes');

      if (!Platform.isWindows) {
        await windowManager.setAsFrameless();
        AppLogger.log('setAsFrameless() called');
      } else {
        AppLogger.log('setAsFrameless() skipped on Windows');
      }
      await windowManager.setResizable(false);
      await windowManager.setAlwaysOnTop(true);
      await _logWindowState('after setResizable/setAlwaysOnTop');
    },
  );
  await _logWindowState('after waitUntilReadyToShow returned');

  await trayManager.setIcon(
    Platform.isWindows
        ? 'assets/images/tray_icon.ico'
        : 'assets/images/tray_icon.png',
  );
  AppLogger.log('Tray icon set');

  await _setTrayMenu();
  dayPilotLocaleNotifier.addListener(_setTrayMenu);
  AppLogger.log('initDesktopWindow complete');
}

Future<void> _setTrayMenu() async {
  final l10n = currentL10n();
  await trayManager.setContextMenu(
    Menu(
      items: [
        MenuItem(key: 'open_app', label: l10n.trayOpen),
        MenuItem.separator(),
        MenuItem(key: 'exit_app', label: l10n.trayExit),
      ],
    ),
  );
}

class DesktopFlyoutScope extends StatefulWidget {
  const DesktopFlyoutScope({super.key, required this.child});

  final Widget child;

  @override
  State<DesktopFlyoutScope> createState() => _DesktopFlyoutScopeState();
}

class _DesktopFlyoutScopeState extends State<DesktopFlyoutScope>
    with WindowListener, TrayListener {
  bool _contentVisible = false;

  @override
  void initState() {
    super.initState();
    if (isDesktopPlatform) {
      windowManager.addListener(this);
      trayManager.addListener(this);
    }
  }

  @override
  void dispose() {
    if (isDesktopPlatform) {
      windowManager.removeListener(this);
      trayManager.removeListener(this);
    }
    super.dispose();
  }

  Future<void> _open() async {
    AppLogger.log('_open() called');
    await _logWindowState('_open: before setAlignment');
    // window_manager's own alignment helper (uses the native monitor work
    // area directly) instead of our own screenRetriever-based calculation,
    // in case that calculation was ever wrong on some monitor setups.
    await windowManager.setAlignment(Alignment.bottomRight);
    await _logWindowState('_open: after setAlignment');
    await windowManager.show();
    await windowManager.focus();
    await _logWindowState('_open: after show+focus');
    setState(() => _contentVisible = true);
  }

  Future<void> _close() async {
    AppLogger.log('_close() called');
    setState(() => _contentVisible = false);
    await Future.delayed(_popDuration);
    await windowManager.hide();
  }

  Future<void> _toggle() async {
    final visible = await windowManager.isVisible();
    AppLogger.log('_toggle() called, currently visible=$visible');
    if (visible) {
      await _close();
    } else {
      await _open();
    }
  }

  @override
  void onWindowBlur() {
    AppLogger.log('onWindowBlur, isPickingFile=${isPickingFileNotifier.value}, contentVisible=$_contentVisible');
    if (isPickingFileNotifier.value) return;
    if (_contentVisible) _close();
  }

  @override
  void onTrayIconMouseDown() {
    AppLogger.log('onTrayIconMouseDown');
    _toggle();
  }

  @override
  void onTrayIconRightMouseDown() {
    trayManager.popUpContextMenu();
  }

  @override
  void onTrayMenuItemClick(MenuItem menuItem) {
    AppLogger.log('onTrayMenuItemClick: ${menuItem.key}');
    switch (menuItem.key) {
      case 'open_app':
        _toggle();
      case 'exit_app':
        windowManager.close();
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!isDesktopPlatform) return widget.child;
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0, end: _contentVisible ? 1.0 : 0.0),
      duration: _popDuration,
      curve: _contentVisible ? _popInCurve : _popOutCurve,
      builder: (context, t, child) {
        return Opacity(
          opacity: t.clamp(0.0, 1.0),
          child: Transform.scale(
            scale: 0.92 + (t * 0.08),
            alignment: Alignment.bottomRight,
            child: child,
          ),
        );
      },
      child: widget.child,
    );
  }
}
