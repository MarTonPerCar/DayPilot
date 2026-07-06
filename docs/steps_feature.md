# Steps feature — implementation notes

Written for a fresh Claude Code session with zero prior context on this work. This
covers everything that was built for the "Steps" (pasos) feature in the
`Incremento-Flutter-TestFinal` branch: what it does, why it's shaped the way it is,
every file involved, and the exact DB migration that was applied to the live
Supabase project to support it.

## What this feature is (and isn't)

The Steps card on the Habits screen is a **read-only display** of step-counting data
that is generated elsewhere (the phone/Android side does the actual step counting
and points calculation). The Flutter app does **not** reimplement any step-counting
or points-generation logic — it only:

1. Reads today's step count, goal, and points already computed in Supabase.
2. Lets the user set a **new daily step goal**, which must apply consistently across
   every device the user is logged in on (see "The goal-change problem" below).

This scope was an explicit, deliberate decision by the user — do not add
client-side points logic, streak logic, or step-counting logic to this feature.

There is intentionally **no caching** for this feature (no `SessionCache` entry).
The user's own words: "this one [is] the few thing where the cache doesn't matter
and is just a display." It refetches from Supabase periodically instead (see
`StepsNotifier` below). This is a deliberate exception to the write-through
`SessionCache` pattern used elsewhere in the app (e.g. Tasks) — don't "fix" it by
adding a cache slot unless asked.

## The goal-change problem (why this isn't a trivial UPDATE)

Naively, "set a new step goal" would just be `UPDATE users SET
default_steps_goal = X`. But the Android app's actual (buggy) behavior is: when you
change your goal, Android does NOT change it immediately. It stores the new goal as
a "pending" value that only takes effect **starting the next day** — because the
goal for *today* should stay whatever it was when today's progress tracking started
(you don't want your progress bar to suddenly reset/rescale mid-day).

Android's real bug: it stores this "pending goal for tomorrow" in local
**SharedPreferences only**. That means if you change your goal on your phone, then
open the app on a second device (or a fresh Flutter client, or reinstall), that
other device has no idea a goal change is pending — the change silently doesn't
apply anywhere except the original device.

The user explicitly asked to **fix this properly** for the Flutter version rather
than copy Android's bug: the pending-goal state must live in the database so every
client (any device, this Flutter app included) sees the same pending goal and the
same "it changes tomorrow" behavior. They chose the "add DB columns now" option
over simpler alternatives (e.g. changing the goal immediately, or keeping it
device-local) when explicitly asked.

The user also specified the exact UX: show a persistent banner/message on the Steps
card whenever a change is pending, e.g. **"Meta pendiente: 4500 pasos (activa
mañana)"** ("Pending goal: 4500 steps, active tomorrow") — modeled on a screenshot
of the existing Android UI for the same message.

### The mechanism (server-authoritative, cron-applied)

1. `users` table gets two new nullable columns: `pending_steps_goal` (int) and
   `pending_steps_goal_date` (date) — the date is "the day this pending goal should
   become the active goal."
2. When the user sets a new goal from any client, that client just writes
   `pending_steps_goal = <new value>`, `pending_steps_goal_date = tomorrow's date`
   to their `users` row. It does **not** touch `default_steps_goal` (today's active
   goal) at all.
