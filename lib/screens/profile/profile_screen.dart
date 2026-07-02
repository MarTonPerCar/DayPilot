import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/avatar.dart';
import '../../components/basic/divider.dart';
import '../../components/cards/profile_stats_card.dart';
import '../../components/forms/form_section.dart';
import '../../components/forms/switch_tile.dart';
import '../../components/forms/select_field.dart';
import '../../component_catalog.dart';
import '../auth/login_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  bool _notifications = true;
  bool _darkMode = false;
  String _theme = 'Verde salvia';

  static const _themes = ['Verde salvia', 'Océano', 'Lavanda', 'Ámbar', 'AMOLED'];

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Perfil',
        actions: [
          IconButton(icon: const Icon(Icons.edit_outlined), onPressed: () {}),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        children: [

          // ── Avatar + username
          Center(
            child: Column(
              children: [
                const DayPilotAvatar(name: 'Mario García', size: 84),
                const SizedBox(height: 12),
                Text(
                  'mario_garcia',
                  style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700),
                ),
                Text(
                  'unNobleXD@hotmail.com',
                  style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                ),
                const SizedBox(height: 4),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                  decoration: BoxDecoration(
                    color: colors.primaryContainer,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    'Miembro desde jun 2024',
                    style: text.labelSmall?.copyWith(color: colors.onPrimaryContainer),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // ── Stats
          const ProfileStatsCard(
            level: 8,
            totalPoints: 12840,
            streak: 12,
            tasksCompleted: 347,
          ),
          const SizedBox(height: 20),

          // ── Preferences
          DayPilotFormSection(
            title: 'Preferencias',
            children: [
              DayPilotSwitchTile(
                label: 'Notificaciones',
                subtitle: 'Recibir alertas y recordatorios',
                icon: Icons.notifications_outlined,
                value: _notifications,
                onChanged: (v) => setState(() => _notifications = v),
              ),
              DayPilotSwitchTile(
                label: 'Modo oscuro',
                icon: Icons.dark_mode_outlined,
                value: _darkMode,
                onChanged: (v) => setState(() => _darkMode = v),
              ),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
                child: DayPilotSelectField<String>(
                  label: 'Tema de color',
                  value: _theme,
                  options: _themes,
                  display: (s) => s,
                  prefixIcon: Icons.palette_outlined,
                  onChanged: (v) => setState(() => _theme = v),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // ── Account
          DayPilotFormSection(
            title: 'Cuenta',
            children: [
              ListTile(
                leading: const Icon(Icons.person_outline_rounded),
                title: const Text('Editar perfil'),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: () {},
              ),
              ListTile(
                leading: const Icon(Icons.privacy_tip_outlined),
                title: const Text('Privacidad'),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: () {},
              ),
              ListTile(
                leading: const Icon(Icons.help_outline_rounded),
                title: const Text('Ayuda y soporte'),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: () {},
              ),
            ],
          ),
          const SizedBox(height: 16),

          // ── Debug
          DayPilotFormSection(
            title: 'Desarrollador',
            children: [
              ListTile(
                leading: Icon(Icons.developer_mode_rounded, color: colors.tertiary),
                title: Text('Catálogo de componentes',
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

          // ── Sign out
          OutlinedButton.icon(
            onPressed: () => Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(builder: (_) => const LoginScreen()),
              (_) => false,
            ),
            icon: const Icon(Icons.logout_rounded),
            label: const Text('Cerrar sesión'),
            style: OutlinedButton.styleFrom(
              foregroundColor: colors.error,
              side: BorderSide(color: colors.error.withAlpha(150)),
              minimumSize: const Size.fromHeight(48),
            ),
          ),
        ],
      ),
    );
  }
}
