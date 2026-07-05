import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:screen_retriever/screen_retriever.dart';
import 'package:tray_manager/tray_manager.dart';
import 'package:window_manager/window_manager.dart';

/// Fixed size of the flyout window, matching a common phone screen so the
/// mobile UI can be used as-is on desktop.
const Size mobileWindowSize = Size(390, 844);

/// Gap kept between the flyout and the screen edges when anchoring it to a
/// corner.
const double _screenEdgeMargin = 24;

/// Duration/curve of the pop-in shown when the flyout opens or closes.
const Duration _popDuration = Duration(milliseconds: 220);
const Curve _popInCurve = Curves.easeOutBack;
const Curve _popOutCurve = Curves.easeIn;

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// Sets up the window as a frameless, transparent, fixed-size flyout: hidden
/// and outside the taskbar until opened from the tray icon. Must be called
/// before `runApp`, after `WidgetsFlutterBinding.ensureInitialized()`.
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
      // Transparent so the pop-in scale/fade doesn't show a plain window
      // background around the shrunk content.
      backgroundColor: Color(0x00000000),
      title: 'DayPilot',
    ),
    () async {
      await windowManager.setAsFrameless();
      await windowManager.setResizable(false);
      // Floats above whatever else has focus whenever it's shown, since
      // the tray click that opens it shouldn't have to fight for z-order.
      await windowManager.setAlwaysOnTop(true);
      // Left hidden here on purpose — only the tray icon opens it.
    },
  );

  await trayManager.setIcon(
    Platform.isWindows
        ? 'assets/images/tray_icon.ico'
        : 'assets/images/tray_icon.png',
  );
  await trayManager.setContextMenu(
    Menu(
      items: [
        MenuItem(key: 'open_app', label: 'Abrir'),
        MenuItem.separator(),
        MenuItem(key: 'exit_app', label: 'Salir'),
      ],
    ),
  );
}

/// Bottom-right corner of the screen's work area (the region excluding the
/// taskbar/panel/dock, whichever edge it's on). Linux's tray backend
/// (AppIndicator) never reports the icon's actual position, so unlike
/// Windows/macOS this can't be anchored to the exact icon/click point — it
/// always opens in this corner.
Future<Offset> _cornerPosition() async {
  final display = await screenRetriever.getPrimaryDisplay();
  final areaOrigin = display.visiblePosition ?? Offset.zero;
  final areaSize = display.visibleSize ?? display.size;
  return Offset(
    areaOrigin.dx + areaSize.width - mobileWindowSize.width - _screenEdgeMargin,
    areaOrigin.dy + areaSize.height - mobileWindowSize.height - _screenEdgeMargin,
  );
}

/// Wraps the app root to drive the flyout: the tray menu opens/hides it, it
/// hides itself when it loses focus (click elsewhere) — same as a
/// OneDrive-style tray popup — and it pops in/out with a scale+fade instead
/// of the window itself moving (window managers don't reliably animate
/// window position, especially on Linux, but animating the content inside
/// an already-placed, transparent window works everywhere).
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
