import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'core/auth/auth_gate.dart';
import 'core/window/desktop_window.dart';
import 'l10n/app_localizations.dart';
import 'l10n/locale_notifier.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initDesktopWindow();

  final env = jsonDecode(await rootBundle.loadString('env.json')) as Map<String, dynamic>;
  await Supabase.initialize(
    url: env['SUPABASE_URL'] as String,
    publishableKey: env['SUPABASE_KEY'] as String,
  );

  runApp(
    const ProviderScope(
      child: DesktopFlyoutScope(child: DayPilotApp()),
    ),
  );
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
                    if (!isDesktopPlatform) return child!;
                    const borderWidth = 5.0;
                    return Container(
                      padding: const EdgeInsets.all(borderWidth),
                      decoration: const BoxDecoration(
                        border: Border.fromBorderSide(
                          BorderSide(color: Colors.black, width: borderWidth),
                        ),
                      ),
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
