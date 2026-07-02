import 'package:flutter/material.dart';
import '../basic/avatar.dart';

enum FriendStatus { accepted, pendingSent, pendingReceived }

class FriendCard extends StatelessWidget {
  final String username;
  final String? avatarUrl;
  final FriendStatus status;
  final VoidCallback? onAccept;
  final VoidCallback? onDecline;
  final VoidCallback? onTap;

  const FriendCard({
    super.key,
    required this.username,
    this.avatarUrl,
    this.status = FriendStatus.accepted,
    this.onAccept,
    this.onDecline,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              DayPilotAvatar(name: username, imageUrl: avatarUrl, size: 44),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      username,
                      style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w500),
                    ),
                    Text(
                      _statusLabel(),
                      style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
              _buildTrailing(context, colors),
            ],
          ),
        ),
      ),
    );
  }

  String _statusLabel() => switch (status) {
        FriendStatus.accepted       => 'Amigo',
        FriendStatus.pendingSent    => 'Solicitud enviada',
        FriendStatus.pendingReceived => 'Quiere ser tu amigo',
      };

  Widget _buildTrailing(BuildContext context, ColorScheme colors) {
    return switch (status) {
      FriendStatus.accepted => Icon(Icons.people_rounded, color: colors.primary),
      FriendStatus.pendingSent => Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: colors.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          'Pendiente',
          style: Theme.of(context).textTheme.labelMedium?.copyWith(
                color: colors.onSurfaceVariant,
              ),
        ),
      ),
      FriendStatus.pendingReceived => Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconButton(
            icon: Icon(Icons.close_rounded, color: colors.error),
            onPressed: onDecline,
            tooltip: 'Rechazar',
          ),
          FilledButton(
            onPressed: onAccept,
            child: const Text('Aceptar'),
          ),
        ],
      ),
    };
  }
}

class UserSearchCard extends StatelessWidget {
  final String username;
  final String? avatarUrl;
  final bool isFriend;
  final bool isPending;
  final VoidCallback? onAdd;
  final VoidCallback? onTap;

  const UserSearchCard({
    super.key,
    required this.username,
    this.avatarUrl,
    this.isFriend = false,
    this.isPending = false,
    this.onAdd,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              DayPilotAvatar(name: username, imageUrl: avatarUrl, size: 44),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  username,
                  style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w500),
                ),
              ),
              if (isFriend)
                Icon(Icons.people_rounded, color: colors.primary)
              else if (isPending)
                Text(
                  'Pendiente',
                  style: text.labelMedium?.copyWith(color: colors.onSurfaceVariant),
                )
              else
                FilledButton.tonal(
                  onPressed: onAdd,
                  child: const Text('Añadir'),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
