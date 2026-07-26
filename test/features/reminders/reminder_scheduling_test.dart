import 'package:daypilot/core/data/models/app_reminder.dart';
import 'package:daypilot/features/reminders/reminder_scheduling.dart';
import 'package:flutter_test/flutter_test.dart';

AppReminder _reminder({
  required DateTime dateTime,
  bool notifyBefore = false,
  String frequency = 'once',
}) {
  return AppReminder(
    id: 'r1',
    title: 'Take a break',
    dateTime: dateTime,
    frequency: frequency,
    notifyBefore: notifyBefore,
  );
}

void main() {
  final now = DateTime(2026, 1, 10, 12, 0);

  group('isMainDue', () {
    test('is due once the target time has passed', () {
      expect(isMainDue(_reminder(dateTime: now.subtract(const Duration(seconds: 1))), now), isTrue);
    });

    test('is due exactly at the target time', () {
      expect(isMainDue(_reminder(dateTime: now), now), isTrue);
    });

    test('is not due before the target time', () {
      expect(isMainDue(_reminder(dateTime: now.add(const Duration(minutes: 1))), now), isFalse);
    });
  });

  group('isEarlyDue', () {
    test('is due inside the 10-minute warning window when notifyBefore is set', () {
      final reminder = _reminder(dateTime: now.add(const Duration(minutes: 5)), notifyBefore: true);
      expect(isEarlyDue(reminder, now), isTrue);
    });

    test('is not due more than 10 minutes before the target time', () {
      final reminder = _reminder(dateTime: now.add(const Duration(minutes: 11)), notifyBefore: true);
      expect(isEarlyDue(reminder, now), isFalse);
    });

    test('is not due once the target time itself has arrived', () {
      final reminder = _reminder(dateTime: now, notifyBefore: true);
      expect(isEarlyDue(reminder, now), isFalse);
    });

    test('is never due when notifyBefore is false', () {
      final reminder = _reminder(dateTime: now.add(const Duration(minutes: 5)));
      expect(isEarlyDue(reminder, now), isFalse);
    });
  });

  group('nextOccurrence', () {
    test('a daily reminder advances by exactly one day', () {
      final reminder = _reminder(dateTime: now, frequency: 'daily');
      expect(nextOccurrence(reminder), now.add(const Duration(days: 1)));
    });

    test('a weekly reminder advances by exactly seven days', () {
      final reminder = _reminder(dateTime: now, frequency: 'weekly');
      expect(nextOccurrence(reminder), now.add(const Duration(days: 7)));
    });

    test('a one-off reminder has no next occurrence', () {
      final reminder = _reminder(dateTime: now, frequency: 'once');
      expect(nextOccurrence(reminder), isNull);
    });
  });
}
