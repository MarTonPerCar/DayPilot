import 'package:flutter/material.dart';
import 'theme/app_theme.dart';

void main() {
  runApp(const DayPilotApp());
}

class DayPilotApp extends StatelessWidget {
  const DayPilotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DayPilot',
      debugShowCheckedModeBanner: false,
      theme:     buildTheme(DayPilotTheme.sageGreen),
      darkTheme: buildTheme(DayPilotTheme.sageGreen, darkMode: true),
      themeMode: ThemeMode.system,
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Scaffold(
      backgroundColor: colors.surface,
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.flight_takeoff_rounded,
                size: 72,
                color: colors.primary,
              ),
              const SizedBox(height: 24),
              Text(
                'DayPilot',
                style: text.displaySmall?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: colors.onSurface,
                  letterSpacing: -1,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Tu día, bajo control.',
                style: text.bodyLarge?.copyWith(
                  color: colors.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
