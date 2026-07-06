import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/dropdown_pill.dart';
import '../../components/basic/empty_state.dart';
import '../../components/basic/task_category.dart';
import '../../components/basic/task_dot.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/calendar_task_card.dart';
import '../../components/cards/month_calendar_card.dart';
import '../../components/forms/task_form_sheet.dart';
import '../../components/sheets/task_detail_sheet.dart';
import '../../core/data/models/app_task.dart';
import '../../features/tasks/task_error.dart';
import '../../features/tasks/tasks_notifier.dart';
import '../../l10n/app_localizations.dart';

class CalendarScreen extends ConsumerStatefulWidget {
  const CalendarScreen({super.key});

  @override
  ConsumerState<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends ConsumerState<CalendarScreen> {
  static final _today = DateTime.now();

  late DateTime _displayedMonth = DateTime(_today.year, _today.month);
  DateTime _selectedDay = _today;
  TaskDifficulty? _difficultyFilter;
  TaskCategory? _categoryFilter;

  bool _isSameDay(DateTime a, DateTime b) =>
      a.year == b.year && a.month == b.month && a.day == b.day;

  List<AppTask> _tasksForDay(List<AppTask> tasks, DateTime day) {
    return tasks.where((t) {
      if (!_isSameDay(t.date, day)) return false;
      if (_difficultyFilter != null && t.difficulty != _difficultyFilter) return false;
      if (_categoryFilter != null && t.category != _categoryFilter) return false;
      return true;
    }).toList();
  }

  Map<int, int> _taskCountByDay(List<AppTask> tasks) {
    final map = <int, int>{};
    for (final t in tasks) {
      if (t.date.year != _displayedMonth.year || t.date.month != _displayedMonth.month) continue;
      if (_difficultyFilter != null && t.difficulty != _difficultyFilter) continue;
      if (_categoryFilter != null && t.category != _categoryFilter) continue;
      map[t.date.day] = (map[t.date.day] ?? 0) + 1;
    }
    return map;
  }

  void _openNewTaskForm() {
    showTaskFormSheet(context, forDate: _selectedDay);
  }

  void _openEditTaskForm(AppTask task) {
    showTaskFormSheet(context, forDate: task.date, existing: task);
  }

  void _openDetail(AppTask task) {
    showTaskDetailSheet(
      context,
      task: task,
      onToggle: () {
        ref.read(tasksNotifierProvider.notifier)
            .toggleTask(occurrenceId: task.occurrenceId, isDone: !task.done);
        Navigator.pop(context);
      },
      onEdit: () {
        Navigator.pop(context);
        _openEditTaskForm(task);
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final tasksState = ref.watch(tasksNotifierProvider);
    final tasks = _tasksForDay(tasksState.tasks, _selectedDay);

    ref.listen(tasksNotifierProvider, (previous, next) {
      if (next.errorType != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(taskErrorMessage(next.errorType!, l10n))),
        );
        ref.read(tasksNotifierProvider.notifier).clearError();
      }
    });

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.calendarTitle, showBack: true),
      body: tasksState.isLoading && tasksState.tasks.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
              children: [
                MonthCalendarCard(
                  month: _displayedMonth,
                  selectedDay: _selectedDay,
                  today: _today,
                  taskCountByDay: _taskCountByDay(tasksState.tasks),
                  onPrevMonth: () => setState(
                    () => _displayedMonth = DateTime(_displayedMonth.year, _displayedMonth.month - 1),
                  ),
                  onNextMonth: () => setState(
                    () => _displayedMonth = DateTime(_displayedMonth.year, _displayedMonth.month + 1),
                  ),
                  onDaySelected: (d) => setState(() => _selectedDay = d),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: DropdownPill<TaskDifficulty>(
                        label: l10n.calendarDifficulty,
                        selected: _difficultyFilter,
                        onChanged: (v) => setState(() => _difficultyFilter = v),
                        items: [
                          DropdownPillItem(value: null, label: l10n.calendarAll, icon: Icons.list_rounded),
                          for (final d in TaskDifficulty.values)
                            DropdownPillItem(
                              value: d,
                              label: d.label(context),
                              icon: Icons.circle,
                              color: d.color(colors),
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: DropdownPill<TaskCategory>(
                        label: l10n.calendarCategory,
                        selected: _categoryFilter,
                        onChanged: (v) => setState(() => _categoryFilter = v),
                        items: [
                          DropdownPillItem(value: null, label: l10n.calendarAll, icon: Icons.list_rounded),
                          for (final c in TaskCategory.values)
                            DropdownPillItem(value: c, label: c.label(context), icon: c.icon, color: c.color),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      l10n.calendarTasksForDay(_selectedDay.day),
                      style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                    ),
                    TextButton.icon(
                      onPressed: _openNewTaskForm,
                      icon: const Icon(Icons.add_rounded, size: 18),
                      label: Text(l10n.calendarAdd),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                if (tasks.isEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 32),
                    child: DayPilotEmptyState(
                      icon: Icons.event_busy_outlined,
                      title: l10n.calendarEmptyDay,
                    ),
                  )
                else
                  ...tasks.map((t) => Padding(
                        padding: const EdgeInsets.only(bottom: 8),
                        child: CalendarTaskCard(
                          title: t.title,
                          difficulty: t.difficulty,
                          category: t.category,
                          durationMinutes: t.durationMinutes,
                          completed: t.done,
                          onToggle: () => ref.read(tasksNotifierProvider.notifier)
                              .toggleTask(occurrenceId: t.occurrenceId, isDone: !t.done),
                          onTap: () => _openDetail(t),
                          onEdit: () => _openEditTaskForm(t),
                          onDelete: () => ref.read(tasksNotifierProvider.notifier).deleteTask(t.id),
                        ),
                      )),
              ],
            ),
    );
  }
}
