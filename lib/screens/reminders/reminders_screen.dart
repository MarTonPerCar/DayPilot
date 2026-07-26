import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/empty_state.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/reminder_card.dart';
import '../../components/forms/reminder_form_sheet.dart';
import '../../features/reminders/reminders_notifier.dart';
import '../../l10n/app_localizations.dart';

class RemindersScreen extends ConsumerWidget {
  const RemindersScreen({super.key});

  void _openForm(BuildContext context, WidgetRef ref) {
    showReminderFormSheet(
      context,
      onSave: (reminder) => ref.read(remindersNotifierProvider.notifier).addReminder(reminder),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final reminders = ref.watch(remindersNotifierProvider);
    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.remindersTitle, showBack: true),
      body: reminders.isEmpty
          ? DayPilotEmptyState(
              icon: Icons.add_rounded,
              title: l10n.remindersEmptyState,
            )
          : ListView.separated(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
              itemCount: reminders.length,
              separatorBuilder: (_, _) => const SizedBox(height: 10),
              itemBuilder: (context, i) {
                final r = reminders[i];
                return ReminderCard(
                  title: r.title,
                  dateTime: r.dateTime,
                  enabled: r.enabled,
                  onToggle: (v) =>
                      ref.read(remindersNotifierProvider.notifier).toggleReminder(r.id, v),
                  onDelete: () =>
                      ref.read(remindersNotifierProvider.notifier).deleteReminder(r.id),
                );
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _openForm(context, ref),
        child: const Icon(Icons.add_rounded),
      ),
    );
  }
}
