import 'package:flutter/material.dart';
import 'core/window/desktop_window.dart';
import 'l10n/app_localizations.dart';
import 'l10n/locale_notifier.dart';
import 'screens/auth/login_screen.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initDesktopWindow();
  runApp(const DesktopFlyoutScope(child: DayPilotApp()));
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
                  home: const LoginScreen(),
                );
              },
            );
          },
        );
      },
    );
  }
}
