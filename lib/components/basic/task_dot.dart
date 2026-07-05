import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

enum TaskDifficulty { easy, medium, hard }

extension TaskDifficultyX on TaskDifficulty {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return switch (this) {
      TaskDifficulty.easy => l10n.difficultyEasy,
      TaskDifficulty.medium => l10n.difficultyMedium,
      TaskDifficulty.hard => l10n.difficultyHard,
    };
  }

  /// Fijos: iguales en cualquier tema, no vienen de [ColorScheme].
  Color color(ColorScheme colors) => switch (this) {
        TaskDifficulty.easy => const Color(0xFF4CAF50),
        TaskDifficulty.medium => const Color(0xFFFFA726),
        TaskDifficulty.hard => colors.error,
      };
}

class DifficultyChip extends StatelessWidget {
  final TaskDifficulty difficulty;
  const DifficultyChip({super.key, required this.difficulty});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final color = difficulty.color(colors);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withAlpha(30),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withAlpha(120)),
      ),
      child: Text(
        difficulty.label(context),
        style: text.labelSmall?.copyWith(color: color, fontWeight: FontWeight.w700),
      ),
    );
  }
}

class TaskDot extends StatelessWidget {
  final TaskDifficulty priority;
  final double size;

  const TaskDot({super.key, required this.priority, this.size = 10});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: priority.color(Theme.of(context).colorScheme),
        shape: BoxShape.circle,
      ),
    );
  }
}
