import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../l10n/app_localizations.dart';

class PermissionsScreen extends StatelessWidget {
  const PermissionsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.permissionsTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 40),
        children: [
          Center(
            child: Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(color: colors.primaryContainer, shape: BoxShape.circle),
              child: Icon(Icons.lock_outline_rounded, color: colors.onPrimaryContainer, size: 34),
            ),
          ),
          const SizedBox(height: 20),
          Text(
            l10n.permissionsIntro,
            textAlign: TextAlign.center,
            style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
          ),
          const SizedBox(height: 20),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: colors.primaryContainer,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.warning_amber_rounded, color: colors.onPrimaryContainer, size: 20),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    l10n.permissionsWarning,
                    style: text.bodySmall?.copyWith(color: colors.onPrimaryContainer),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          Card.filled(
            clipBehavior: Clip.hardEdge,
            margin: EdgeInsets.zero,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 16,
                        backgroundColor: colors.primaryContainer,
                        child: Icon(Icons.check_rounded, color: colors.onPrimaryContainer, size: 18),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          l10n.permissionsUsageAccessTitle,
                          style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: colors.primaryContainer,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          l10n.permissionsGranted,
                          style: text.labelMedium?.copyWith(
                            color: colors.onPrimaryContainer,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Divider(height: 1, color: colors.outlineVariant),
                  const SizedBox(height: 12),
                  Text(
                    l10n.permissionsUsageAccessBody,
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card.filled(
            clipBehavior: Clip.hardEdge,
            margin: EdgeInsets.zero,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 16,
                        backgroundColor: colors.primaryContainer,
                        child: Text(
                          '2',
                          style: text.labelLarge?.copyWith(
                            color: colors.onPrimaryContainer,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          l10n.permissionsAccessibilityTitle,
                          style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Divider(height: 1, color: colors.outlineVariant),
                  const SizedBox(height: 12),
                  Text(
                    l10n.permissionsAccessibilityBody,
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                  const SizedBox(height: 10),
                  Text.rich(
                    TextSpan(
                      style: text.labelMedium?.copyWith(color: colors.onSurface),
                      children: [
                        TextSpan(text: l10n.permissionsPathSettings, style: const TextStyle(fontWeight: FontWeight.w700)),
                        TextSpan(text: l10n.permissionsPathAccessibility, style: const TextStyle(fontWeight: FontWeight.w700)),
                        TextSpan(text: l10n.permissionsPathInstalledServices, style: const TextStyle(fontWeight: FontWeight.w700)),
                        const TextSpan(text: 'DayPilot', style: TextStyle(fontWeight: FontWeight.w700)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: () {},
                      child: Text(l10n.permissionsOpenAccessibility),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
