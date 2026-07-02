import 'package:flutter/material.dart';

enum DayPilotTheme { sageGreen, ocean, lavender, amber, amoled }

// Seed colors — one per theme
const _seedSageGreen = Color(0xFF4C9966);
const _seedOcean     = Color(0xFF0055A4);
const _seedLavender  = Color(0xFF7B5EA7);
const _seedAmber     = Color(0xFFE8A020);

ThemeData buildTheme(DayPilotTheme theme, {bool darkMode = false}) {
  if (theme == DayPilotTheme.amoled) return _amoledTheme();

  final seed = switch (theme) {
    DayPilotTheme.sageGreen => _seedSageGreen,
    DayPilotTheme.ocean     => _seedOcean,
    DayPilotTheme.lavender  => _seedLavender,
    DayPilotTheme.amber     => _seedAmber,
    DayPilotTheme.amoled    => _seedSageGreen,
  };

  return ThemeData(
    useMaterial3: true,
    colorScheme: ColorScheme.fromSeed(
      seedColor: seed,
      brightness: darkMode ? Brightness.dark : Brightness.light,
    ),
  );
}

// AMOLED: pure black surfaces to minimise OLED power draw
ThemeData _amoledTheme() {
  final base = ColorScheme.fromSeed(
    seedColor: Colors.blueGrey,
    brightness: Brightness.dark,
  );

  return ThemeData(
    useMaterial3: true,
    colorScheme: base.copyWith(
      surface:                    Colors.black,
      onSurface:                  Colors.white,
      surfaceContainerLowest:     Colors.black,
      surfaceContainerLow:        const Color(0xFF0A0A0A),
      surfaceContainer:           const Color(0xFF111111),
      surfaceContainerHigh:       const Color(0xFF1A1A1A),
      surfaceContainerHighest:    const Color(0xFF242424),
    ),
  );
}
