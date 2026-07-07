import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'calendar_day_card.dart';

class MonthCalendarCard extends StatelessWidget {
  final DateTime month;
  final DateTime selectedDay;
  final DateTime today;
  final Map<int, int> taskCountByDay;
  final VoidCallback onPrevMonth;
  final VoidCallback onNextMonth;
  final ValueChanged<DateTime> onDaySelected;

  const MonthCalendarCard({
    super.key,
    required this.month,
    required this.selectedDay,
    required this.today,
    this.taskCountByDay = const {},
    required this.onPrevMonth,
    required this.onNextMonth,
    required this.onDaySelected,
  });

  static const _weekdayLabels = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];
  static const _cellHeight = 44.0;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    final daysInMonth = DateTime(month.year, month.month + 1, 0).day;
    final leadingBlanks = DateTime(month.year, month.month, 1).weekday - 1;
    final totalCells = ((leadingBlanks + daysInMonth) / 7).ceil() * 7;

    final isCurrentMonth = month.year == today.year && month.month == today.month;
    final isSelectedMonth = month.year == selectedDay.year && month.month == selectedDay.month;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  icon: const Icon(Icons.chevron_left_rounded),
                  onPressed: onPrevMonth,
                ),
                Text(
                  DateFormat.yMMMM(Localizations.localeOf(context).languageCode).format(month),
                  style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                ),
                IconButton(
                  icon: const Icon(Icons.chevron_right_rounded),
                  onPressed: onNextMonth,
                ),
              ],
            ),
            const SizedBox(height: 4),

            Row(
              children: _weekdayLabels.map((l) {
                return Expanded(
                  child: Center(
                    child: Text(
                      l,
                      style: text.labelSmall?.copyWith(
                        color: colors.onSurfaceVariant,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 4),

            for (int row = 0; row < totalCells ~/ 7; row++)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 1),
                child: Row(
                  children: List.generate(7, (col) {
                    final cellIndex = row * 7 + col;
                    final day = cellIndex - leadingBlanks + 1;
                    final isValidDay = day >= 1 && day <= daysInMonth;

                    return Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 2),
                        child: SizedBox(
                          height: _cellHeight,
                          child: isValidDay
                              ? CalendarDayCard(
                                  day: day,
                                  taskCount: taskCountByDay[day] ?? 0,
                                  isSelected: isSelectedMonth && day == selectedDay.day,
                                  isToday: isCurrentMonth && day == today.day,
                                  onTap: () => onDaySelected(DateTime(month.year, month.month, day)),
                                )
                              : const SizedBox.shrink(),
                        ),
                      ),
                    );
                  }),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
