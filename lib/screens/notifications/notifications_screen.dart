import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  static const _items = [
    _Notif(
      icon: Icons.emoji_events_rounded,
      title: 'Nuevo logro desbloqueado',
      body: 'Has completado 7 días seguidos de actividad.',
      time: 'hace 5 min',
      isNew: true,
    ),
    _Notif(
      icon: Icons.group_rounded,
      title: 'Solicitud de amistad',
      body: 'sofia_mn quiere ser tu amiga.',
      time: 'hace 1 h',
      isNew: true,
    ),
    _Notif(
      icon: Icons.task_alt_rounded,
      title: 'Tarea vencida',
      body: 'Revisar documentación de Flutter venció ayer.',
      time: 'ayer',
      isNew: false,
    ),
    _Notif(
      icon: Icons.trending_up_rounded,
      title: 'Subiste en el ranking',
      body: 'Ahora estás en la posición #4 entre tus amigos.',
      time: 'ayer',
      isNew: false,
    ),
    _Notif(
      icon: Icons.directions_walk_rounded,
      title: 'Meta de pasos alcanzada',
      body: 'Completaste tus 10 000 pasos diarios.',
      time: 'hace 2 días',
      isNew: false,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Avisos',
        actions: [
          TextButton(
            onPressed: () {},
            child: const Text('Leer todo'),
          ),
        ],
      ),
      body: ListView.builder(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        itemCount: _items.length,
        itemBuilder: (ctx, i) {
          final n = _items[i];
          return Container(
            margin: const EdgeInsets.only(bottom: 8),
            decoration: BoxDecoration(
              color: n.isNew
                  ? colors.primaryContainer.withAlpha(80)
                  : colors.surfaceContainerLow,
              borderRadius: BorderRadius.circular(16),
              border: n.isNew
                  ? Border.all(color: colors.primary.withAlpha(60))
                  : null,
            ),
            child: ListTile(
              contentPadding:
                  const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              leading: Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: colors.primaryContainer,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(n.icon, size: 22, color: colors.primary),
              ),
              title: Text(
                n.title,
                style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
              ),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 2),
                  Text(n.body,
                      style: text.bodySmall
                          ?.copyWith(color: colors.onSurfaceVariant)),
                  const SizedBox(height: 4),
                  Text(n.time,
                      style: text.labelSmall
                          ?.copyWith(color: colors.onSurfaceVariant)),
                ],
              ),
              trailing: n.isNew
                  ? Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: colors.primary,
                        shape: BoxShape.circle,
                      ),
                    )
                  : null,
            ),
          );
        },
      ),
    );
  }
}

class _Notif {
  final IconData icon;
  final String title;
  final String body;
  final String time;
  final bool isNew;
  const _Notif({
    required this.icon,
    required this.title,
    required this.body,
    required this.time,
    required this.isNew,
  });
}
