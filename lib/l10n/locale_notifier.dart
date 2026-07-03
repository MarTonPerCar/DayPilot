import 'package:flutter/material.dart';

/// Fuente única de verdad para el idioma activo, para que cambiarlo desde
/// Ajustes se refleje al instante en toda la app.
final dayPilotLocaleNotifier = ValueNotifier<Locale>(const Locale('es'));
