import 'package:flutter/material.dart';
import 'screens/auth/login_screen.dart';
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
      home: const LoginScreen(),
    );
  }
}
