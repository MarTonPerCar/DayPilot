import 'dart:math' as math;
import 'package:flutter/material.dart';

class TimerCard extends StatelessWidget {
  final String modeName;
  final double progress;
  final String timeDisplay;
  final bool isRunning;
  final VoidCallback? onPlayPause;

  const TimerCard({
    super.key,
    required this.modeName,
    required this.progress,
    required this.timeDisplay,
    this.isRunning = false,
    this.onPlayPause,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
        child: Column(
          children: [
            Text(
              modeName,
              style: text.titleMedium?.copyWith(
                color: colors.onSurfaceVariant,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: 200,
              height: 200,
              child: CustomPaint(
                painter: _ArcPainter(
                  progress: progress.clamp(0.0, 1.0),
                  color: colors.primary,
                  trackColor: colors.surfaceContainerHighest,
                  strokeWidth: 10,
                ),
                child: Center(
                  child: Text(
                    timeDisplay,
                    style: text.displaySmall?.copyWith(
                      fontWeight: FontWeight.w300,
                      color: colors.onSurface,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 28),
            FilledButton.icon(
              onPressed: onPlayPause,
              icon: Icon(isRunning
                  ? Icons.pause_rounded
                  : Icons.play_arrow_rounded),
              label: Text(isRunning ? 'Pausar' : 'Iniciar'),
              style: FilledButton.styleFrom(
                minimumSize: const Size(160, 48),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ArcPainter extends CustomPainter {
  final double progress;
  final Color color;
  final Color trackColor;
  final double strokeWidth;

  const _ArcPainter({
    required this.progress,
    required this.color,
    required this.trackColor,
    this.strokeWidth = 8.0,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = (size.shortestSide - strokeWidth) / 2;
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;

    paint.color = trackColor;
    canvas.drawCircle(center, radius, paint);

    if (progress > 0) {
      paint.color = color;
      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        -math.pi / 2,
        progress * 2 * math.pi,
        false,
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(_ArcPainter old) =>
      old.progress != progress ||
      old.color != color ||
      old.trackColor != trackColor;
}

class TimerHubCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final Color? accentColor;
  final VoidCallback? onTap;

  const TimerHubCard({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    this.accentColor,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final accent = accentColor ?? colors.primary;

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  color: accent.withAlpha(30),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(icon, color: accent, size: 28),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: text.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      description,
                      style:
                          text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
              Icon(Icons.chevron_right_rounded, color: colors.onSurfaceVariant),
            ],
          ),
        ),
      ),
    );
  }
}
