import 'package:flutter/material.dart';
import '../../components/cards/friend_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import 'search_friends_screen.dart';

class FriendsScreen extends StatefulWidget {
  const FriendsScreen({super.key});

  @override
  State<FriendsScreen> createState() => _FriendsScreenState();
}

class _FriendsScreenState extends State<FriendsScreen> with SingleTickerProviderStateMixin {
  late final TabController _tabController = TabController(length: 2, vsync: this);

  static const _friends = AppData.friends;
  static const _requests = AppData.friendRequests;

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
                Tab(text: _requests.isNotEmpty
                    ? l10n.friendsRequestsTabCount(_requests.length)
                    : l10n.friendsRequestsTab),
              ],
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [_buildFriends(), _buildRequests(context)],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFriends() {
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      itemCount: _friends.length,
      separatorBuilder: (_, _) => const SizedBox(height: 12),
      itemBuilder: (ctx, i) {
        final f = _friends[i];
        return FriendCard(
          name: f.name,
          email: f.email,
          points: f.points,
          streak: f.streak,
          weeklyPoints: f.weeklyPoints,
          weeklyTasks: f.weeklyTasks,
          weeklySteps: f.weeklySteps,
          weeklyStreak: f.weeklyStreak,
          reactionSelected: f.reactionSelected,
          onReact: (emoji) {},
          onRemove: () {},
        );
      },
    );
  }

  Widget _buildRequests(BuildContext context) {
    if (_requests.isEmpty) {
      return Center(child: Text(AppLocalizations.of(context).friendsNoRequests));
    }
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      itemCount: _requests.length,
      separatorBuilder: (_, _) => const SizedBox(height: 12),
      itemBuilder: (ctx, i) {
        final r = _requests[i];
        return FriendRequestCard(
          name: r.name,
          email: r.email,
          onAccept: () {},
          onDecline: () {},
        );
      },
    );
  }
}
