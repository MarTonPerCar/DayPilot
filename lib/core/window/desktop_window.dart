import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
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

/// Set to true while a native OS dialog (file picker, etc.) is open, so the
/// flyout's onWindowBlur doesn't mistake the resulting focus loss for the
/// user dismissing the flyout.
final isPickingFileNotifier = ValueNotifier<bool>(false);

Future<void> initDesktopWindow() async {
  if (!isDesktopPlatform) return;

  await windowManager.ensureInitialized();
  await windowManager.waitUntilReadyToShow(
    const WindowOptions(
      size: mobileWindowSize,
      minimumSize: mobileWindowSize,
      maximumSize: mobileWindowSize,
      skipTaskbar: true,
      titleBarStyle: TitleBarStyle.hidden,
      backgroundColor: Color(0x00000000), // transparent, for the pop-in fade
      title: 'DayPilot',
    ),
    () async {
      await windowManager.setAsFrameless();
      await windowManager.setResizable(false);
      await windowManager.setAlwaysOnTop(true);
      // stays hidden — only the tray icon opens it
    },
  );

  await trayManager.setIcon(
    Platform.isWindows
        ? 'assets/images/tray_icon.ico'
        : 'assets/images/tray_icon.png',
  );

  // Locale isn't restored from prefs until after this runs (see main.dart),
  // and can change later from Settings — rebuild the menu on every change so
  // it's never stuck showing the startup-default locale.
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

/// Linux's tray backend never reports the icon's position, so this always
/// opens bottom-right instead of anchoring to the click point.
Future<Offset> _cornerPosition() async {
  final display = await screenRetriever.getPrimaryDisplay();
  final areaOrigin = display.visiblePosition ?? Offset.zero;
  final areaSize = display.visibleSize ?? display.size;
  return Offset(
    areaOrigin.dx + areaSize.width - mobileWindowSize.width - _screenEdgeMargin,
    areaOrigin.dy + areaSize.height - mobileWindowSize.height - _screenEdgeMargin,
  );
}

/// Pops in/out via scale+fade rather than moving the window — window
/// managers (especially Linux) don't animate position reliably.
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
