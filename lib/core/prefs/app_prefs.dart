import 'package:shared_preferences/shared_preferences.dart';

class AppPrefs {
  AppPrefs._(this._prefs);

  final SharedPreferences _prefs;

  static Future<AppPrefs> load() async => AppPrefs._(await SharedPreferences.getInstance());

  static const _themeKey = 'app_theme';
  static const _themeModeKey = 'app_theme_mode';
  static const _localeKey = 'app_locale';
  static const _notificationsKey = 'notifications_enabled';
  static const _taskRemindersKey = 'task_reminders_enabled';
  static const _streakAlertsKey = 'streak_alerts_enabled';
  static const _launchAtStartupConfiguredKey = 'launch_at_startup_configured';
  static const _lastOpenDateKey = 'last_open_date';
  static const _taskReminderFiredDateKey = 'task_reminder_fired_date';
  static const _streakAlertFiredDateKey = 'streak_alert_fired_date';

  String? get theme => _prefs.getString(_themeKey);
  Future<void> setTheme(String value) => _prefs.setString(_themeKey, value);

  String? get themeMode => _prefs.getString(_themeModeKey);
  Future<void> setThemeMode(String value) => _prefs.setString(_themeModeKey, value);

  String? get locale => _prefs.getString(_localeKey);
  Future<void> setLocale(String value) => _prefs.setString(_localeKey, value);

  bool get notificationsEnabled => _prefs.getBool(_notificationsKey) ?? true;
  Future<void> setNotificationsEnabled(bool value) => _prefs.setBool(_notificationsKey, value);

  bool get taskRemindersEnabled => _prefs.getBool(_taskRemindersKey) ?? true;
  Future<void> setTaskRemindersEnabled(bool value) => _prefs.setBool(_taskRemindersKey, value);

  bool get streakAlertsEnabled => _prefs.getBool(_streakAlertsKey) ?? true;
  Future<void> setStreakAlertsEnabled(bool value) => _prefs.setBool(_streakAlertsKey, value);

  bool get launchAtStartupConfigured => _prefs.getBool(_launchAtStartupConfiguredKey) ?? false;
  Future<void> setLaunchAtStartupConfigured(bool value) =>
      _prefs.setBool(_launchAtStartupConfiguredKey, value);

  String get lastOpenDate => _prefs.getString(_lastOpenDateKey) ?? '';
  Future<void> setLastOpenDate(String value) => _prefs.setString(_lastOpenDateKey, value);

  String get taskReminderFiredDate => _prefs.getString(_taskReminderFiredDateKey) ?? '';
  Future<void> setTaskReminderFiredDate(String value) =>
      _prefs.setString(_taskReminderFiredDateKey, value);

  String get streakAlertFiredDate => _prefs.getString(_streakAlertFiredDateKey) ?? '';
  Future<void> setStreakAlertFiredDate(String value) =>
      _prefs.setString(_streakAlertFiredDateKey, value);
}
