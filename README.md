# DayPilot

Productivity app (tasks, steps, Pomodoro timers, app usage limits, social ranking), backed by Supabase. Originally built for Android with Kotlin and Jetpack Compose, later ported to Flutter for cross-platform desktop support.

Users track daily tasks, steps, and app usage limits, and compete with friends on a 30-day points ranking. The app includes timers (Pomodoro and custom), reminders, a real-time notification system, and a weekly progress summary.

## Branches

**Android (Kotlin, Jetpack Compose):**

| Branch | Purpose |
|---|---|
| `Incremento-Android` | Main development branch — base for future increments |
| `Incremento-Android-TestFinal` | Final state of the first completed increment |
| `Informacion-Supabase` | SQL migration files (schema, seed data, drop script) |
| `Version-Original` | Original codebase before the Android increment |
| `Test-Funcional-Android` | Functional testing branch |

**Flutter (Dart, cross-platform):**

| Branch | Purpose |
|---|---|
| `Incremento-Flutter` | Final increment — real Riverpod architecture and Supabase backend |
| `Test-Diseño-Flutter` | Design-system-only base (themes, components, dummy data) |
| `Test-Supabase-Flutter` | Standalone spike validating `supabase_flutter` |

## Stack

| | Android | Flutter |
|---|---|---|
| **Language** | Kotlin | Dart |
| **UI** | Jetpack Compose + Material 3 | Flutter widgets + Material 3 |
| **State management** | MVVM with repository pattern | Riverpod |
| **Backend** | Supabase (PostgreSQL, Auth, Storage, Realtime) | Supabase (PostgreSQL, Auth, Storage, Realtime) |
| **CI/CD** | GitHub Actions | GitHub Actions |

## Downloads

**Android:**
- [⬇️ Descargar DayPilot (Version-Original)](https://github.com/MarTonPerCar/DayPilot/releases/download/v0.1.1/DayPilot.apk)
- [⬇️ Descargar DayPilot (Incremento-Android)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-android-latest/DayPilot-Incremento-Android.apk)

**Flutter (Windows/Linux):**
- [⬇️ Download DayPilot for Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Windows.zip)
- [⬇️ Download DayPilot for Linux](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Linux.zip)

**iOS / macOS:** not available yet — future development. Distributing an unsigned build isn't practical on either platform: macOS Gatekeeper blocks unsigned apps for normal users, and iOS can't install an unsigned `.ipa` outside Xcode's own device deployment. Both need an Apple Developer account to sign and notarize/distribute properly.
