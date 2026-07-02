import 'package:flutter/material.dart';

class CalendarDayCard extends StatelessWidget {
  final int day;
  final int taskCount;
  final bool isSelected;
  final bool isToday;
  final bool isCurrentMonth;
  final VoidCallback? onTap;

  const CalendarDayCard({
    super.key,
    required this.day,
    this.taskCount = 0,
    this.isSelected = false,
    this.isToday = false,
    this.isCurrentMonth = true,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    Color bg;
    Color fg;
    if (isSelected) {
      bg = colors.primary;
      fg = colors.onPrimary;
    } else if (isToday) {
      bg = colors.primaryContainer;
      fg = colors.onPrimaryContainer;
    } else {
      bg = Colors.transparent;
      fg = isCurrentMonth ? colors.onSurface : colors.onSurfaceVariant;
    }

    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              '$day',
              style: text.bodyMedium?.copyWith(
                color: fg,
                fontWeight: (isSelected || isToday) ? FontWeight.w700 : FontWeight.w400,
              ),
            ),
            const SizedBox(height: 3),
            _TaskDots(count: taskCount, selected: isSelected, colors: colors),
          ],
        ),
      ),
    );
  }
}

class _TaskDots extends StatelessWidget {
  final int count;
  final bool selected;
  final ColorScheme colors;

  const _TaskDots({
    required this.count,
    required this.selected,
    required this.colors,
  });

  @override
  Widget build(BuildContext context) {
    if (count == 0) return const SizedBox(height: 5);
    final dotColor = selected ? colors.onPrimary : colors.primary;
    final dots = count.clamp(0, 3);
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(dots, (_) {
        return Container(
          width: 4,
          height: 4,
          margin: const EdgeInsets.symmetric(horizontal: 1),
          decoration: BoxDecoration(color: dotColor, shape: BoxShape.circle),
        );
      }),
    );
  }
}

class CalendarWeekRow extends StatefulWidget {
  const CalendarWeekRow({super.key});

  @override
  State<CalendarWeekRow> createState() => _CalendarWeekRowState();
}

class _CalendarWeekRowState extends State<CalendarWeekRow> {
  int _selected = 2;

  static const _days = [
    (day: 30, tasks: 1),
    (day: 1, tasks: 3),
    (day: 2, tasks: 0),
    (day: 3, tasks: 2),
    (day: 4, tasks: 1),
    (day: 5, tasks: 0),
    (day: 6, tasks: 4),
  ];

  @override
  Widget build(BuildContext context) {
    return Row(
      children: List.generate(_days.length, (i) {
        final d = _days[i];
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 2),
            child: AspectRatio(
              aspectRatio: 0.7,
              child: CalendarDayCard(
                day: d.day,
                taskCount: d.tasks,
                isSelected: _selected == i,
                isToday: i == 3,
                isCurrentMonth: i != 0,
                onTap: () => setState(() => _selected = i),
              ),
            ),
          ),
        );
      }),
    );
  }
}
