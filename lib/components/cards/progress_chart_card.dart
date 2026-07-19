import 'dart:math';
import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

enum ProgressMetric { points, steps, tasks }

class ProgressChartCard extends StatefulWidget {
  final List<double> pointsHistory;
  final List<double> stepsHistory;
  final List<double> tasksHistory;
  final List<int> dayLabels;

  const ProgressChartCard({
    super.key,
    required this.pointsHistory,
    required this.stepsHistory,
    required this.tasksHistory,
    required this.dayLabels,
  });

  @override
  State<ProgressChartCard> createState() => _ProgressChartCardState();
}

class _ProgressChartCardState extends State<ProgressChartCard> {
  ProgressMetric _metric = ProgressMetric.points;

  static const _daysStep = 5;

  List<double> get _data => switch (_metric) {
        ProgressMetric.points => widget.pointsHistory,
        ProgressMetric.steps => widget.stepsHistory,
        ProgressMetric.tasks => widget.tasksHistory,
      };

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final data = _data;

    final (icon, label, lineColor) = switch (_metric) {
      ProgressMetric.points => (Icons.star_rounded, l10n.commonPoints, const Color(0xFFFFD700)),
      ProgressMetric.steps => (Icons.directions_walk_rounded, l10n.commonSteps, colors.tertiary),
      ProgressMetric.tasks => (Icons.task_alt_rounded, l10n.commonTasks, colors.primary),
    };

