import 'task_category.dart';
import 'task_difficulty.dart';

class NewTaskData {
  const NewTaskData({
    required this.date,
    required this.title,
    required this.category,
    required this.difficulty,
    required this.durationMinutes,
    this.description = '',
    this.reminder = false,
    this.recurring = false,
    this.recurrenceDays = 1,
  });

  final DateTime date;
  final String title;
  final String description;
  final TaskCategory category;
  final TaskDifficulty difficulty;
  final int durationMinutes;
  final bool reminder;
  final bool recurring;
  final int recurrenceDays;
}

/// [id] is the recurring series (`tasks.id`); [occurrenceId] is a single date
/// (`task_days.id`), used to toggle done.
class AppTask {
  const AppTask({
    required this.id,
    required this.occurrenceId,
    required this.title,
    required this.difficulty,
    required this.category,
    required this.date,
    required this.durationMinutes,
    this.description,
    this.reminder = false,
    this.recurring = false,
    this.done = false,
  });

  final String id;
  final String occurrenceId;
  final String title;
  final String? description;
  final TaskDifficulty difficulty;
  final TaskCategory category;
  final DateTime date;
  final int durationMinutes;
  final bool reminder;
  final bool recurring;
  final bool done;

  AppTask copyWith({
    String? title,
    String? description,
    TaskDifficulty? difficulty,
    TaskCategory? category,
    int? durationMinutes,
    bool? done,
  }) {
    return AppTask(
      id: id,
      occurrenceId: occurrenceId,
      title: title ?? this.title,
      description: description ?? this.description,
      difficulty: difficulty ?? this.difficulty,
      category: category ?? this.category,
      date: date,
      durationMinutes: durationMinutes ?? this.durationMinutes,
      reminder: reminder,
      recurring: recurring,
      done: done ?? this.done,
    );
  }

  factory AppTask.fromMap(Map<String, dynamic> map) {
    final dateParts = (map['date'] as String).split('-');
    return AppTask(
      id: map['task_id'] as String,
      occurrenceId: map['occurrence_id'] as String,
      title: map['title'] as String,
      description: map['description'] as String?,
      difficulty: _difficultyFromDb(map['difficulty'] as String),
      category: _categoryFromDb(map['category'] as String),
      date: DateTime(
        int.parse(dateParts[0]),
        int.parse(dateParts[1]),
        int.parse(dateParts[2]),
      ),
      durationMinutes: map['estimated_minutes'] as int,
      reminder: map['reminder_enabled'] as bool? ?? false,
      recurring: map['is_recurring'] as bool? ?? false,
      done: map['is_completed'] as bool,
    );
  }
}

String taskCategoryToDb(TaskCategory category) => switch (category) {
      TaskCategory.trabajo => 'Trabajo',
      TaskCategory.estudio => 'Estudio',
      TaskCategory.deporte => 'Deporte',
      TaskCategory.salud => 'Salud',
      TaskCategory.personal => 'General',
      TaskCategory.hogar => 'Hogar',
      TaskCategory.otro => 'General',
    };

TaskCategory _categoryFromDb(String value) => switch (value) {
      'Estudio' => TaskCategory.estudio,
      'Trabajo' => TaskCategory.trabajo,
      'Deporte' => TaskCategory.deporte,
      'Salud' => TaskCategory.salud,
      'Bienestar' => TaskCategory.salud,
      'Hogar' => TaskCategory.hogar,
      'Compra' => TaskCategory.hogar,
      'Finanzas' => TaskCategory.trabajo,
      _ => TaskCategory.otro,
    };

TaskDifficulty _difficultyFromDb(String value) => switch (value.toUpperCase()) {
      'MEDIUM' => TaskDifficulty.medium,
      'HARD' => TaskDifficulty.hard,
      _ => TaskDifficulty.easy,
    };
