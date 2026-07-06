import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/data/models/app_task.dart';
import '../../features/tasks/tasks_notifier.dart';
import '../../l10n/app_localizations.dart';
import '../basic/sheet_handle.dart';
import '../basic/task_category.dart';
import '../basic/task_dot.dart';
import '../basic/text_field.dart';
import 'category_chip_group.dart';
import 'collapsible_section.dart';
import 'difficulty_field.dart';
import 'stepper_field.dart';
import 'switch_tile.dart';

Future<void> showTaskFormSheet(
  BuildContext context, {
  required DateTime forDate,
  AppTask? existing,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => TaskFormSheet(forDate: forDate, existing: existing),
  );
}

class TaskFormSheet extends ConsumerStatefulWidget {
  final DateTime forDate;
  final AppTask? existing;

  const TaskFormSheet({super.key, required this.forDate, this.existing});

  @override
  ConsumerState<TaskFormSheet> createState() => _TaskFormSheetState();
}

class _TaskFormSheetState extends ConsumerState<TaskFormSheet> {
  late final _titleCtrl = TextEditingController(text: widget.existing?.title ?? '');
  late final _descCtrl = TextEditingController(text: widget.existing?.description ?? '');
  late TaskCategory _category = widget.existing?.category ?? TaskCategory.personal;
  late TaskDifficulty _difficulty = widget.existing?.difficulty ?? TaskDifficulty.easy;
  late int _duration = widget.existing?.durationMinutes ?? 30;
  late bool _reminder = widget.existing?.reminder ?? false;
  late bool _recurring = widget.existing?.recurring ?? false;
  late int _repeatEveryDays = 1;
  bool _hasTitleError = false;

  bool get _isEditing => widget.existing != null;

  void _submit() {
    final title = _titleCtrl.text.trim();
    if (title.isEmpty) {
      setState(() => _hasTitleError = true);
      return;
    }
    final description = _descCtrl.text.trim();
    final notifier = ref.read(tasksNotifierProvider.notifier);
    Navigator.pop(context);
    if (_isEditing) {
      notifier.updateTask(
        id: widget.existing!.id,
        title: title,
        description: description,
        category: _category,
        difficulty: _difficulty,
        durationMinutes: _duration,
      );
    } else {
      notifier.addTask(NewTaskData(
        date: widget.forDate,
        title: title,
        description: description,
        category: _category,
        difficulty: _difficulty,
        durationMinutes: _duration,
        reminder: _reminder,
        recurring: _recurring,
        recurrenceDays: _repeatEveryDays,
      ));
    }
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: DraggableScrollableSheet(
        initialChildSize: 0.85,
        minChildSize: 0.5,
        maxChildSize: 0.95,
        expand: false,
        builder: (context, scrollController) {
          return SingleChildScrollView(
            controller: scrollController,
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const DayPilotSheetHandle(),
                const SizedBox(height: 16),
                Text(
                  _isEditing ? l10n.taskEditTitle : l10n.taskNewTitle,
                  style: text.headlineSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 16),

                DayPilotCollapsibleSection(
                  icon: Icons.edit_outlined,
                  title: l10n.taskInfoSection,
                  children: [
                    const SizedBox(height: 12),
                    DayPilotTextField(
                      controller: _titleCtrl,
                      label: l10n.taskTitleLabel,
                      errorText: _hasTitleError ? l10n.taskTitleRequired : null,
                      onChanged: (_) {
                        if (_hasTitleError) setState(() => _hasTitleError = false);
                      },
                    ),
                    const SizedBox(height: 12),
                    DayPilotTextField(
                      controller: _descCtrl,
                      label: l10n.taskDescriptionLabel,
                      maxLines: 3,
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                DayPilotCollapsibleSection(
                  icon: Icons.list_alt_rounded,
                  title: l10n.taskDetailsSection,
                  children: [
                    const SizedBox(height: 8),
                    CategoryChipGroup(
                      label: l10n.calendarCategory,
                      selected: _category,
                      onChanged: (c) => setState(() => _category = c),
                    ),
                    const SizedBox(height: 16),
                    DifficultyField(
                      label: l10n.calendarDifficulty,
                      value: _difficulty,
                      onChanged: (d) => setState(() => _difficulty = d),
                    ),
                    const SizedBox(height: 16),
                    DayPilotStepper(
                      label: l10n.taskDurationEstimate,
                      value: _duration,
                      min: 5,
                      max: 240,
                      step: 5,
                      suffix: l10n.taskMinSuffix,
                      onChanged: (v) => setState(() => _duration = v),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                DayPilotCollapsibleSection(
                  icon: Icons.notifications_outlined,
                  title: l10n.taskReminderSection,
                  initiallyExpanded: false,
                  children: [
                    const SizedBox(height: 4),
                    DayPilotSwitchTile(
                      label: l10n.taskActivateReminder,
                      subtitle: l10n.taskReminderSubtitle,
                      value: _reminder,
                      onChanged: (v) => setState(() => _reminder = v),
                    ),
                    DayPilotSwitchTile(
                      label: l10n.taskRecurring,
                      subtitle: l10n.taskRecurringSubtitle,
                      value: _recurring,
                      onChanged: (v) => setState(() => _recurring = v),
                    ),
                    if (_recurring) ...[
                      const SizedBox(height: 12),
                      DayPilotStepper(
                        label: l10n.taskRepeatEvery,
                        value: _repeatEveryDays,
                        min: 1,
                        max: 30,
                        step: 1,
                        suffix: l10n.taskDaysSuffix,
                        onChanged: (v) => setState(() => _repeatEveryDays = v),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 24),

                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => Navigator.pop(context),
                        child: Text(l10n.commonCancel),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: FilledButton(
                        onPressed: _submit,
                        child: Text(_isEditing ? l10n.commonSaveChanges : l10n.taskCreateButton),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
