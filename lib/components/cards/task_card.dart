import 'package:flutter/material.dart';
import '../basic/task_dot.dart';

class TaskCard extends StatelessWidget {
  final String title;
  final String? description;
  final String? dueDate;
  final TaskPriority priority;
  final String? category;
  final bool completed;
  final VoidCallback? onToggle;
  final VoidCallback? onTap;

  const TaskCard({
    super.key,
    required this.title,
    this.description,
    this.dueDate,
    this.priority = TaskPriority.medium,
    this.category,
    this.completed = false,
    this.onToggle,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
          child: Row(
            children: [
              Checkbox(
                value: completed,
                onChanged: (_) => onToggle?.call(),
                shape: const CircleBorder(),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(right: 12, top: 8, bottom: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          TaskDot(priority: priority),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              title,
                              style: text.bodyLarge?.copyWith(
                                decoration: completed ? TextDecoration.lineThrough : null,
                                color: completed ? colors.onSurfaceVariant : colors.onSurface,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                      if (description != null) ...[
                        const SizedBox(height: 2),
                        Text(
                          description!,
                          style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                      if (dueDate != null || category != null) ...[
                        const SizedBox(height: 6),
                        Row(
                          children: [
                            if (dueDate != null) ...[
                              Icon(Icons.calendar_today_outlined, size: 12, color: colors.onSurfaceVariant),
                              const SizedBox(width: 4),
                              Text(dueDate!, style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
                            ],
                            if (category != null) ...[
                              const SizedBox(width: 8),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                decoration: BoxDecoration(
                                  color: colors.secondaryContainer,
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Text(
                                  category!,
                                  style: text.labelSmall?.copyWith(color: colors.onSecondaryContainer),
                                ),
                              ),
                            ],
                          ],
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class TaskSwipeCard extends StatelessWidget {
  final String id;
  final String title;
  final String? description;
  final String? dueDate;
  final TaskPriority priority;
  final String? category;
  final bool completed;
  final VoidCallback? onToggle;
  final VoidCallback? onTap;
  final VoidCallback? onDelete;

  const TaskSwipeCard({
    super.key,
    required this.id,
    required this.title,
    this.description,
    this.dueDate,
    this.priority = TaskPriority.medium,
    this.category,
    this.completed = false,
    this.onToggle,
    this.onTap,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Dismissible(
      key: ValueKey(id),
      direction: DismissDirection.endToStart,
      onDismissed: (_) => onDelete?.call(),
      background: Container(
        decoration: BoxDecoration(
          color: colors.errorContainer,
          borderRadius: BorderRadius.circular(12),
        ),
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        child: Icon(Icons.delete_outline_rounded, color: colors.onErrorContainer),
      ),
      child: TaskCard(
        title: title,
        description: description,
        dueDate: dueDate,
        priority: priority,
        category: category,
        completed: completed,
        onToggle: onToggle,
        onTap: onTap,
      ),
    );
  }
}