    final total = data.fold<double>(0, (a, b) => a + b);
    final avg = data.isEmpty ? 0.0 : total / data.length;
    final best = data.isEmpty ? 0.0 : data.reduce(max);

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _MetricSelector(
              selected: _metric,
              onSelected: (m) => setState(() => _metric = m),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Icon(icon, size: 18, color: lineColor),
                const SizedBox(width: 6),
                Text(label, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
              ],
            ),
            const SizedBox(height: 8),
            SizedBox(
              height: 200,
              width: double.infinity,
              child: CustomPaint(
                size: Size.infinite,
                painter: _LineChartPainter(
                  data: data,
                  dayLabels: widget.dayLabels,
                  daysStep: _daysStep,
                  lineColor: lineColor,
                  gridColor: colors.outlineVariant,
                  labelColor: colors.onSurfaceVariant,
                  textStyle: text.labelSmall,
                ),
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: _StatLabel(value: _fmt(total), label: l10n.commonTotal)),
                Expanded(child: _StatLabel(value: _fmt(avg), label: l10n.commonAverage)),
                Expanded(child: _StatLabel(value: _fmt(best), label: l10n.commonBest)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  static String _fmt(double v) {
    if (v >= 1000) return '${(v / 1000).toStringAsFixed(1)}k';
    if (v == v.roundToDouble()) return v.toInt().toString();
    return v.toStringAsFixed(1);
  }
}

class _MetricSelector extends StatelessWidget {
  final ProgressMetric selected;
  final ValueChanged<ProgressMetric> onSelected;

  const _MetricSelector({required this.selected, required this.onSelected});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    final options = [
      (metric: ProgressMetric.points, label: l10n.commonPoints),
      (metric: ProgressMetric.steps, label: l10n.commonSteps),
      (metric: ProgressMetric.tasks, label: l10n.commonTasks),
    ];

    return Row(
      children: options.map((o) {
        final isSelected = o.metric == selected;
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 3),
            child: GestureDetector(
              onTap: () => onSelected(o.metric),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 150),
                padding: const EdgeInsets.symmetric(vertical: 10),
                decoration: BoxDecoration(
                  color: isSelected ? colors.primaryContainer : colors.surfaceContainerHigh,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (isSelected) ...[
                      Icon(Icons.check_rounded, size: 16, color: colors.onPrimaryContainer),
                      const SizedBox(width: 4),
                    ],
                    Text(
                      o.label,
                      style: text.labelLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: isSelected ? colors.onPrimaryContainer : colors.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

class _StatLabel extends StatelessWidget {
  final String value;
  final String label;

  const _StatLabel({required this.value, required this.label});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      children: [
        Text(value, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
        Text(label, style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
      ],
    );
  }
}

class _LineChartPainter extends CustomPainter {
  final List<double> data;
  final List<int> dayLabels;
  final int daysStep;
  final Color lineColor;
  final Color gridColor;
  final Color labelColor;
  final TextStyle? textStyle;

  _LineChartPainter({
    required this.data,
    required this.dayLabels,
    required this.daysStep,
    required this.lineColor,
    required this.gridColor,
    required this.labelColor,
    required this.textStyle,
  });

  static const _leftAxisWidth = 34.0;
  static const _bottomAxisHeight = 20.0;
  static const _topPadding = 26.0;

  @override
  void paint(Canvas canvas, Size size) {
    final chartRect = Rect.fromLTWH(
      _leftAxisWidth,
      _topPadding,
      size.width - _leftAxisWidth,
      size.height - _topPadding - _bottomAxisHeight,
    );

    final maxValue = data.isEmpty ? 0.0 : data.reduce(max);
    final niceMax = _niceCeil(maxValue);

    for (int i = 0; i <= 4; i++) {
      final v = niceMax * i / 4;
      final y = chartRect.bottom - chartRect.height * (i / 4);
      canvas.drawLine(
        Offset(chartRect.left, y),
        Offset(chartRect.right, y),
        Paint()
          ..color = gridColor.withAlpha(i == 0 ? 255 : 110)
          ..strokeWidth = 1,
      );
      final tp = _text(_fmtAxis(v), labelColor);
      tp.paint(canvas, Offset(0, y - tp.height / 2));
    }

    if (data.isEmpty) return;

    final todayIndex = data.length - 1;

    final points = <Offset>[
      for (int i = 0; i < data.length; i++)
        Offset(
          chartRect.left + chartRect.width * ((i + 0.5) / data.length),
          chartRect.bottom - chartRect.height * (niceMax == 0 ? 0 : (data[i] / niceMax).clamp(0.0, 1.0)),
        ),
    ];

    final areaPath = Path()..moveTo(points.first.dx, chartRect.bottom);
    for (final p in points) {
      areaPath.lineTo(p.dx, p.dy);
    }
    areaPath.lineTo(points.last.dx, chartRect.bottom);
    areaPath.close();
    canvas.drawPath(
      areaPath,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [lineColor.withAlpha(70), lineColor.withAlpha(0)],
        ).createShader(chartRect),
    );

    final linePath = Path()..moveTo(points.first.dx, points.first.dy);
    for (final p in points.skip(1)) {
      linePath.lineTo(p.dx, p.dy);
    }
    canvas.drawPath(
      linePath,
      Paint()
        ..color = lineColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5
        ..strokeJoin = StrokeJoin.round
        ..strokeCap = StrokeCap.round,
    );

    final dotPaint = Paint()..color = lineColor;
    for (final p in points) {
      canvas.drawCircle(p, 3, dotPaint);
    }

    final todayX = points[todayIndex].dx;
    _drawDashedLine(
      canvas,
      Offset(todayX, chartRect.top),
      Offset(todayX, chartRect.bottom),
      Paint()
        ..color = lineColor.withAlpha(180)
        ..strokeWidth = 1.5,
    );

    for (int i = 0; i < points.length; i++) {
      final isToday = i == todayIndex;
      if (i % daysStep != 0 && !isToday) continue;
      final tp = _text(_fmtAxis(data[i]), lineColor, bold: isToday);
      tp.paint(canvas, Offset(points[i].dx - tp.width / 2, points[i].dy - tp.height - 4));
    }

    for (int i = 0; i < dayLabels.length && i < points.length; i++) {
      final isToday = i == todayIndex;
      final day = dayLabels[i];
      if (day % daysStep != 0 && !isToday) continue;
      final tp = _text('$day', isToday ? lineColor : labelColor, bold: isToday);
      tp.paint(canvas, Offset(points[i].dx - tp.width / 2, chartRect.bottom + 4));
    }
  }

  void _drawDashedLine(Canvas canvas, Offset start, Offset end, Paint paint) {
    const dashLength = 4.0;
    const gapLength = 3.0;
    final totalLength = (end - start).distance;
    if (totalLength == 0) return;
    final direction = (end - start) / totalLength;
    double drawn = 0;
    while (drawn < totalLength) {
      final segEnd = min(drawn + dashLength, totalLength);
      canvas.drawLine(start + direction * drawn, start + direction * segEnd, paint);
      drawn += dashLength + gapLength;
    }
  }

  TextPainter _text(String s, Color color, {bool bold = false}) {
    final tp = TextPainter(
      text: TextSpan(
        text: s,
        style: (textStyle ?? const TextStyle(fontSize: 11)).copyWith(
          color: color,
          fontWeight: bold ? FontWeight.w700 : null,
        ),
      ),
      textDirection: TextDirection.ltr,
    );
    tp.layout();
    return tp;
  }

  String _fmtAxis(double v) {
    if (v >= 1000) return '${(v / 1000).toStringAsFixed(v % 1000 == 0 ? 0 : 1)}k';
    return v.round().toString();
  }

  double _niceCeil(double maxValue) {
    if (maxValue <= 0) return 4;
    final raw = maxValue / 4;
    final magnitude = pow(10, (log(raw) / ln10).floor()).toDouble();
    final residual = raw / magnitude;
    final niceStep = residual <= 1
        ? 1
        : residual <= 2
            ? 2
            : residual <= 5
                ? 5
                : 10;
    return niceStep * magnitude * 4;
  }

  @override
  bool shouldRepaint(covariant _LineChartPainter oldDelegate) {
    return oldDelegate.data != data ||
        oldDelegate.lineColor != lineColor ||
        oldDelegate.gridColor != gridColor;
  }
}
