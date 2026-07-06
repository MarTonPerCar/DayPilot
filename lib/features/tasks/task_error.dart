import '../../l10n/app_localizations.dart';

enum TaskErrorType { create, update, toggle, delete }

String taskErrorMessage(TaskErrorType type, AppLocalizations l10n) => switch (type) {
      TaskErrorType.create => l10n.taskErrorCreate,
      TaskErrorType.update => l10n.taskErrorUpdate,
      TaskErrorType.toggle => l10n.taskErrorToggle,
      TaskErrorType.delete => l10n.taskErrorDelete,
    };
