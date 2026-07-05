import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/divider.dart';
import '../../components/forms/form_section.dart';
import '../../components/forms/switch_tile.dart';
import '../../components/forms/theme_swatch_picker.dart';
import '../../component_catalog.dart';
import '../../l10n/app_localizations.dart';
import '../../l10n/locale_notifier.dart';
import '../../theme/app_theme.dart';
import '../auth/login_screen.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _notifications = true;
  bool _taskReminders = true;
  bool _streakAlerts = true;

  static const _themeColors = {
    DayPilotTheme.sageGreen: Color(0xFF4A7C59),
    DayPilotTheme.ocean: Color(0xFF1A6B8A),
    DayPilotTheme.lavender: Color(0xFF6B4FA8),
    DayPilotTheme.amber: Color(0xFFB85C00),
    DayPilotTheme.amoled: Color(0xFF424242),
  };

  static const _languages = [
    (locale: Locale('es'), nativeName: 'Español'),
    (locale: Locale('en'), nativeName: 'English'),
    (locale: Locale('de'), nativeName: 'Deutsch'),
  ];

  String _themeName(AppLocalizations l10n, DayPilotTheme t) => switch (t) {
        DayPilotTheme.sageGreen => l10n.themeSageGreen,
        DayPilotTheme.ocean => l10n.themeOcean,
        DayPilotTheme.lavender => l10n.themeLavender,
        DayPilotTheme.amber => l10n.themeAmber,
        DayPilotTheme.amoled => l10n.themeAmoled,
      };

  void _pickLanguage(Locale current) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        final l10n = AppLocalizations.of(context);
        final colors = Theme.of(context).colorScheme;
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    l10n.settingsChooseLanguage,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                  ),
                ),
              ),
              ..._languages.map((lang) {
                final isSelected = lang.locale.languageCode == current.languageCode;
                return ListTile(
                  title: Text(lang.nativeName),
                  trailing: isSelected ? Icon(Icons.check_rounded, color: colors.primary) : null,
                  onTap: () {
                    dayPilotLocaleNotifier.value = lang.locale;
                    Navigator.pop(context);
                  },
                );
              }),
              const SizedBox(height: 8),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.settingsTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        children: [
          ValueListenableBuilder<DayPilotTheme>(
            valueListenable: dayPilotThemeNotifier,
            builder: (context, activeTheme, _) {
              return DayPilotThemeSwatchPicker<DayPilotTheme>(
                value: activeTheme,
                options: DayPilotTheme.values,
                colorOf: (t) => _themeColors[t]!,
                nameOf: (t) => _themeName(l10n, t),
                onChanged: (t) => dayPilotThemeNotifier.value = t,
              );
            },
          ),
          const SizedBox(height: 20),

          ValueListenableBuilder<ThemeMode>(
            valueListenable: dayPilotThemeModeNotifier,
            builder: (context, activeThemeMode, _) {
              return DayPilotFormSection(
                children: [
                  DayPilotSwitchTile(
                    label: l10n.settingsDarkMode,
                    subtitle: l10n.settingsDarkModeSubtitle,
                    icon: Icons.dark_mode_outlined,
                    value: activeThemeMode == ThemeMode.dark,
                    onChanged: (v) => dayPilotThemeModeNotifier.value =
                        v ? ThemeMode.dark : ThemeMode.system,
                  ),
                ],
              );
            },
          ),
          const SizedBox(height: 16),

          DayPilotFormSection(
            children: [
              DayPilotSwitchTile(
                label: l10n.settingsNotifications,
                subtitle: l10n.settingsNotificationsSubtitle,
                icon: Icons.notifications_outlined,
                value: _notifications,
                onChanged: (v) => setState(() => _notifications = v),
              ),
              DayPilotSwitchTile(
                label: l10n.settingsTaskReminders,
                subtitle: l10n.settingsTaskRemindersSubtitle,
                icon: Icons.calendar_today_outlined,
                value: _notifications && _taskReminders,
                onChanged: _notifications
                    ? (v) => setState(() => _taskReminders = v)
                    : null,
              ),
              DayPilotSwitchTile(
                label: l10n.settingsStreakAlert,
                subtitle: l10n.settingsStreakAlertSubtitle,
                icon: Icons.local_fire_department_outlined,
                value: _notifications && _streakAlerts,
                onChanged: _notifications
                    ? (v) => setState(() => _streakAlerts = v)
                    : null,
              ),
            ],
          ),
          const SizedBox(height: 16),

          DayPilotFormSection(
            children: [
              ListTile(
                leading: const Icon(Icons.person_outline_rounded),
                title: Text(l10n.settingsEditProfile),
                subtitle: Text(l10n.settingsEditProfileSubtitle),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: () {},
              ),
            ],
          ),
          const SizedBox(height: 16),

          ValueListenableBuilder<Locale>(
            valueListenable: dayPilotLocaleNotifier,
            builder: (context, activeLocale, _) {
              final nativeName = _languages
                  .firstWhere((l) => l.locale.languageCode == activeLocale.languageCode)
                  .nativeName;
              return DayPilotFormSection(
                children: [
                  ListTile(
                    leading: const Icon(Icons.language_rounded),
                    title: Text(l10n.settingsLanguage),
                    subtitle: Text(nativeName),
                    trailing: const Icon(Icons.expand_more_rounded),
                    onTap: () => _pickLanguage(activeLocale),
                  ),
                ],
              );
            },
          ),
          const SizedBox(height: 16),

          DayPilotFormSection(
            title: l10n.settingsDeveloper,
            children: [
              ListTile(
                leading: Icon(Icons.developer_mode_rounded, color: colors.tertiary),
                title: Text(l10n.settingsComponentCatalog,
                    style: TextStyle(color: colors.tertiary)),
                trailing: Icon(Icons.open_in_new_rounded, color: colors.tertiary, size: 18),
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const ComponentCatalog()),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          const DayPilotDivider(),
          const SizedBox(height: 16),

          FilledButton.icon(
            onPressed: () => Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(builder: (_) => const LoginScreen()),
              (_) => false,
            ),
            icon: const Icon(Icons.logout_rounded),
            label: Text(l10n.settingsSignOut),
            style: FilledButton.styleFrom(
              backgroundColor: colors.errorContainer,
              foregroundColor: colors.onErrorContainer,
              minimumSize: const Size.fromHeight(52),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(28),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
