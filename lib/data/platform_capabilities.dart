import 'package:flutter/foundation.dart';

/// Punto único para preguntar si el dispositivo actual soporta funciones que
/// dependen de APIs específicas de Android/iOS (permisos del sistema,
/// estadísticas de uso, accesibilidad...). Cuando lleguen más pantallas
/// atadas a permisos del sistema operativo, deben consultar esto en lugar
/// de repetir la comprobación de plataforma por su cuenta.
class PlatformCapabilities {
  PlatformCapabilities._();

  static bool get supportsDeviceFeatures =>
      defaultTargetPlatform == TargetPlatform.android || defaultTargetPlatform == TargetPlatform.iOS;
}
