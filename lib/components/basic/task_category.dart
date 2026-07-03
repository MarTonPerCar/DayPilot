import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

enum TaskCategory { trabajo, estudio, deporte, salud, personal, hogar, otro }

extension TaskCategoryX on TaskCategory {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return switch (this) {
      TaskCategory.trabajo => l10n.categoryTrabajo,
      TaskCategory.estudio => l10n.categoryEstudio,
      TaskCategory.deporte => l10n.categoryDeporte,
      TaskCategory.salud => l10n.categorySalud,
      TaskCategory.personal => l10n.categoryPersonal,
      TaskCategory.hogar => l10n.categoryHogar,
      TaskCategory.otro => l10n.categoryOtro,
    };
  }

  IconData get icon => switch (this) {
        TaskCategory.trabajo => Icons.work_rounded,
        TaskCategory.estudio => Icons.school_rounded,
        TaskCategory.deporte => Icons.directions_run_rounded,
        TaskCategory.salud => Icons.favorite_rounded,
        TaskCategory.personal => Icons.person_rounded,
        TaskCategory.hogar => Icons.home_rounded,
        TaskCategory.otro => Icons.star_rounded,
      };

  /// Colores semánticos fijos por categoría, iguales en cualquier tema,
  /// para que se puedan distinguir de un vistazo en calendario y filtros.
  Color get color => switch (this) {
        TaskCategory.trabajo => const Color(0xFF4A90D9),
        TaskCategory.estudio => const Color(0xFFAB6FDB),
        TaskCategory.deporte => const Color(0xFF4CAF50),
        TaskCategory.salud => const Color(0xFFE85D75),
        TaskCategory.personal => const Color(0xFFFFA726),
        TaskCategory.hogar => const Color(0xFFE8A33D),
        TaskCategory.otro => const Color(0xFF9E9E9E),
      };
}

/// Chip compacto de categoría: icono + etiqueta, coloreado según la categoría.
class TaskCategoryChip extends StatelessWidget {
  final TaskCategory category;
  final bool filled;

  const TaskCategoryChip({super.key, required this.category, this.filled = false});

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final color = category.color;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: filled ? color : color.withAlpha(38),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(category.icon, size: 12, color: filled ? Colors.white : color),
          const SizedBox(width: 4),
          Text(
            category.label(context),
            style: text.labelSmall?.copyWith(
              color: filled ? Colors.white : color,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