3. A `pg_cron` job runs once a day (00:02 UTC — right after the existing
   `fn_close_daily_progress` job at 00:00 that closes out the previous day) and
   calls a new SQL function, `fn_apply_pending_steps_goals()`, which:
   - Finds every user where `pending_steps_goal_date <= CURRENT_DATE` (i.e. "today
     is the day this should apply, or later if the cron somehow missed a day").
   - Copies `pending_steps_goal` into `default_steps_goal`.
   - Clears both `pending_steps_goal` and `pending_steps_goal_date` back to `NULL`.
4. Every client just reads `default_steps_goal` (today's real goal) and
   `pending_steps_goal` (if non-null, show the pending banner) on every fetch —
   there is no special client-side "is it tomorrow yet" logic. The cron job is the
   single source of truth for *when* the switchover happens, so all devices agree.

This mirrors the existing pattern already used in this schema for other nightly
rollovers (`fn_close_daily_progress` at 00:00 UTC, `fn_cleanup_completed_tasks` at
00:15 UTC) — same style, just one more scheduled function.

### Exact SQL migration applied to the live Supabase project

This was already run against the real project via the Supabase Dashboard SQL
Editor (I have no DDL/admin access myself — only the anon/publishable key — so I
handed this to the user to run manually). It has been verified as applied: the
`users` table has both new columns, currently `NULL` for all seeded test users.

```sql
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS pending_steps_goal INTEGER,
    ADD COLUMN IF NOT EXISTS pending_steps_goal_date DATE;

CREATE OR REPLACE FUNCTION fn_apply_pending_steps_goals()
RETURNS void AS $$
BEGIN
    UPDATE users
    SET default_steps_goal      = pending_steps_goal,
        pending_steps_goal      = NULL,
        pending_steps_goal_date = NULL
    WHERE pending_steps_goal IS NOT NULL
      AND pending_steps_goal_date <= CURRENT_DATE;
END;
$$ LANGUAGE plpgsql;

SELECT cron.schedule(
    'apply-pending-steps-goals',
    '2 0 * * *',
    $$SELECT fn_apply_pending_steps_goals();$$
);
```

**Not yet done, flagged as separate future work, do not do unprompted:**
- This migration has not been written up as a versioned schema snapshot
  (`01_schema_v005.sql` or similar) in the `Informacion-Supabase` branch — the
  live DB has it, but the tracked schema history doesn't yet reflect it.
- The real Android app has **not** been updated to write to these new columns —
  it still only uses local SharedPreferences for the pending goal. If Android is
  ever updated to be DB-aware here, this is the mechanism it should adopt too.

## Data model

`lib/core/data/models/app_steps.dart`:

```dart
class AppSteps {
  const AppSteps({
    required this.steps,
    required this.goal,
    required this.pointsEarnedToday,
    this.pendingGoal,
  });

  final int steps;
  final int goal;
  final int pointsEarnedToday;
  final int? pendingGoal;

  bool get hasPendingGoalChange => pendingGoal != null;
}
```

- `steps` — today's step count so far.
- `goal` — today's **active** goal (`users.default_steps_goal`).
- `pointsEarnedToday` — points already computed server-side for today's steps.
- `pendingGoal` — non-null only if a goal change is queued for tomorrow
  (`users.pending_steps_goal`); the UI shows the pending banner whenever this is
  non-null.

## Repository layer

`lib/core/data/repositories/steps_repository.dart` (abstract interface):

```dart
abstract class StepsRepository {
  Future<AppSteps> getSteps();

  /// Takes effect tomorrow — applied server-side by a nightly job, not by
  /// any client, so it's the same for every device.
  Future<void> setGoal(int newGoal);
}
```

`lib/core/data/repositories/supabase_steps_repository.dart` (real implementation):

- `getSteps()`:
  - Reads `users` row for the current auth user: `default_steps_goal`,
    `pending_steps_goal`.
  - Reads the current user's row from `daily_progress`: `steps`, `steps_points`
    (this table has one live row per user per the schema's
    `UNIQUE(user_id)` constraint, so "today's row" is just "the row").
  - If there's no logged-in user at all, returns a hardcoded fallback
    (`AppSteps(steps: 0, goal: 2000, pointsEarnedToday: 0)`) rather than throwing —
    this only matters pre-login/pre-auth-gate, not a real usage path today.
  - If `daily_progress` has no row yet for the user (e.g. brand new account, no
    activity recorded today), `steps`/`pointsEarnedToday` default to `0`.
- `setGoal(newGoal)`: writes `pending_steps_goal = newGoal`,
  `pending_steps_goal_date = <tomorrow's ISO date>` to the user's `users` row.
  Does **not** touch `default_steps_goal`. Uses the shared `isoDate()` helper
  (`lib/core/utils/iso_date.dart`) for consistent `YYYY-MM-DD` formatting — the
  same helper the Tasks feature uses, factored out to avoid duplicating date
  formatting logic across repositories.

Registered in `lib/core/data/repositories/providers.dart`:

```dart
final stepsRepositoryProvider = Provider<StepsRepository>((ref) {
  return SupabaseStepsRepository(ref.read(supabaseClientProvider));
});
```

## State layer (Riverpod)

`lib/features/steps/steps_notifier.dart`:

```dart
class StepsNotifier extends Notifier<AppSteps?> {
  static const _refreshInterval = Duration(minutes: 5);

  @override
  AppSteps? build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return null;
  }

  Future<void> refresh() async {
    state = await ref.read(stepsRepositoryProvider).getSteps();
  }

  Future<void> setGoal(int newGoal) async {
    await ref.read(stepsRepositoryProvider).setGoal(newGoal);
    await refresh();
  }
}

final stepsNotifierProvider = NotifierProvider<StepsNotifier, AppSteps?>(StepsNotifier.new);
```

Design notes:

- State is `AppSteps?` — `null` means "not loaded yet," which is how the UI knows
  to show a spinner instead of the card.
- **Riverpod gotcha (hit before, avoided proactively here):** you cannot
  synchronously assign to `state` from inside `build()` itself — the provider
  isn't finished initializing yet and this throws "Bad state: Tried to read the
  state of an uninitialized provider." That's why the initial load is deferred via
  `Future.microtask(refresh)` rather than calling `refresh()` (or setting `state`)
  directly in `build()`. This same bug was previously hit and fixed the same way
  in `TasksNotifier` — same fix pattern, applied here preemptively.
- Auto-refreshes every 5 minutes via `Timer.periodic`, cancelled on dispose via
  `ref.onDispose(timer.cancel)`. This is the "refreshed periodically" behavior the
  user asked for, since there's no realtime subscription — steps data changes on
  the phone side and this app just polls for updates.
- `setGoal` is fire-and-then-refetch: write the pending goal, then immediately
  `refresh()` so the UI picks up the new `pendingGoal` value (and shows the
  banner) right away, without waiting for the next 5-minute poll.
- No optimistic local update here (unlike Tasks) — since the goal change doesn't
  take visual effect until the pending banner appears (which requires the
  server round-trip to confirm the write succeeded anyway), there's no benefit to
  faking it locally first.

## UI layer

### `lib/screens/habits/habits_screen.dart`

- `ConsumerStatefulWidget`, watches `stepsNotifierProvider`.
- Shows `CircularProgressIndicator` while `steps == null`.
- Once loaded, renders `StepsProgressCard` with all fields wired from `AppSteps`,
  and passes `onConfigureGoal` which opens the goal sheet
  (`showStepsGoalSheet`) and wires its `onSave` callback straight to
  `ref.read(stepsNotifierProvider.notifier).setGoal(newGoal)`.

### `lib/components/cards/steps_progress_card.dart`

Pure presentation, takes `steps`, `goal`, `pointsEarnedToday`, `pendingGoal`
(nullable), `onConfigureGoal` as params. Contains:

- A circular gauge (custom `_GaugePainter`, a 270°-sweep arc from 135° to 405°)
  showing progress toward the goal, with the step count / "of {goal}" / percent
  text centered inside it.
- Milestone chips (50%, 75%, 100%) that fill in as they're reached.
- "Points earned today" and "Next goal" rows.
- **Pending-goal banner**, shown only `if (pendingGoal != null)`:
  ```dart
  Container(
    width: double.infinity,
    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
    decoration: BoxDecoration(
      color: colors.primaryContainer,
      borderRadius: BorderRadius.circular(12),
    ),
    child: Text(
      l10n.stepsPendingGoal(pendingGoal!),
      style: text.bodySmall?.copyWith(
        color: colors.onPrimaryContainer,
        fontWeight: FontWeight.w600,
      ),
    ),
  ),
  ```

**Visual bug fixed in this same file (2026-07-05):** the gauge track and the
"unreached" milestone chips were originally filled with
`colors.surfaceContainerHighest`. This card is built with `Card.filled()`, whose
own default background **is** `colorScheme.surfaceContainerHighest` — so both
elements were rendering in a color identical to the card behind them and were
effectively invisible (the gauge showed as a tiny near-invisible blob instead of a
ring). Fixed by switching both to `colors.surfaceContainerHigh` (one tonal step
down, distinct from the card background but still subtle). Two call sites fixed
in this file: the `_GaugePainter`'s `trackColor`, and the milestone chip's
unreached-state `color:`.

**This same anti-pattern (`Card.filled()` + a same-color `surfaceContainerHighest`
fill drawn on top of it) was found, via grep, to also exist unfixed in five other
component files**, not touched as part of this feature and not yet reported/fixed
as of this writing — flag this to the user before touching them, don't fix
unprompted:
- `lib/components/cards/app_limit_card.dart`
- `lib/components/cards/tech_restriction_card.dart`
- `lib/components/cards/calendar_task_card.dart`
- `lib/components/cards/progress_chart_card.dart`
- `lib/components/cards/weekly_reaction_card.dart`

### `lib/components/forms/steps_goal_sheet.dart`

`showStepsGoalSheet(context, {required currentGoal, required onSave})` — a modal
bottom sheet with a slider (`DottedSliderField`, 1,000–30,000 in steps of 500),
quick-pick chips for common goals (2000/5000/8000/10000/15000), Cancel/Save
buttons. `onSave` is a plain `ValueChanged<int>` callback — the sheet itself has
no Supabase/Riverpod awareness, it's pure UI; the caller (`habits_screen.dart`)
wires `onSave` to the notifier.

## l10n

Added to all three ARB files (`app_es.arb`, `app_en.arb`, `app_de.arb`):

```json
"stepsPendingGoal": "Meta pendiente: {goal} pasos (activa mañana)",
"@stepsPendingGoal": {"placeholders": {"goal": {"type": "int"}}},
```

(English: "Pending goal: {goal} steps (active tomorrow)". German: "Ausstehendes
Ziel: {goal} Schritte (aktiv ab morgen)".)

All other Steps-related l10n keys (`stepsGoalTitle`, `stepsConfigureGoal`,
`stepsMilestones`, `stepsOfGoal`, `stepsPointsEarnedToday`, `stepsNextGoal`, the
goal-sheet keys, etc.) already existed from the design-system phase
(`Test-Diseño-Flutter`) — only `stepsPendingGoal` was newly added for this
feature. Run `flutter pub get` after any ARB edit to regenerate
`app_localizations*.dart`.

## What was explicitly deferred / out of scope (don't add unprompted)

- **No `SessionCache` entry for steps** — confirmed deliberate, see "What this
  feature is" above.
- **No client-side points/streak/step-counting logic** — all of that already
  happens server-side (from the phone's actual pedometer) or in `pg_cron`/trigger
  logic; Flutter only displays results.
- **No app-open "loading screen"/pre-warming** — this is why the Steps card (like
  every other per-screen fetch in this app right now) shows a spinner briefly on
  first navigation to Habits, rather than being instantly populated. A future
  splash/loading-screen phase is meant to pre-fetch this (and Tasks, and profile)
  up front before the user reaches any screen — not built yet, tracked as
  separate future work, not part of this feature.
- **Weekly steps stats/history** — not implemented; only "today" is shown.
- **`01_schema_v005.sql` doc snapshot** and **Android DB-awareness for the pending
  goal** — both explicitly called out above as separate, not-yet-started future
  work.

## Status

Feature confirmed working end-to-end by the user against the real Supabase
project (read + goal-set + pending-goal banner + cron rollover mechanism), including
the gauge visibility fix. **Not yet committed** as of this writing — this doc,
along with the Steps feature files themselves, are part of the current uncommitted
working tree on `Incremento-Flutter-TestFinal` (last commit was Tasks,
`2c815b6`).
