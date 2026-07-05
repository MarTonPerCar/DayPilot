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

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// Sets up the window as a frameless, fixed-size flyout: hidden and outside
/// the taskbar until opened from the tray icon. Must be called before
/// `runApp`, after `WidgetsFlutterBinding.ensureInitialized()`.
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
      title: 'DayPilot',
    ),
    () async {
      await windowManager.setAsFrameless();
      await windowManager.setResizable(false);
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

/// Moves the flyout to the bottom-right corner of the screen's work area
/// (the region excluding the taskbar/panel/dock — whichever edge it's on)
/// and shows it. Linux's tray backend (AppIndicator) never reports the
/// icon's actual position, so unlike Windows/macOS this can't be anchored to
/// the exact icon/click point — it always opens in this corner.
Future<void> _showFlyout() async {
  final display = await screenRetriever.getPrimaryDisplay();
  final areaOrigin = display.visiblePosition ?? Offset.zero;
  final areaSize = display.visibleSize ?? display.size;
  final position = Offset(
    areaOrigin.dx + areaSize.width - mobileWindowSize.width - _screenEdgeMargin,
    areaOrigin.dy + areaSize.height - mobileWindowSize.height - _screenEdgeMargin,
  );
  await windowManager.setPosition(position);
  await windowManager.show();
  await windowManager.focus();
}

/// Wraps the app root to drive the flyout: the tray menu opens/hides it and
/// it hides itself when it loses focus (click elsewhere), same as a
/// OneDrive-style tray popup.
class DesktopFlyoutScope extends StatefulWidget {
  const DesktopFlyoutScope({super.key, required this.child});

  final Widget child;

  @override
  State<DesktopFlyoutScope> createState() => _DesktopFlyoutScopeState();
}

class _DesktopFlyoutScopeState extends State<DesktopFlyoutScope>
    with WindowListener, TrayListener {
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

  @override
  void onWindowBlur() async {
    await windowManager.hide();
  }

  @override
  void onTrayIconMouseDown() async {
    if (await windowManager.isVisible()) {
      await windowManager.hide();
    } else {
      await _showFlyout();
    }
  }

  @override
  void onTrayIconRightMouseDown() {
    trayManager.popUpContextMenu();
  }

  @override
  void onTrayMenuItemClick(MenuItem menuItem) async {
    switch (menuItem.key) {
      case 'open_app':
        await _showFlyout();
      case 'exit_app':
        await windowManager.close();
    }
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
