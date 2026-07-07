import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/cards/friend_card.dart';
import '../../core/data/models/app_friend.dart';
import '../../features/friends/friends_notifier.dart';
import '../../features/rivalry/ranking_notifier.dart';
import '../../l10n/app_localizations.dart';
import 'search_friends_screen.dart';

class FriendsScreen extends ConsumerStatefulWidget {
  const FriendsScreen({super.key});

  @override
  ConsumerState<FriendsScreen> createState() => _FriendsScreenState();
}

class _FriendsScreenState extends ConsumerState<FriendsScreen> with SingleTickerProviderStateMixin {
  late final TabController _tabController = TabController(length: 2, vsync: this);

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(friendsNotifierProvider);
    final requests = state.requests;

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 8, 0),
              child: Row(
                children: [
                  Expanded(
                    child: Text(
                      l10n.navAmigos,
                      style: text.headlineMedium?.copyWith(fontWeight: FontWeight.w800),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.person_add_rounded),
                    onPressed: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const SearchFriendsScreen()),
                    ),
                  ),
                ],
              ),
            ),
            TabBar(
              controller: _tabController,
              labelColor: colors.primary,
              unselectedLabelColor: colors.onSurfaceVariant,
              indicatorColor: colors.primary,
              tabs: [
                Tab(text: l10n.navAmigos),
                Tab(
                  text: requests.isNotEmpty
                      ? l10n.friendsRequestsTabCount(requests.length)
                      : l10n.friendsRequestsTab,
                ),
              ],
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [_buildFriends(state), _buildRequests(context, requests)],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFriends(FriendsState state) {
    if (state.friends.isEmpty) {
      return Center(child: Text(AppLocalizations.of(context).friendsNoFriends));
    }
    return RefreshIndicator(
      onRefresh: () async {
        await ref.read(friendsNotifierProvider.notifier).refresh();
        await ref.read(rankingNotifierProvider.notifier).refresh();
      },
      child: ListView.separated(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
        itemCount: state.friends.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (ctx, i) {
          final f = state.friends[i];
          return FriendCard(
            name: f.name,
            email: f.username,
            avatarUrl: f.avatarUrl,
            points: f.points,
            streak: f.streak,
            weeklyPoints: f.weeklyPoints,
            weeklyTasks: f.weeklyTasks,
            weeklySteps: f.weeklySteps,
            weeklyStreak: f.weeklyStreak,
            reactionSelected: f.reactionSelected,
            onReact: f.weeklySummaryId == null
                ? null
                : (emoji) => ref.read(friendsNotifierProvider.notifier).react(f, emoji),
            onRemove: () => ref.read(friendsNotifierProvider.notifier).removeFriend(f.friendRowId),
          );
        },
      ),
    );
  }

  Widget _buildRequests(BuildContext context, List<AppFriendRequest> requests) {
    if (requests.isEmpty) {
      return Center(child: Text(AppLocalizations.of(context).friendsNoRequests));
    }
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      itemCount: requests.length,
      separatorBuilder: (_, _) => const SizedBox(height: 12),
      itemBuilder: (ctx, i) {
        final r = requests[i];
        return FriendRequestCard(
          name: r.name,
          email: r.username,
          avatarUrl: r.avatarUrl,
          onAccept: () => ref.read(friendsNotifierProvider.notifier).acceptRequest(r),
          onDecline: () => ref.read(friendsNotifierProvider.notifier).declineRequest(r.requestId),
        );
      },
    );
  }
}
