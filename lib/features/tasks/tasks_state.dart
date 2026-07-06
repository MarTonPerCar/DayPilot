import '../../core/data/models/app_task.dart';
import 'task_error.dart';

class TasksState {
  const TasksState({this.tasks = const [], this.isLoading = false, this.errorType});

  final List<AppTask> tasks;
  final bool isLoading;
  final TaskErrorType? errorType;

  TasksState copyWith({List<AppTask>? tasks, bool? isLoading, TaskErrorType? errorType}) {
    return TasksState(
      tasks: tasks ?? this.tasks,
      isLoading: isLoading ?? this.isLoading,
      errorType: errorType,
    );
  }
}
