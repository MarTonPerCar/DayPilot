# DayPilot (Flutter — Design System)

Flutter port of DayPilot, a productivity app (tasks, steps, Pomodoro timers, app usage limits, social ranking) originally built for Android with Kotlin and Jetpack Compose, backed by Supabase.

This branch is the **design-system-only starting point** for the Flutter port: a full Material 3 theming system, a library of reusable presentation components, and every screen wired to dummy/mock data. There is no state management, routing, or persistence yet — that's built on top of this in `Incremento-Flutter`.

## What's here

- **5 Material 3 themes** (light/dark variants each), switchable at runtime.
- **~75 reusable components** (`lib/components/{basic,cards,forms,sheets}`) — cards, form fields, chips, buttons, dialogs — used consistently across every screen; many (not all) are prefixed `DayPilot*`.
- **One file per screen**, each wired to static/dummy data so the UI can be reviewed without a backend. Create/edit/delete actions in forms and sheets are visual-only stubs (e.g. `onSave`/`onDelete` no-ops) — no data actually mutates on this branch.
- **Localization** in Spanish, English, and German (`lib/l10n`).
- A **component catalog** screen (`lib/component_catalog.dart`) previewing most reusable components in one place. Theme switching lives in the Settings screen, not the catalog.

## Related branches

| Branch | Purpose |
|---|---|
| `Incremento-Flutter` | Final increment — real Riverpod architecture, routing, and Supabase backend built on top of this design system |
| `Test-Supabase-Flutter` | Standalone spike validating `supabase_flutter` against the real project database |

## Stack

- **Language:** Dart
- **Framework:** Flutter
- **UI:** Material 3, custom `DayPilot*` component library
- **Localization:** `flutter_localizations`, ARB files (es/en/de)
