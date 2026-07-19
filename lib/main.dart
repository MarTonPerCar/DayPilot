import 'dart:async';
import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'core/auth/auth_gate.dart';
import 'core/logging/app_logger.dart';
import 'core/prefs/app_prefs.dart';
import 'core/window/desktop_window.dart';
import 'l10n/app_localizations.dart';
import 'l10n/locale_notifier.dart';
import 'theme/app_theme.dart';

void main() async {
  runZonedGuarded(() async {
    WidgetsFlutterBinding.ensureInitialized();
    await AppLogger.init();

    FlutterError.onError = (details) {
      AppLogger.logError('FlutterError', details.exception, details.stack ?? StackTrace.current);
      FlutterError.presentError(details);
    };
    PlatformDispatcher.instance.onError = (error, stack) {
      AppLogger.logError('PlatformDispatcher', error, stack);
      return true;
    };

    try {
      await initDesktopWindow();
    } catch (e, st) {
      AppLogger.logError('initDesktopWindow', e, st);
    }

    final env = jsonDecode(await rootBundle.loadString('env.json')) as Map<String, dynamic>;
    await Supabase.initialize(
      url: env['SUPABASE_URL'] as String,
      publishableKey: env['SUPABASE_KEY'] as String,
    );

    await _restorePersistedPreferences();

    runApp(
      const ProviderScope(
        child: DesktopFlyoutAnimator(child: DayPilotApp()),
      ),
    );

  }, (error, stack) {
    AppLogger.logError('runZonedGuarded', error, stack);
  });
}

Future<void> _restorePersistedPreferences() async {
  final prefs = await AppPrefs.load();

  final theme = prefs.theme;
  if (theme != null) {
    dayPilotThemeNotifier.value = DayPilotTheme.values.byName(theme);
  }
  final themeMode = prefs.themeMode;
  if (themeMode != null) {
    dayPilotThemeModeNotifier.value = ThemeMode.values.byName(themeMode);
  }
  final locale = prefs.locale;
  if (locale != null) {
    dayPilotLocaleNotifier.value = Locale(locale);
  }

  dayPilotThemeNotifier.addListener(() => prefs.setTheme(dayPilotThemeNotifier.value.name));
  dayPilotThemeModeNotifier.addListener(() => prefs.setThemeMode(dayPilotThemeModeNotifier.value.name));
  dayPilotLocaleNotifier.addListener(() => prefs.setLocale(dayPilotLocaleNotifier.value.languageCode));
}

class DayPilotApp extends StatelessWidget {
  const DayPilotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<DayPilotTheme>(
      valueListenable: dayPilotThemeNotifier,
      builder: (context, activeTheme, _) {
        return ValueListenableBuilder<ThemeMode>(
          valueListenable: dayPilotThemeModeNotifier,
          builder: (context, activeThemeMode, _) {
            return ValueListenableBuilder<Locale>(
              valueListenable: dayPilotLocaleNotifier,
              builder: (context, activeLocale, _) {
                return MaterialApp(
                  title: 'DayPilot',
                  debugShowCheckedModeBanner: false,
                  theme: buildTheme(activeTheme),
                  darkTheme: buildTheme(activeTheme, darkMode: true),
                  themeMode: activeThemeMode,
                  locale: activeLocale,
                  localizationsDelegates: AppLocalizations.localizationsDelegates,
                  supportedLocales: AppLocalizations.supportedLocales,
                  home: const AuthGate(),
                  builder: (context, child) {
                    const borderWidth = 5.0;
                    return Container(
                      color: Colors.black,
                      padding: const EdgeInsets.all(borderWidth),
                      child: child,
                    );
                  },
                );
              },
            );
          },
        );
      },
    );
  }
}
