import 'package:flutter/material.dart';

enum TaskPriority { low, medium, high }

class TaskDot extends StatelessWidget {
  final TaskPriority priority;
  final double size;

  const TaskDot({super.key, required this.priority, this.size = 10});

  Color _color(ColorScheme colors) => switch (priority) {
        TaskPriority.low    => colors.tertiary,
        TaskPriority.medium => colors.primary,
        TaskPriority.high   => colors.error,
      };

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: _color(Theme.of(context).colorScheme),
        shape: BoxShape.circle,
      ),
    );
  }
}
