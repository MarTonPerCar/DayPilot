import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:launch_at_startup/launch_at_startup.dart';
import 'package:screen_retriever/screen_retriever.dart';
import 'package:tray_manager/tray_manager.dart';
import 'package:window_manager/window_manager.dart';

import '../data/notification_l10n.dart';
import '../logging/app_logger.dart';
import '../prefs/app_prefs.dart';
import '../../l10n/locale_notifier.dart';

const Size mobileWindowSize = Size(390, 844);
const Duration _popDuration = Duration(milliseconds: 260);
const Curve _popInCurve = Curves.easeOutCubic;
const Curve _popOutCurve = Curves.easeInCubic;

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// Kept for compatibility with edit_profile_screen.dart's file-picker guard.
final isPickingFileNotifier = ValueNotifier<bool>(false);

/// Drives the pop-in/pop-out animation in [DesktopFlyoutAnimator]. Toggled
/// from here (not from a widget's setState) because the tray/window-close
/// handlers below are the ones that know when a hide/show is happening.
final flyoutVisibleNotifier = ValueNotifier<bool>(true);

DateTime? _lastShowAt;
bool _isHiding = false;
bool _isShowing = false;

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

Future<void> _alignToPrimaryTaskbarCorner() async {
  final primary = await screenRetriever.getPrimaryDisplay();
  final workPos = primary.visiblePosition ?? const Offset(0, 0);
  final workSize = primary.visibleSize ?? primary.size;
  final windowSize = await windowManager.getSize();

  final x = workPos.dx + workSize.width - windowSize.width;
  final y = workPos.dy + workSize.height - windowSize.height;

  await windowManager.setPosition(Offset(x, y));
}

/// Plays the pop-out animation, THEN hides the native window — the window
/// has to still be on screen while the animation runs, or there'd be
/// nothing visible to animate.
Future<void> _hideWithAnimation(String reason) async {
  if (_isHiding) {
    AppLogger.log('_hideWithAnimation($reason) ignored — already hiding');
    return;
  }
  if (_lastShowAt != null &&
      DateTime.now().difference(_lastShowAt!) < const Duration(milliseconds: 400)) {
    AppLogger.log('_hideWithAnimation($reason) ignored — too soon after show()');
    return;
  }
  _isHiding = true;
  AppLogger.log('_hideWithAnimation($reason)');
  // Force topmost just for the closing animation, so whatever window just
  // took focus (e.g. an app the user clicked) doesn't instantly cover ours
  // and cut the animation off before it's even visible.
  await windowManager.setAlwaysOnTop(true);
  flyoutVisibleNotifier.value = false;
  await Future.delayed(_popDuration);
  await windowManager.setAlwaysOnTop(false);
  await windowManager.hide();
  await _logWindowState('after $reason -> hide()');
  _isHiding = false;
}

/// Shows the native window first, THEN plays the pop-in animation.
Future<void> _showWithAnimation(String reason) async {
  if (_isShowing) {
    AppLogger.log('_showWithAnimation($reason) ignored — already showing');
    return;
  }
  _isShowing = true;
  // Set the grace-period timestamp BEFORE the native show()/focus() calls —
  // a spurious blur can fire while they're still in flight, and if we only
  // stamp the time afterward, that blur slips through uncovered.
  _lastShowAt = DateTime.now();
  AppLogger.log('_showWithAnimation($reason)');
  await _alignToPrimaryTaskbarCorner();
  await windowManager.show();
  flyoutVisibleNotifier.value = true;
  await windowManager.focus();
  await _logWindowState('after $reason -> show()');
  _isShowing = false;
}

Future<void> _configureLaunchAtStartup() async {
  launchAtStartup.setup(appName: 'DayPilot', appPath: Platform.resolvedExecutable);
  AppLogger.log('launchAtStartup configured');

  final prefs = await AppPrefs.load();
  if (!prefs.launchAtStartupConfigured) {
    // First run ever: default to enabled, like most tray apps do. On every
    // later run we leave it alone and respect whatever the user chose via
    // the Settings toggle, instead of silently re-enabling it each launch.
    await launchAtStartup.enable();
    await prefs.setLaunchAtStartupConfigured(true);
    AppLogger.log('launchAtStartup: first run, enabled by default');
  }
}

