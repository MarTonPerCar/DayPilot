import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/timer_preset_card.dart';
import '../../components/forms/timer_config_sheet.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import 'timer_running_screen.dart';

class TimersScreen extends StatelessWidget {
  const TimersScreen({super.key});

  void _start(BuildContext context, TimerPreset preset) {
    switch (preset.mode) {
      case TimerMode.pomodoro:
        showPomodoroConfigSheet(
          context,
          color: preset.color,
          workMinutes: preset.minutes,
          restMinutes: preset.restMinutes,
          defaultSessions: preset.sessions,
          onStart: (sessions) => Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => TimerRunningScreen(
                title: preset.name,
                color: preset.color,
                isPomodoro: true,
                workMinutes: preset.minutes,
                restMinutes: preset.restMinutes,
                totalSessions: sessions,
              ),
            ),
          ),
        );
        break;
      case TimerMode.custom:
        showCustomTimerSheet(
          context,
          color: preset.color,
          onStart: (minutes) => Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => TimerRunningScreen(
                title: preset.name,
                color: preset.color,
                workMinutes: minutes,
              ),
            ),
          ),
        );
        break;
      case TimerMode.fixed:
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => TimerRunningScreen(
              title: preset.name,
              color: preset.color,
              workMinutes: preset.minutes,
            ),
          ),
        );
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.habitsTimersTitle, showBack: true),
      body: ListView.separated(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        itemCount: AppData.timerPresets.length,
        separatorBuilder: (_, _) => const SizedBox(height: 10),
        itemBuilder: (context, i) {
          final preset = AppData.timerPresets[i];
          return TimerPresetCard(preset: preset, onPlay: () => _start(context, preset));
        },
      ),
    );
  }
}
