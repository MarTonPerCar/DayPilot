import 'package:daypilot/core/data/models/app_reminder.dart';
import 'package:daypilot/core/data/repositories/local_reminder_repository.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  late LocalReminderRepository repo;

  setUp(() {
    SharedPreferences.setMockInitialValues({});
    repo = LocalReminderRepository();
  });

  test('starts empty when nothing has been saved yet', () async {
    expect(await repo.getReminders(), isEmpty);
  });

  test('addReminder persists it and returns it back unchanged', () async {
    final reminder = AppReminder(
      id: 'r1',
      title: 'Drink water',
      dateTime: DateTime(2026, 1, 10, 9, 0),
      frequency: 'daily',
      notifyBefore: true,
    );

    final added = await repo.addReminder(reminder);

    expect(added, same(reminder));
    final stored = await repo.getReminders();
    expect(stored, hasLength(1));
    expect(stored.single.id, 'r1');
    expect(stored.single.title, 'Drink water');
    expect(stored.single.dateTime, DateTime(2026, 1, 10, 9, 0));
    expect(stored.single.frequency, 'daily');
    expect(stored.single.notifyBefore, isTrue);
    expect(stored.single.enabled, isTrue);
  });

  test('deleteReminder removes only the matching reminder', () async {
    await repo.addReminder(AppReminder(id: 'r1', title: 'A', dateTime: DateTime(2026)));
    await repo.addReminder(AppReminder(id: 'r2', title: 'B', dateTime: DateTime(2026)));

    await repo.deleteReminder('r1');

    final stored = await repo.getReminders();
    expect(stored, hasLength(1));
    expect(stored.single.id, 'r2');
  });

  test('toggleReminder flips enabled without touching other reminders', () async {
    await repo.addReminder(AppReminder(id: 'r1', title: 'A', dateTime: DateTime(2026)));
    await repo.addReminder(AppReminder(id: 'r2', title: 'B', dateTime: DateTime(2026)));

    await repo.toggleReminder('r1', false);

    final stored = await repo.getReminders();
    expect(stored.firstWhere((r) => r.id == 'r1').enabled, isFalse);
    expect(stored.firstWhere((r) => r.id == 'r2').enabled, isTrue);
  });

  test('updateDateTime moves a reminder to its next occurrence', () async {
    await repo.addReminder(AppReminder(id: 'r1', title: 'A', dateTime: DateTime(2026, 1, 1)));

    await repo.updateDateTime('r1', DateTime(2026, 1, 2));

    final stored = await repo.getReminders();
    expect(stored.single.dateTime, DateTime(2026, 1, 2));
  });

  test('survives across separate repository instances (real persistence, not in-memory)', () async {
    await repo.addReminder(AppReminder(id: 'r1', title: 'A', dateTime: DateTime(2026)));

    final reloaded = LocalReminderRepository();

    expect(await reloaded.getReminders(), hasLength(1));
  });
}
