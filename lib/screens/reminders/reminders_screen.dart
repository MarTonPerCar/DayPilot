import 'package:flutter/material.dart';
import '../../components/basic/empty_state.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/reminder_card.dart';
import '../../components/forms/reminder_form_sheet.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class RemindersScreen extends StatefulWidget {
  const RemindersScreen({super.key});

  @override
  State<RemindersScreen> createState() => _RemindersScreenState();
}

class _RemindersScreenState extends State<RemindersScreen> {
  final List<AppReminder> _reminders = AppData.newReminderList();

  void _openForm() {
    showReminderFormSheet(
      context,
      onSave: (_) {},
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.remindersTitle, showBack: true),
      body: _reminders.isEmpty
          ? DayPilotEmptyState(
              icon: Icons.add_rounded,
              title: l10n.remindersEmptyState,
            )
          : ListView.separated(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
              itemCount: _reminders.length,
              separatorBuilder: (_, _) => const SizedBox(height: 10),
              itemBuilder: (context, i) {
                final r = _reminders[i];
                return ReminderCard(
                  title: r.title,
                  dateTime: r.dateTime,
                  enabled: r.enabled,
                  onToggle: (v) => setState(() => r.enabled = v),
                  onDelete: () {},
                );
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _openForm,
        child: const Icon(Icons.add_rounded),
      ),
    );
  }
}
