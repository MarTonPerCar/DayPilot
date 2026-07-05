import 'package:flutter/material.dart';

class DottedSliderField extends StatelessWidget {
  final double value;
  final double min;
  final double max;
  final ValueChanged<double> onChanged;
  final String? leftLabel;
  final String? rightLabel;
  final Color? activeColor;

  const DottedSliderField({
    super.key,
    required this.value,
    required this.min,
    required this.max,
    required this.onChanged,
    this.leftLabel,
    this.rightLabel,
    this.activeColor,
  });

  void _handle(Offset localPosition, double width) {
    final ratio = (localPosition.dx / width).clamp(0.0, 1.0);
    onChanged(min + (max - min) * ratio);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final color = activeColor ?? colors.primary;
    final ratio = max > min ? ((value - min) / (max - min)).clamp(0.0, 1.0) : 0.0;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        LayoutBuilder(
          builder: (context, constraints) {
            final width = constraints.maxWidth;
            return GestureDetector(
              onTapDown: (d) => _handle(d.localPosition, width),
              onHorizontalDragUpdate: (d) => _handle(d.localPosition, width),
              child: SizedBox(
                height: 28,
                width: double.infinity,
                child: CustomPaint(
                  painter: _DottedTrackPainter(
                    ratio: ratio,
                    activeColor: color,
                    inactiveColor: colors.surfaceContainerHighest,
                  ),
                ),
              ),
            );
          },
        ),
        if (leftLabel != null || rightLabel != null) ...[
          const SizedBox(height: 4),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(leftLabel ?? '', style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
              Text(rightLabel ?? '', style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
            ],
          ),
        ],
      ],
    );
  }
}

class _DottedTrackPainter extends CustomPainter {
  final double ratio;
  final Color activeColor;
  final Color inactiveColor;

  _DottedTrackPainter({
    required this.ratio,
    required this.activeColor,
    required this.inactiveColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    const trackHeight = 10.0;
    final trackY = size.height / 2;
    final thumbX = size.width * ratio;
    const radius = trackHeight / 2;

    if (thumbX > 0) {
      canvas.drawRRect(
        RRect.fromRectAndCorners(
          Rect.fromLTWH(0, trackY - radius, thumbX, trackHeight),
          topLeft: const Radius.circular(radius),
          bottomLeft: const Radius.circular(radius),
          topRight: thumbX >= size.width ? const Radius.circular(radius) : Radius.zero,
          bottomRight: thumbX >= size.width ? const Radius.circular(radius) : Radius.zero,
        ),
        Paint()..color = activeColor,
      );
    }
    if (thumbX < size.width) {
      canvas.drawRRect(
        RRect.fromRectAndCorners(
          Rect.fromLTWH(thumbX, trackY - radius, size.width - thumbX, trackHeight),
          topRight: const Radius.circular(radius),
          bottomRight: const Radius.circular(radius),
          topLeft: thumbX <= 0 ? const Radius.circular(radius) : Radius.zero,
          bottomLeft: thumbX <= 0 ? const Radius.circular(radius) : Radius.zero,
        ),
        Paint()..color = inactiveColor,
      );
    }

    const dotSpacing = 9.0;
    final dotCount = (size.width / dotSpacing).floor();
    for (int i = 0; i <= dotCount; i++) {
      final x = i * dotSpacing + dotSpacing / 2;
      if (x > size.width) break;
      final onActive = x < thumbX;
      canvas.drawCircle(
        Offset(x, trackY),
        1.5,
        Paint()..color = onActive ? Colors.white.withAlpha(170) : activeColor.withAlpha(90),
      );
    }

    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromCenter(center: Offset(thumbX, trackY), width: 3, height: size.height),
        const Radius.circular(2),
      ),
      Paint()..color = activeColor,
    );
  }

  @override
  bool shouldRepaint(covariant _DottedTrackPainter oldDelegate) {
    return oldDelegate.ratio != ratio || oldDelegate.activeColor != activeColor;
  }
}
