import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../components/basic/button.dart';
import '../components/basic/empty_state.dart';
import '../core/connectivity/connectivity_service.dart';
import '../core/connectivity/offline_notifier.dart';
import '../features/friends/friends_notifier.dart';
import '../features/notifications/notifications_notifier.dart';
import '../features/profile/profile_notifier.dart';
import '../features/profile/weekly_summary_notifier.dart';
import '../features/progress/progress_notifier.dart';
import '../features/rivalry/ranking_notifier.dart';
import '../features/steps/steps_notifier.dart';
import '../features/tasks/tasks_notifier.dart';
import '../features/techhealth/tech_health_notifier.dart';
import '../l10n/app_localizations.dart';

class NoInternetScreen extends ConsumerStatefulWidget {
  const NoInternetScreen({super.key});

  @override
  ConsumerState<NoInternetScreen> createState() => _NoInternetScreenState();
}

class _NoInternetScreenState extends ConsumerState<NoInternetScreen> {
  bool _checking = false;

  Future<void> _retry() async {
    setState(() => _checking = true);
    final hasInternet = await ref.read(connectivityServiceProvider).hasInternetConnection();
    if (hasInternet) {
      ref.read(isOfflineProvider.notifier).setOffline(false);
      _refreshIfAlive(tasksNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(stepsNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(rankingNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(profileStatsNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(weeklySummaryNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(progressNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(friendsNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(notificationsNotifierProvider, (n) => n.refresh());
      _refreshIfAlive(techHealthNotifierProvider, (n) => n.refresh());
    }
    if (mounted) setState(() => _checking = false);
  }

  void _refreshIfAlive<N extends Notifier<S>, S>(NotifierProvider<N, S> provider, void Function(N) refresh) {
    if (ref.exists(provider)) {
      refresh(ref.read(provider.notifier));
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Material(
      color: Theme.of(context).colorScheme.surface,
      child: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: DayPilotEmptyState(
                icon: Icons.wifi_off_rounded,
                title: l10n.noInternetTitle,
                subtitle: l10n.noInternetSubtitle,
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(bottom: 48),
              child: SizedBox(
                width: 200,
                child: DayPilotButton(
                  label: l10n.noInternetRetry,
                  isLoading: _checking,
                  onPressed: _retry,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
