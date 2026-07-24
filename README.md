# DayPilot (Flutter)

[![Flutter Test](https://github.com/MarTonPerCar/DayPilot/actions/workflows/flutter-test.yml/badge.svg?branch=Incremento-Flutter-TestFinal)](https://github.com/MarTonPerCar/DayPilot/actions/workflows/flutter-test.yml)

*Haz clic en la insignia para ver el detalle de cada test individual (nombre y resultado), no solo el resultado global.*

Adaptación a Flutter de DayPilot, una aplicación de productividad (tareas, pasos, temporizadores Pomodoro, límites de uso de apps, ranking social) construida originalmente para Android con Kotlin y Jetpack Compose, con Supabase como backend.

Los usuarios registran tareas diarias, pasos y límites de uso de apps, y compiten con sus amigos en un ranking de puntos de 30 días. La app incluye temporizadores (Pomodoro y personalizados), recordatorios, un sistema de notificaciones en tiempo real y un resumen semanal de progreso.

Esta es la rama de trabajo activa para el incremento final: UI real, arquitectura real con Riverpod y backend real de Supabase, construida sobre el sistema de diseño de `Test-Diseño-Flutter`. Ya se ha fusionado una vez con `Incremento-Flutter` (la rama permanente), y se sigue re-fusionando periódicamente a medida que avanza el trabajo aquí.

## Funcionalidades

- Tareas (crear/editar/completar, recurrencia, puntos al completarlas)
- Seguimiento de pasos con metas diarias y bonificaciones por hitos
- Temporizadores Pomodoro y personalizados, con una bonificación de puntos diaria
- Salud tecnológica: restricciones de uso por app — de momento solo en Android (la propia pantalla de configuración está limitada a Android; en otras plataformas se muestra una pantalla de "no disponible", ya que el control en tiempo real del uso/bloqueo aún no está implementado para ellas)
- Amigos, solicitudes de amistad y un ranking de puntos de 30 días
- Notificaciones en la app en tiempo real (actividad de amigos, reacciones, subidas de nivel, riesgo de racha, metas, tareas, temporizadores, recordatorios, resumen diario)
- Resumen semanal de progreso con reacciones
- Localización en español, inglés y alemán
- Soporte de escritorio (Windows/Linux): funciona como una ventana flotante desde el icono de la bandeja, con el tamaño de un móvil

## Tecnologías

- **Lenguaje:** Dart
- **Framework:** Flutter
- **Gestión de estado:** Riverpod (`Notifier`/`NotifierProvider`, sin generación de código)
- **Backend:** Supabase (PostgreSQL, Auth, Storage, Realtime)
- **Arquitectura:** una interfaz de repositorio por dominio (con una implementación respaldada por Supabase), uno o más notifiers de Riverpod por dominio
- **Localización:** `flutter_localizations`, archivos ARB (es/en/de)

## Ejecutar en local

1. Copia `env.json.example` a `env.json` y rellena la URL y la clave publicable de tu proyecto de Supabase.
2. `flutter pub get`
3. `flutter run -d <windows|linux|macos|android|chrome>`

## Ramas relacionadas

| Rama | Propósito |
|---|---|
| `Incremento-Flutter` | Rama permanente a la que se fusiona periódicamente este trabajo |
| `Test-Diseño-Flutter` | Base de solo sistema de diseño sobre la que se construye esta rama |
| `Test-Supabase-Flutter` | Prueba de concepto independiente que valida `supabase_flutter` contra la base de datos real del proyecto |

## Descargas

Los releases de esta rama se etiquetan como `v2.2.X.Y.Z` (el esquema del repositorio completo: `V`=plataforma — `1` Android, `2` Flutter — `SV`=rama — `1` main, `2` TestFinal — seguido de la versión semántica real). La CI también mantiene una etiqueta flotante `incremento-flutter-testfinal-latest` que siempre apunta a la build más reciente de esta rama en concreto — a diferencia del "latest" general del repositorio, que mezcla ramas de Android y Flutter no relacionadas y puede acabar apuntando a la build equivocada.

- [⬇️ Descargar DayPilot para Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-testfinal-latest/DayPilot-Setup.exe) — ejecuta el instalador tras descargarlo
- Linux (.deb):
  ```bash
  wget -O DayPilot-Setup.deb https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-testfinal-latest/DayPilot-Setup.deb && sudo apt install ./DayPilot-Setup.deb
  ```

> **Nota:** la etiqueta/release `incremento-flutter-testfinal-latest` la crea la CI en el próximo push de tag (`v2.2.X.Y.Z`) y todavía no existe — estos enlaces darán 404 hasta entonces. Esta es la rama de trabajo activa — las builds aquí pueden contener errores aún en proceso de arreglo, o ser idénticas al último release de `Incremento-Flutter` si todavía no ha cambiado nada. Para la versión estable, usa las descargas de `Incremento-Flutter`.
