import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/habit_card.dart';
import '../../components/cards/steps_progress_card.dart';
import '../../components/forms/steps_goal_sheet.dart';
import '../../data/platform_capabilities.dart';
import '../../features/steps/steps_notifier.dart';
import '../../l10n/app_localizations.dart';
import '../reminders/reminders_screen.dart';
import '../techhealth/tech_health_screen.dart';
import '../techhealth/tech_health_unavailable_screen.dart';
import '../timers/timers_screen.dart';

class HabitsScreen extends ConsumerStatefulWidget {
  const HabitsScreen({super.key});

  @override
  ConsumerState<HabitsScreen> createState() => _HabitsScreenState();
}

class _HabitsScreenState extends ConsumerState<HabitsScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(stepsNotifierProvider.notifier).refresh());
  }

  void _openTechHealth() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => PlatformCapabilities.supportsDeviceFeatures
            ? const TechHealthScreen()
            : const TechHealthUnavailableScreen(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final steps = ref.watch(stepsNotifierProvider);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.commonHabits, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 40),
        children: [
          Text(
            l10n.commonSteps,
            style: text.labelLarge?.copyWith(color: colors.onSurfaceVariant, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          if (steps == null)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 24),
              child: Center(child: CircularProgressIndicator()),
            )
          else
            StepsProgressCard(
              steps: steps.steps,
              goal: steps.goal,
              pointsEarnedToday: steps.pointsEarnedToday,
              pendingGoal: steps.pendingGoal,
              onConfigureGoal: () => showStepsGoalSheet(
                context,
                currentGoal: steps.goal,
                onSave: (newGoal) =>
                    ref.read(stepsNotifierProvider.notifier).setGoal(newGoal),
              ),
            ),
          const SizedBox(height: 24),
          Text(
            l10n.habitsOtherHabits,
            style: text.labelLarge?.copyWith(color: colors.onSurfaceVariant, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.timer_rounded,
            title: l10n.habitsTimersTitle,
            subtitle: l10n.habitsTimersSubtitle,
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const TimersScreen())),
          ),
          const SizedBox(height: 10),
          HabitCard(
            icon: Icons.smartphone_rounded,
            title: l10n.techHealthTitle,
            subtitle: l10n.habitsTechHealthSubtitle,
            onTap: _openTechHealth,
          ),
          const SizedBox(height: 10),
          HabitCard(
            icon: Icons.notifications_rounded,
            title: l10n.remindersTitle,
            subtitle: l10n.habitsRemindersSubtitle,
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RemindersScreen())),
          ),
        ],
      ),
    );
  }
}
