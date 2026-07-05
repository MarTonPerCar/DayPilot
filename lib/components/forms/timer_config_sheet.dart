import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/quick_pick_chip.dart';
import '../basic/sheet_handle.dart';
import 'dotted_slider.dart';

Future<void> showCustomTimerSheet(
  BuildContext context, {
  required Color color,
  required ValueChanged<int> onStart,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => _CustomTimerSheet(color: color, onStart: onStart),
  );
}

class _CustomTimerSheet extends StatefulWidget {
  final Color color;
  final ValueChanged<int> onStart;
  const _CustomTimerSheet({required this.color, required this.onStart});

  @override
  State<_CustomTimerSheet> createState() => _CustomTimerSheetState();
}

class _CustomTimerSheetState extends State<_CustomTimerSheet> {
  static const _quick = [15, 30, 45, 60, 90];
  int _minutes = 30;

  String _fmt(int m) => m >= 60 ? '${(m / 60).toStringAsFixed(m % 60 == 0 ? 0 : 1)}h' : '${m}m';

  @override
  Widget build(BuildContext context) {
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
            Text(l10n.timerCustomTitle, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 24),
            Center(
              child: Text(
                l10n.timerMinutesValue(_minutes),
                style: text.headlineMedium?.copyWith(color: widget.color, fontWeight: FontWeight.w700),
              ),
            ),
            const SizedBox(height: 16),
            DottedSliderField(
              value: _minutes.toDouble(),
              min: 5,
              max: 180,
              leftLabel: l10n.timerMinValue(5),
              rightLabel: '3 h',
              activeColor: widget.color,
              onChanged: (v) => setState(() => _minutes = (v / 5).round() * 5),
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _quick.map((m) {
                return QuickPickChip(
                  label: _fmt(m),
                  selected: _minutes == m,
                  color: widget.color,
                  onTap: () => setState(() => _minutes = m),
                );
              }).toList(),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                style: FilledButton.styleFrom(backgroundColor: widget.color),
                onPressed: () {
                  Navigator.pop(context);
                  widget.onStart(_minutes);
                },
                child: Text(l10n.commonStart),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

Future<void> showPomodoroConfigSheet(
  BuildContext context, {
  required Color color,
  required int workMinutes,
  required int restMinutes,
  required int defaultSessions,
  required ValueChanged<int> onStart,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => _PomodoroConfigSheet(
      color: color,
      workMinutes: workMinutes,
      restMinutes: restMinutes,
      defaultSessions: defaultSessions,
      onStart: onStart,
    ),
  );
}

class _PomodoroConfigSheet extends StatefulWidget {
  final Color color;
  final int workMinutes;
  final int restMinutes;
  final int defaultSessions;
  final ValueChanged<int> onStart;

  const _PomodoroConfigSheet({
    required this.color,
    required this.workMinutes,
    required this.restMinutes,
    required this.defaultSessions,
    required this.onStart,
  });

  @override
  State<_PomodoroConfigSheet> createState() => _PomodoroConfigSheetState();
}

class _PomodoroConfigSheetState extends State<_PomodoroConfigSheet> {
  late int _sessions = widget.defaultSessions;

  int get _totalMinutes => (widget.workMinutes + widget.restMinutes) * _sessions;

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
            Text(l10n.timerPomodoroConfigTitle, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.symmetric(vertical: 16),
              decoration: BoxDecoration(
                color: colors.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: _SummaryStat(
                      dotColor: widget.color,
                      value: l10n.timerMinValue(widget.workMinutes),
                      label: l10n.timerWork,
                      valueColor: widget.color,
                    ),
                  ),
                  Expanded(
                    child: _SummaryStat(
                      dotColor: colors.primary,
                      value: l10n.timerMinValue(widget.restMinutes),
                      label: l10n.timerRest,
                      valueColor: colors.primary,
                    ),
                  ),
                  Expanded(
                    child: _SummaryStat(
                      icon: Icons.timer_outlined,
                      value: l10n.timerMinValue(_totalMinutes),
                      label: l10n.timerTotal,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            Text(l10n.timerSessionsCount, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
            const SizedBox(height: 8),
            Center(
              child: Text(
                l10n.timerSessionsValue(_sessions),
                style: text.headlineSmall?.copyWith(color: widget.color, fontWeight: FontWeight.w700),
              ),
            ),
            const SizedBox(height: 12),
            DottedSliderField(
              value: _sessions.toDouble(),
              min: 1,
              max: 8,
              activeColor: widget.color,
              leftLabel: l10n.timerOneSession(widget.workMinutes + widget.restMinutes),
              rightLabel: l10n.timerEightSessions(
                ((widget.workMinutes + widget.restMinutes) * 8 / 60).toStringAsFixed(0),
              ),
              onChanged: (v) => setState(() => _sessions = v.round()),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                style: FilledButton.styleFrom(backgroundColor: widget.color),
                onPressed: () {
                  Navigator.pop(context);
                  widget.onStart(_sessions);
                },
                child: Text(l10n.timerStartPomodoro),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SummaryStat extends StatelessWidget {
  final Color? dotColor;
  final IconData? icon;
  final String value;
  final String label;
  final Color? valueColor;

  const _SummaryStat({
    this.dotColor,
    this.icon,
    required this.value,
    required this.label,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      children: [
        if (dotColor != null)
          Container(width: 14, height: 14, decoration: BoxDecoration(color: dotColor, shape: BoxShape.circle))
        else if (icon != null)
          Icon(icon, size: 16, color: colors.onSurfaceVariant),
        const SizedBox(height: 6),
        Text(value, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700, color: valueColor)),
        Text(label, style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
      ],
    );
  }
}
