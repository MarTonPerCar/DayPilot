import 'dart:ffi' hide Size;
import 'dart:io';

import 'package:ffi/ffi.dart' as ffi;
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:launch_at_startup/launch_at_startup.dart';
import 'package:screen_retriever/screen_retriever.dart';
import 'package:tray_manager/tray_manager.dart';
import 'package:win32/win32.dart' as win32;
import 'package:window_manager/window_manager.dart';

import '../data/notification_l10n.dart';
import '../logging/app_logger.dart';
import '../prefs/app_prefs.dart';
import '../../l10n/locale_notifier.dart';

const Size mobileWindowSize = Size(390, 844);
const Duration _popDuration = Duration(milliseconds: 260);
const Curve _popInCurve = Curves.easeOutCubic;
const Curve _popOutCurve = Curves.easeInCubic;

// Matches app_theme.dart's darkest surface tone — the native window's
// fallback color during animation, so any sliver exposed while the
// content shrinks/grows blends in instead of looking like stray UI.
const _windowBackgroundColor = Color(0x00000000);

const int _vkEscape = 0x1B;
const int _keyeventfKeyup = 0x0002;

bool get isDesktopPlatform =>
    !kIsWeb && (Platform.isLinux || Platform.isWindows || Platform.isMacOS);

/// Kept for compatibility with edit_profile_screen.dart's file-picker guard.
final isPickingFileNotifier = ValueNotifier<bool>(false);

/// Drives the pop-in/pop-out animation in [DesktopFlyoutAnimator].
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

/// Closes Windows' native "show hidden icons" tray flyout if it happened to
/// be open when our tray icon was clicked (our icon can live inside that
/// overflow panel) — otherwise it lingers on screen, overlapping our own
/// window with stray system UI. Escape reliably dismisses that panel, same
/// as it would if the user pressed it themselves.
void _dismissWindowsTrayFlyout() {
  if (!Platform.isWindows) return;
  final inputs = ffi.calloc<win32.INPUT>(2);
  try {
    inputs[0].type = win32.INPUT_KEYBOARD;
    inputs[0].ki.wVk = win32.VIRTUAL_KEY.VK_ESCAPE;
    inputs[1].type = win32.INPUT_KEYBOARD;
    inputs[1].ki.wVk = win32.VIRTUAL_KEY.VK_ESCAPE;
    inputs[1].ki.dwFlags = win32.KEYBD_EVENT_FLAGS.KEYEVENTF_KEYUP;
    win32.SendInput(2, inputs, sizeOf<win32.INPUT>());
  } finally {
    ffi.calloc.free(inputs);
  }
}

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
  await windowManager.setAlwaysOnTop(true);
  flyoutVisibleNotifier.value = false;
  await Future.delayed(_popDuration);
  await windowManager.setAlwaysOnTop(false);
  await windowManager.hide();
  await _logWindowState('after $reason -> hide()');
  _isHiding = false;
}

Future<void> _showWithAnimation(String reason) async {
  if (_isShowing) {
    AppLogger.log('_showWithAnimation($reason) ignored — already showing');
    return;
  }
  _isShowing = true;
  _lastShowAt = DateTime.now();
  AppLogger.log('_showWithAnimation($reason)');
  _dismissWindowsTrayFlyout();
  await _alignToPrimaryTaskbarCorner();
  await windowManager.show();
  flyoutVisibleNotifier.value = true;
  // Some window managers (Linux especially) ignore a focus request right
  // after the window is mapped. Forcing always-on-top briefly raises it
  // regardless of whether the WM grants keyboard focus.
  await windowManager.setAlwaysOnTop(true);
  await Future.delayed(const Duration(milliseconds: 60));
  await windowManager.focus();
  await windowManager.setAlwaysOnTop(false);
  await _logWindowState('after $reason -> show()');
  _isShowing = false;
}

Future<void> _configureLaunchAtStartup() async {
  launchAtStartup.setup(appName: 'DayPilot', appPath: Platform.resolvedExecutable);
  AppLogger.log('launchAtStartup configured');

  final prefs = await AppPrefs.load();
  if (!prefs.launchAtStartupConfigured) {
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
      backgroundColor: _windowBackgroundColor,
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
    AppLogger.log('onWindowBlur, isPickingFile=${isPickingFileNotifier.value}');
    if (isPickingFileNotifier.value) return;
    await _hideWithAnimation('onWindowBlur');
  }

  @override
  void onWindowFocus() {
    AppLogger.log('onWindowFocus');
  }
}

/// Wraps the app in the pop-in/pop-out grow-from-corner animation. No
/// separate backdrop layer anymore — the native window's own
/// [_windowBackgroundColor] handles the "don't show stray pixels while
/// content is smaller than the window" job now, so the whole thing (app +
/// border) shrinks/grows as one unified block instead of a static panel
/// sitting behind a shrinking card.
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
          child: TweenAnimationBuilder<double>(
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
        );
      },
      child: child,
    );
  }
}