Future<void> initDesktopWindow() async {
  if (!isDesktopPlatform) {
    AppLogger.log('initDesktopWindow: not a desktop platform, skipping');
    return;
  }

  await windowManager.ensureInitialized();
  AppLogger.log('windowManager.ensureInitialized() done');
  await _logWindowState('right after ensureInitialized');

  await windowManager.hide();
  AppLogger.log('windowManager.hide() called explicitly');
  await _logWindowState('after explicit hide()');

  await windowManager.waitUntilReadyToShow(
    WindowOptions(
      size: mobileWindowSize,
      minimumSize: mobileWindowSize,
      maximumSize: mobileWindowSize,
      skipTaskbar: true,
      backgroundColor: const Color(0x00000000),
      title: 'DayPilot',
    ),
    () async {
      AppLogger.log('waitUntilReadyToShow callback entered');
      await windowManager.setAsFrameless();
      await windowManager.setResizable(false);
      await _alignToPrimaryTaskbarCorner();
      await windowManager.show();
      await windowManager.focus();
      flyoutVisibleNotifier.value = true;
      await _logWindowState('after show+focus');
    },
  );
  AppLogger.log('initDesktopWindow complete');

  await trayManager.setIcon(
    Platform.isWindows
        ? 'assets/images/tray_icon.ico'
        : 'assets/images/tray_icon.png',
  );
  AppLogger.log('Tray icon set');

  await _setTrayMenu();
  dayPilotLocaleNotifier.addListener(_setTrayMenu);

  TrayIconClickHandler.register();

  await windowManager.setPreventClose(true);
  AppLogger.log('setPreventClose(true) set');
  WindowCloseHandler.register();

  await _configureLaunchAtStartup();
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

class TrayIconClickHandler with TrayListener {
  TrayIconClickHandler._();

  static void register() {
    trayManager.addListener(TrayIconClickHandler._());
  }

  Future<void> _showOrFocus() async {
    final visible = await windowManager.isVisible();
    AppLogger.log('_showOrFocus, currently visible=$visible');
    if (!visible) {
      await _showWithAnimation('_showOrFocus');
    } else {
      _lastShowAt = DateTime.now();
      await windowManager.focus();
    }
    await _logWindowState('after _showOrFocus');
  }

  @override
  void onTrayIconMouseDown() {
    AppLogger.log('onTrayIconMouseDown');
    _showOrFocus();
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
        _showOrFocus();
      case 'exit_app':
        windowManager.setPreventClose(false);
        windowManager.close();
    }
  }
}

/// Intercepts the native close (X button / Alt+F4) and hides the window
/// instead of letting the app exit. Also hides on blur (click outside),
/// unless a native file picker is currently open. Both use the animated
/// hide so the pop-out plays before the window actually disappears.
class WindowCloseHandler with WindowListener {
  WindowCloseHandler._();

  static void register() {
    windowManager.addListener(WindowCloseHandler._());
  }

  @override
  void onWindowClose() async {
    await _hideWithAnimation('onWindowClose');
  }

  @override
  void onWindowBlur() async {
    AppLogger.log(
      'onWindowBlur, isPickingFile=${isPickingFileNotifier.value}',
    );
    if (isPickingFileNotifier.value) return;
    await _hideWithAnimation('onWindowBlur');
  }

  @override
  void onWindowFocus() {
    AppLogger.log('onWindowFocus');
  }
}

/// Wraps the app in the pop-in/pop-out grow-from-corner animation.
class DesktopFlyoutAnimator extends StatelessWidget {
  const DesktopFlyoutAnimator({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    if (!isDesktopPlatform) return child;
    return ValueListenableBuilder<bool>(
      valueListenable: flyoutVisibleNotifier,
      builder: (context, visible, child) {
        return Directionality(
          textDirection: TextDirection.ltr,
          child: Stack(
            children: [
              // Paints the full window every frame so shrinking/growing
              // never leaves a stale, unrepainted "ghost" of the previous
              // frame behind.
              const Positioned.fill(
                child: ColoredBox(color: Color(0xFF0E1F13)),
              ),
              TweenAnimationBuilder<double>(
                tween: Tween(begin: 0, end: visible ? 1.0 : 0.0),
                duration: _popDuration,
                curve: visible ? _popInCurve : _popOutCurve,
                builder: (context, t, child) {
                  final scale = 0.05 + (t * 0.95);
                  return Transform.scale(
                    scale: scale,
                    alignment: Alignment.bottomRight,
                    child: child,
                  );
                },
                child: child,
              ),
            ],
          ),
        );
      },
      child: child,
    );
  }
}