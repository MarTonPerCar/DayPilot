import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../l10n/app_localizations.dart';

class TechHealthUnavailableScreen extends StatelessWidget {
  const TechHealthUnavailableScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.techHealthTitle, showBack: true),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 88,
                height: 88,
                decoration: BoxDecoration(color: colors.surfaceContainerHighest, shape: BoxShape.circle),
                child: Icon(Icons.phonelink_erase_rounded, size: 42, color: colors.onSurfaceVariant),
              ),
              const SizedBox(height: 24),
              Text(
                l10n.techHealthUnavailableTitle,
                textAlign: TextAlign.center,
                style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 12),
              Text(
                l10n.techHealthUnavailableBody,
                textAlign: TextAlign.center,
                style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
