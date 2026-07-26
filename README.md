# DayPilot

Aplicación de productividad (tareas, pasos, temporizadores Pomodoro, límites de uso de apps, ranking social), con Supabase como backend. Construida originalmente para Android con Kotlin y Jetpack Compose, y más tarde adaptada a Flutter para dar soporte multiplataforma de escritorio.

Los usuarios registran tareas diarias, pasos y límites de uso de apps, y compiten con sus amigos en un ranking de puntos de 30 días. La app incluye temporizadores (Pomodoro y personalizados), recordatorios, un sistema de notificaciones en tiempo real y un resumen semanal de progreso.

## Ramas

**Android (Kotlin, Jetpack Compose):**

| Rama | Propósito |
|---|---|
| `Incremento-Android` | Rama principal de desarrollo — base para futuros incrementos |
| `Incremento-Android-TestFinal` | Rama de trabajo/testing activa, fusionada periódicamente en `Incremento-Android` |
| `Informacion-Supabase` | Archivos de migración SQL (esquema, datos semilla, script de borrado) |
| `Version-Original` | Código original antes del incremento de Android |
| `Test-Diseno-Android` | Rama de pruebas de solo sistema de diseño |
| `Test-Funcional-Android` | Rama de pruebas funcionales |
| `Test-Supabase-Android` | Prueba de concepto independiente que valida la conectividad con Supabase desde Android |

**Flutter (Dart, multiplataforma):**

| Rama | Propósito |
|---|---|
| `Incremento-Flutter` | Incremento final — arquitectura real con Riverpod y backend real de Supabase |
| `Incremento-Flutter-TestFinal` | Rama de trabajo/testing activa, fusionada periódicamente en `Incremento-Flutter` |
| `Test-Diseño-Flutter` | Base de solo sistema de diseño (temas, componentes, datos de prueba) |
| `Test-Supabase-Flutter` | Prueba de concepto independiente que valida `supabase_flutter` |

## Tecnologías

| | Android | Flutter |
|---|---|---|
| **Lenguaje** | Kotlin | Dart |
| **UI** | Jetpack Compose + Material 3 | Widgets de Flutter + Material 3 |
| **Gestión de estado** | MVVM con patrón repositorio | Riverpod |
| **Backend** | Supabase (PostgreSQL, Auth, Storage, Realtime) | Supabase (PostgreSQL, Auth, Storage, Realtime) |
| **CI/CD** | GitHub Actions | GitHub Actions |

## Descargas

**Android:**
- [⬇️ Descargar DayPilot (Version-Original)](https://github.com/MarTonPerCar/DayPilot/releases/download/v0.1.1/DayPilot.apk)
- [⬇️ Descargar DayPilot (Incremento-Android)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-android-latest/DayPilot-Incremento-Android.apk)

**Flutter (Windows/Linux):**
- [⬇️ Descargar DayPilot para Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.exe) — ejecuta el instalador tras descargarlo
- Linux (.deb):
  ```bash
  wget -O DayPilot-Setup.deb https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.deb && sudo apt install ./DayPilot-Setup.deb
  ```

**iOS / macOS:** todavía no disponible — desarrollo futuro. Distribuir una build sin firmar no es viable en ninguna de las dos plataformas: macOS Gatekeeper bloquea las apps sin firmar para usuarios normales, y iOS no permite instalar un `.ipa` sin firmar fuera del despliegue propio de Xcode a dispositivo. Ambas necesitan una cuenta de Apple Developer para firmar y notarizar/distribuir correctamente.

## Páginas de autenticación (`docs/`)

Páginas estáticas a las que Supabase Auth redirige para la confirmación de email y el restablecimiento de contraseña (`docs/confirm.html`, `docs/reset-password.html`), además de las plantillas HTML de email configuradas en el panel de Supabase (`docs/emails/`). Se despliegan mediante GitHub Pages a través de `.github/workflows/deploy-pages.yml`, que inyecta `SUPABASE_URL`/`SUPABASE_KEY` desde los secrets del repositorio en `reset-password.html` en el momento del despliegue en lugar de comitearlos — requiere que el origen de Pages del repositorio esté configurado como "GitHub Actions" (Settings → Pages).
