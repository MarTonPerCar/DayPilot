import 'package:flutter/material.dart';
import '../../components/basic/dropdown_pill.dart';
import '../../components/basic/empty_state.dart';
import '../../components/basic/task_category.dart';
import '../../components/basic/task_dot.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/calendar_task_card.dart';
import '../../components/cards/month_calendar_card.dart';
import '../../components/forms/task_form_sheet.dart';
import '../../components/sheets/task_detail_sheet.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> {
  static final _today = AppData.today;

  late DateTime _displayedMonth = DateTime(_today.year, _today.month);
  DateTime _selectedDay = _today;
  final List<AppTask> _tasks = AppData.newTaskList();
  TaskDifficulty? _difficultyFilter;
  TaskCategory? _categoryFilter;

  bool _isSameDay(DateTime a, DateTime b) =>
      a.year == b.year && a.month == b.month && a.day == b.day;

  List<AppTask> get _tasksForSelectedDay {
    return _tasks.where((t) {
      if (!_isSameDay(t.date, _selectedDay)) return false;
      if (_difficultyFilter != null && t.difficulty != _difficultyFilter) return false;
      if (_categoryFilter != null && t.category != _categoryFilter) return false;
      return true;
    }).toList();
  }

  Map<int, int> get _taskCountByDayInMonth {
    final map = <int, int>{};
    for (final t in _tasks) {
      if (t.date.year != _displayedMonth.year || t.date.month != _displayedMonth.month) continue;
      if (_difficultyFilter != null && t.difficulty != _difficultyFilter) continue;
      if (_categoryFilter != null && t.category != _categoryFilter) continue;
      map[t.date.day] = (map[t.date.day] ?? 0) + 1;
    }
    return map;
  }

  void _openNewTaskForm() {
    showTaskFormSheet(
      context,
      forDate: _selectedDay,
      onSave: (task) => setState(() => _tasks.add(task)),
    );
  }

  void _openEditTaskForm(AppTask task) {
    showTaskFormSheet(
      context,
      forDate: task.date,
      existing: task,
      onSave: (updated) {
        setState(() {
          final idx = _tasks.indexWhere((t) => t.id == updated.id);
          if (idx != -1) _tasks[idx] = updated;
        });
      },
    );
  }

  void _openDetail(AppTask task) {
    showTaskDetailSheet(
      context,
      task: task,
      onToggle: () {
        setState(() => task.done = !task.done);
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
    final tasks = _tasksForSelectedDay;

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.calendarTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
        children: [
          MonthCalendarCard(
            month: _displayedMonth,
            selectedDay: _selectedDay,
            today: _today,
            taskCountByDay: _taskCountByDayInMonth,
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
                    onToggle: () => setState(() => t.done = !t.done),
                    onTap: () => _openDetail(t),
                    onEdit: () => _openEditTaskForm(t),
                    onDelete: () => setState(() => _tasks.removeWhere((x) => x.id == t.id)),
                  ),
                )),
        ],
      ),
    );
  }
}
