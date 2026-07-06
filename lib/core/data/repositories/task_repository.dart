import '../models/app_task.dart';
import '../models/task_category.dart';
import '../models/task_difficulty.dart';

abstract class TaskRepository {
  Future<List<AppTask>> getTasks();

  Future<void> addTask(NewTaskData data);

  Future<void> updateTask({
    required String id,
    required String title,
    required String description,
    required TaskCategory category,
    required TaskDifficulty difficulty,
    required int durationMinutes,
  });

  Future<void> toggleTask({required String occurrenceId, required bool isDone});

  Future<void> deleteTask(String id);
}
