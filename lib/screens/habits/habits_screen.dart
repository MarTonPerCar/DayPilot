import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/habit_card.dart';
import '../../components/cards/steps_progress_card.dart';
import '../../components/forms/steps_goal_sheet.dart';
import '../../data/app_data.dart';
import '../../data/platform_capabilities.dart';
import '../../l10n/app_localizations.dart';
import '../reminders/reminders_screen.dart';
import '../techhealth/tech_health_screen.dart';
import '../techhealth/tech_health_unavailable_screen.dart';
import '../timers/timers_screen.dart';

class HabitsScreen extends StatefulWidget {
  const HabitsScreen({super.key});

  @override
  State<HabitsScreen> createState() => _HabitsScreenState();
}

class _HabitsScreenState extends State<HabitsScreen> {
  int _stepsGoal = AppData.stepsGoal;

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
          StepsProgressCard(
            steps: AppData.stepsToday,
            goal: _stepsGoal,
            pointsEarnedToday: AppData.pointsTodayFromSteps,
            onConfigureGoal: () => showStepsGoalSheet(
              context,
              currentGoal: _stepsGoal,
              onSave: (g) => setState(() => _stepsGoal = g),
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
