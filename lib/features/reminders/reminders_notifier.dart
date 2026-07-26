import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/data/models/app_reminder.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../../core/window/desktop_notifications.dart';
import '../../core/window/desktop_window.dart' show isDesktopPlatform;
import '../../l10n/app_localizations.dart';
import 'reminder_scheduling.dart';

class RemindersNotifier extends Notifier<List<AppReminder>> {
  static const _checkInterval = Duration(seconds: 20);

  final Set<String> _earlyFired = {};
  AppLocalizations? _l10n;

  @override
  List<AppReminder> build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_checkInterval, (_) => _checkDue());
    ref.onDispose(timer.cancel);
    return [];
  }

  /// Localized strings for the fired-notification text can't come from a widget's
  /// BuildContext here (this runs on a timer, off the widget tree) — the app's root
  /// MaterialApp installs this lookup so notification text still follows the user's locale.
  void configureLocalization(AppLocalizations l10n) => _l10n = l10n;

  Future<void> refresh() async {
    try {
      state = await ref.read(reminderRepositoryProvider).getReminders();
    } catch (e, st) {
      AppLogger.logError('RemindersNotifier.refresh', e, st);
    }
  }

  Future<void> addReminder(AppReminder reminder) async {
    await ref.read(reminderRepositoryProvider).addReminder(reminder);
    await refresh();
  }

  Future<void> deleteReminder(String id) async {
    _earlyFired.remove(id);
    await ref.read(reminderRepositoryProvider).deleteReminder(id);
    await refresh();
  }

  Future<void> toggleReminder(String id, bool enabled) async {
    if (!enabled) _earlyFired.remove(id);
    await ref.read(reminderRepositoryProvider).toggleReminder(id, enabled);
    await refresh();
  }

  Future<void> _checkDue() async {
    if (state.isEmpty) return;
    final now = DateTime.now();

    for (final reminder in state) {
      if (!reminder.enabled) continue;

      if (!_earlyFired.contains(reminder.id) && isEarlyDue(reminder, now)) {
        _earlyFired.add(reminder.id);
        await _fire(
          title: _l10n?.reminderFiredEarlyTitle(reminder.title) ?? reminder.title,
          body: _l10n?.reminderFiredEarlyBody ?? '',
        );
      }

      if (isMainDue(reminder, now)) {
        _earlyFired.remove(reminder.id);
        await _fire(title: reminder.title, body: _l10n?.reminderFiredBody ?? '');
        await _handleRecurrence(reminder);
      }
    }
  }

  Future<void> _fire({required String title, required String body}) async {
    if (!isDesktopPlatform) return;
    try {
      await showDesktopNotification(title, body);
    } catch (e, st) {
      AppLogger.logError('RemindersNotifier.fire', e, st);
    }
  }

  Future<void> _handleRecurrence(AppReminder reminder) async {
    final repo = ref.read(reminderRepositoryProvider);
    final next = nextOccurrence(reminder);
    if (next == null) {
      await repo.deleteReminder(reminder.id);
    } else {
      await repo.updateDateTime(reminder.id, next);
    }
    await refresh();
  }
}

final remindersNotifierProvider = NotifierProvider<RemindersNotifier, List<AppReminder>>(
  RemindersNotifier.new,
);
