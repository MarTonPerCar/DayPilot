import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

class ProfileInfoCard extends StatelessWidget {
  final String username;
  final String email;
  final String memberSince;

  const ProfileInfoCard({
    super.key,
    required this.username,
    required this.email,
    required this.memberSince,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final l10n = AppLocalizations.of(context);

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Column(
        children: [
          _InfoRow(label: l10n.profileInfoEmail, value: email),
          Divider(height: 1, indent: 16, endIndent: 16, color: colors.outlineVariant),
          _InfoRow(label: l10n.profileInfoSince, value: memberSince),
          Divider(height: 1, indent: 16, endIndent: 16, color: colors.outlineVariant),
          _InfoRow(label: l10n.profileInfoUsername, value: '@$username'),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;

  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
          Text(value, style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}
