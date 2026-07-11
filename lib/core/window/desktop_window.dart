import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:launch_at_startup/launch_at_startup.dart';
import 'package:screen_retriever/screen_retriever.dart';
import 'package:tray_manager/tray_manager.dart';
import 'package:window_manager/window_manager.dart';

import '../data/notification_l10n.dart';
import '../../l10n/locale_notifier.dart';

const Size mobileWindowSize = Size(390, 844);
const double _screenEdgeMargin = 24;
const Duration _popDuration = Duration(milliseconds: 220);
const Curve _popInCurve = Curves.easeOutBack;
const Curve _popOutCurve = Curves.easeIn;

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// True while a native OS dialog is open, so onWindowBlur doesn't close the flyout.
final isPickingFileNotifier = ValueNotifier<bool>(false);

Future<void> initDesktopWindow() async {
  if (!isDesktopPlatform) return;

  launchAtStartup.setup(appName: 'DayPilot', appPath: Platform.resolvedExecutable);

  await windowManager.ensureInitialized();
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
      await windowManager.setAsFrameless();
      await windowManager.setResizable(false);
      await windowManager.setAlwaysOnTop(true);
    },
  );

  await trayManager.setIcon(
    Platform.isWindows
        ? 'assets/images/tray_icon.ico'
        : 'assets/images/tray_icon.png',
  );

  await _setTrayMenu();
  dayPilotLocaleNotifier.addListener(_setTrayMenu);
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

/// Linux never reports the tray icon's position, so this opens bottom-right.
Future<Offset> _cornerPosition() async {
  final display = await screenRetriever.getPrimaryDisplay();
  final areaOrigin = display.visiblePosition ?? Offset.zero;
  final areaSize = display.visibleSize ?? display.size;
  return Offset(
    areaOrigin.dx + areaSize.width - mobileWindowSize.width - _screenEdgeMargin,
    areaOrigin.dy + areaSize.height - mobileWindowSize.height - _screenEdgeMargin,
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
    await windowManager.setPosition(await _cornerPosition());
    await windowManager.show();
    await windowManager.focus();
    if (Platform.isWindows) {
      // The win32 Flutter embedder resizes its child view off a real WM_SIZE
      // message, which setting the same size again doesn't reliably send.
      // Nudging the size by a pixel and back forces a genuine one, so the
      // child view actually gets resized to match the window.
      await windowManager.setSize(
        Size(mobileWindowSize.width + 1, mobileWindowSize.height),
      );
      await windowManager.setSize(mobileWindowSize);
    }
    setState(() => _contentVisible = true);
  }

  Future<void> _close() async {
    setState(() => _contentVisible = false);
    await Future.delayed(_popDuration);
    await windowManager.hide();
  }

  Future<void> _toggle() async {
    if (await windowManager.isVisible()) {
      await _close();
    } else {
      await _open();
    }
  }

  @override
  void onWindowBlur() {
    if (isPickingFileNotifier.value) return;
    if (_contentVisible) _close();
  }

  @override
  void onTrayIconMouseDown() {
    _toggle();
  }

  @override
  void onTrayIconRightMouseDown() {
    trayManager.popUpContextMenu();
  }

  @override
  void onTrayMenuItemClick(MenuItem menuItem) {
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
