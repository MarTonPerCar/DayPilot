import 'package:flutter/material.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../basic/sheet_handle.dart';
import '../basic/task_category.dart';
import '../basic/task_dot.dart';
import '../cards/calendar_task_card.dart';

Future<void> showTaskDetailSheet(
  BuildContext context, {
  required AppTask task,
  required VoidCallback onToggle,
  required VoidCallback onEdit,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => TaskDetailSheet(task: task, onToggle: onToggle, onEdit: onEdit),
  );
}

class TaskDetailSheet extends StatelessWidget {
  final AppTask task;
  final VoidCallback onToggle;
  final VoidCallback onEdit;

  const TaskDetailSheet({
    super.key,
    required this.task,
    required this.onToggle,
    required this.onEdit,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const DayPilotSheetHandle(),
            const SizedBox(height: 20),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    task.title,
                    style: text.headlineSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                      decoration: task.done ? TextDecoration.lineThrough : null,
                      color: task.done ? colors.onSurfaceVariant : colors.onSurface,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                GestureDetector(
                  onTap: onToggle,
                  child: CircleAvatar(
                    radius: 16,
                    backgroundColor: task.done ? colors.primary : colors.surfaceContainerHighest,
                    child: Icon(
                      Icons.check_rounded,
                      size: 18,
                      color: task.done ? colors.onPrimary : colors.onSurfaceVariant,
                    ),
                  ),
                ),
              ],
            ),
            if (task.description != null) ...[
              const SizedBox(height: 10),
              Text(
                task.description!,
                style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
              ),
            ],
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                DifficultyChip(difficulty: task.difficulty),
                TaskCategoryChip(category: task.category),
                DurationChip(minutes: task.durationMinutes),
              ],
            ),
            if (task.reminder || task.recurring) ...[
              const SizedBox(height: 12),
              Wrap(
                spacing: 16,
                runSpacing: 8,
                children: [
                  if (task.reminder)
                    _InfoRow(icon: Icons.notifications_active_rounded, label: l10n.taskDetailReminderActive),
                  if (task.recurring)
                    _InfoRow(
                      icon: Icons.repeat_rounded,
                      label: task.repeatEveryDays == 1
                          ? l10n.taskDetailRepeatsDaily
                          : l10n.taskDetailRepeatsEveryDays(task.repeatEveryDays),
                    ),
                ],
              ),
            ],
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: onToggle,
                    child: Text(task.done ? l10n.taskDetailMarkPending : l10n.taskDetailMarkDone),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton.icon(
                    onPressed: onEdit,
                    icon: const Icon(Icons.edit_rounded, size: 18),
                    label: Text(l10n.taskEditTitle),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  const _InfoRow({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 16, color: colors.primary),
        const SizedBox(width: 6),
        Text(label, style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant)),
      ],
    );
  }
}
