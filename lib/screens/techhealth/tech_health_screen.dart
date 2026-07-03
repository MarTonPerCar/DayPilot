import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/tech_restriction_card.dart';
import '../../components/forms/restriction_form_sheet.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import 'permissions_screen.dart';

class TechHealthScreen extends StatefulWidget {
  const TechHealthScreen({super.key});

  @override
  State<TechHealthScreen> createState() => _TechHealthScreenState();
}

class _TechHealthScreenState extends State<TechHealthScreen> {
  final List<TechRestriction> _restrictions = AppData.newRestrictionList();

  void _showPointInfo() {
    final l10n = AppLocalizations.of(context);
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.techHealthPointDialogTitle),
        content: Text(l10n.techHealthPointDialogBody),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: Text(l10n.commonAccept)),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: l10n.techHealthTitle,
        showBack: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.lock_outline_rounded),
            tooltip: l10n.permissionsTitle,
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const PermissionsScreen()),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
        children: [
          GestureDetector(
            onTap: _showPointInfo,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
              decoration: BoxDecoration(
                color: colors.primaryContainer,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 14,
                    backgroundColor: colors.primary,
                    child: Icon(Icons.info_outline_rounded, size: 16, color: colors.onPrimary),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      l10n.techHealthPointBannerLabel,
                      style: text.bodyMedium?.copyWith(
                        color: colors.onPrimaryContainer,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            l10n.techHealthRestrictionsTitle,
            style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          ),
          Text(
            l10n.techHealthRestrictionsCount(_restrictions.length),
            style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
          ),
          const SizedBox(height: 12),
          ...(_restrictions.map((r) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: TechRestrictionCard(
                  restriction: r,
                  onToggle: (v) => setState(() => r.enabled = v),
                  onDelete: () {},
                ),
              ))),
        ],
      ),
      // Nota: esta rama es solo diseño — crear/eliminar no persiste datos.
      floatingActionButton: FloatingActionButton(
        onPressed: () => showAddRestrictionSheet(
          context,
          onCreate: (_) {},
        ),
        child: const Icon(Icons.add_rounded),
      ),
    );
  }
}
