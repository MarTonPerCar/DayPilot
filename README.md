# DayPilot (Flutter)

Flutter port of DayPilot, a productivity app (tasks, steps, Pomodoro timers, app usage limits, social ranking) originally built for Android with Kotlin and Jetpack Compose, backed by Supabase.

Users track daily tasks, steps, and app usage limits, and compete with friends on a 30-day points ranking. The app includes timers (Pomodoro and custom), reminders, a real-time notification system, and a weekly progress summary.

This is the active working branch for the final increment: real UI, real Riverpod architecture, and a real Supabase backend, built on top of the design system from `Test-Diseño-Flutter`. It has already been merged once into `Incremento-Flutter` (the permanent branch), and continues to be re-merged there periodically as work lands here.

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
| `Incremento-Flutter` | Permanent branch this work is periodically merged into |
| `Test-Diseño-Flutter` | Design-system-only base this branch builds on |
| `Test-Supabase-Flutter` | Standalone spike validating `supabase_flutter` against the real project database |

## Downloads

Releases for this branch are tagged `v2.2.X.Y.Z` (the repo-wide scheme: `V`=platform — `1` Android, `2` Flutter — `SV`=branch — `1` main, `2` TestFinal — followed by the real semantic version). CI also maintains a floating `incremento-flutter-testfinal-latest` tag that always points at the newest build for this branch specifically — unlike the repo-wide "latest" release, which mixes in unrelated Android/Flutter branches and can resolve to the wrong build entirely.

- [⬇️ Download DayPilot for Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-testfinal-latest/DayPilot-Setup.exe)
- [⬇️ Download DayPilot for Linux (.deb)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-testfinal-latest/DayPilot-Setup.deb)

> **Note:** the `incremento-flutter-testfinal-latest` tag/release is created by CI on the next tag push (`v2.2.X.Y.Z`) and doesn't exist yet — these links will 404 until then. This is the active working branch — builds here may contain bugs still being worked out, or may be identical to the latest `Incremento-Flutter` release if nothing has changed yet. For the stable version, use the downloads on `Incremento-Flutter` instead.
