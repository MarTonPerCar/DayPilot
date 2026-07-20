# DayPilot (Flutter)

Flutter port of DayPilot, a productivity app (tasks, steps, Pomodoro timers, app usage limits, social ranking) originally built for Android with Kotlin and Jetpack Compose, backed by Supabase.

Users track daily tasks, steps, and app usage limits, and compete with friends on a 30-day points ranking. The app includes timers (Pomodoro and custom), reminders, a real-time notification system, and a weekly progress summary.

This branch, `Incremento-Flutter`, is the **stable, permanent home** for the final increment: real UI, real Riverpod architecture, and a real Supabase backend, built on top of the design system from `Test-Diseño-Flutter`. Active work and testing happen on `Incremento-Flutter-TestFinal`, which is merged in here periodically once it's verified.

## Features

- Tasks (create/edit/complete, recurrence, points on completion)
- Steps tracking with daily goals and milestone bonuses
- Pomodoro and custom timers, with a once-per-day point bonus
- Tech Health: per-app usage restrictions — Android-only for now (the configuration screen itself is gated to Android; other platforms show an "unavailable" screen, since real-time usage watching/blocking hasn't been wired up for them yet)
- Friends, friend requests, and a 30-day points ranking
- Real-time in-app notifications (friend activity, reactions, level-ups, streak risk, goals, tasks, timers, reminders, daily summary)
- Weekly progress summary with reactions
- Spanish, English, and German localization
- Desktop support (Windows/Linux): runs as a tray-icon flyout window sized like a phone

## Stack

- **Language:** Dart
- **Framework:** Flutter
- **State management:** Riverpod (`Notifier`/`NotifierProvider`, no codegen)
- **Backend:** Supabase (PostgreSQL, Auth, Storage, Realtime)
- **Architecture:** one repository interface per domain (with a Supabase-backed implementation), one or more Riverpod notifiers per domain
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

Releases for this branch are tagged `v2.1.X.Y.Z` (the repo-wide scheme: `V`=platform — `1` Android, `2` Flutter — `SV`=branch — `1` main, `2` TestFinal — followed by the real semantic version). CI also maintains a floating `incremento-flutter-latest` tag that always points at the newest build for this branch specifically.

- [⬇️ Download DayPilot for Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.exe) — run the installer after downloading
- Linux (.deb):
  ```bash
  wget -O DayPilot-Setup.deb https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.deb && sudo apt install ./DayPilot-Setup.deb
  ```

> **Note:** these links always point at the latest release built from this branch (`Incremento-Flutter`), the stable version of the app.
