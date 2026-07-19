# DayPilot (Flutter)

Flutter port of DayPilot, a productivity app (tasks, steps, Pomodoro timers, app usage limits, social ranking) originally built for Android with Kotlin and Jetpack Compose, backed by Supabase.

Users track daily tasks, steps, and app usage limits, and compete with friends on a 30-day points ranking. The app includes timers (Pomodoro and custom), reminders, a real-time notification system, and a weekly progress summary.

This branch, `Incremento-Flutter`, is the **stable, permanent home** for the final increment: real UI, real Riverpod architecture, and a real Supabase backend, built on top of the design system from `Test-Diseño-Flutter`. Active work and testing happen on `Incremento-Flutter-TestFinal`, which is merged in here periodically once it's verified.

## Features

- Tasks (create/edit/complete, recurrence, points on completion)
- Steps tracking with daily goals and milestone bonuses
- Pomodoro and custom timers, with a once-per-day point bonus
- Tech Health: per-app usage restrictions (configuration only — enforcement/blocking isn't built yet on any platform but Android's native app)
- Friends, friend requests, and a 30-day points ranking
- Real-time in-app notifications (friend activity, level-ups, goals, tasks)
- Weekly progress summary with reactions
- Spanish, English, and German localization
- Desktop support (Windows/Linux): runs as a tray-icon flyout window sized like a phone

## Stack

- **Language:** Dart
- **Framework:** Flutter
- **State management:** Riverpod (`Notifier`/`NotifierProvider`, no codegen)
- **Backend:** Supabase (PostgreSQL, Auth, Storage, Realtime)
- **Architecture:** one repository interface + one notifier per domain
- **Localization:** `flutter_localizations`, ARB files (es/en/de)

## Running locally

1. Copy `env.json.example` to `env.json` and fill in your Supabase project's URL and publishable key.
2. `flutter pub get`
3. `flutter run -d <windows|linux|macos|android|chrome>`

## Related branches

| Branch | Purpose |
|---|---|
| `Incremento-Flutter-TestFinal` | Active working branch, merged in here periodically once verified |
| `Test-Diseño-Flutter` | Design-system-only base this work builds on |
| `Test-Supabase-Flutter` | Standalone spike validating `supabase_flutter` against the real project database |

## Downloads

- [⬇️ Download DayPilot for Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.exe)
- [⬇️ Download DayPilot for Linux (.deb)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.deb)

> **Note:** these links always point at the latest release built from this branch (`Incremento-Flutter`), the stable version of the app.
