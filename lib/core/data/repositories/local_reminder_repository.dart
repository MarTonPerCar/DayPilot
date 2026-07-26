import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

import '../../logging/app_logger.dart';
import '../models/app_reminder.dart';
import 'reminder_repository.dart';

// Reminders are device-local only — no server-side table, unlike the rest of the app's data.
class LocalReminderRepository implements ReminderRepository {
  static const _prefsKey = 'daypilot_reminders';

  Future<List<AppReminder>> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_prefsKey);
    if (raw == null) return [];
    try {
      final decoded = jsonDecode(raw) as List<dynamic>;
      return decoded.map((e) => AppReminder.fromJson(e as Map<String, dynamic>)).toList();
    } catch (e, st) {
      AppLogger.logError('LocalReminderRepository.load', e, st);
      return [];
    }
  }

  Future<void> _save(List<AppReminder> reminders) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefsKey, jsonEncode(reminders.map((r) => r.toJson()).toList()));
  }

  @override
  Future<List<AppReminder>> getReminders() => _load();

  @override
  Future<AppReminder> addReminder(AppReminder reminder) async {
    final list = await _load();
    list.add(reminder);
    await _save(list);
    return reminder;
  }

  @override
  Future<void> deleteReminder(String id) async {
    final list = await _load();
    list.removeWhere((r) => r.id == id);
    await _save(list);
  }

  @override
  Future<void> toggleReminder(String id, bool enabled) async {
    final list = await _load();
    final idx = list.indexWhere((r) => r.id == id);
    if (idx < 0) return;
    list[idx] = list[idx].copyWith(enabled: enabled);
    await _save(list);
  }

  @override
  Future<void> updateDateTime(String id, DateTime dateTime) async {
    final list = await _load();
    final idx = list.indexWhere((r) => r.id == id);
    if (idx < 0) return;
    list[idx] = list[idx].copyWith(dateTime: dateTime);
    await _save(list);
  }
}
