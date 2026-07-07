import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/text_field.dart';
import '../../components/cards/friend_card.dart';
import '../../core/data/models/app_friend.dart';
import '../../core/data/repositories/providers.dart';
import '../../l10n/app_localizations.dart';

class SearchFriendsScreen extends ConsumerStatefulWidget {
  const SearchFriendsScreen({super.key});

  @override
  ConsumerState<SearchFriendsScreen> createState() => _SearchFriendsScreenState();
}

class _SearchFriendsScreenState extends ConsumerState<SearchFriendsScreen> {
  final _queryController = TextEditingController();
  Timer? _debounce;
  List<AppUserSearchResult> _results = const [];
  bool _searching = false;

  @override
  void dispose() {
    _debounce?.cancel();
    _queryController.dispose();
    super.dispose();
  }

  void _onQueryChanged(String query) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 400), () => _search(query));
  }

  Future<void> _search(String query) async {
    if (query.trim().isEmpty) {
      setState(() => _results = const []);
      return;
    }
    setState(() => _searching = true);
    final results = await ref.read(friendsRepositoryProvider).searchUsers(query);
    if (!mounted) return;
    setState(() {
      _results = results;
      _searching = false;
    });
  }

  Future<void> _addFriend(AppUserSearchResult result) async {
    setState(() {
      _results = [
        for (final r in _results)
          if (r.userId == result.userId)
            AppUserSearchResult(
              userId: r.userId,
              name: r.name,
              username: r.username,
              avatarUrl: r.avatarUrl,
              isFriend: r.isFriend,
              isPending: true,
            )
          else
            r,
      ];
    });
    await ref.read(friendsRepositoryProvider).sendFriendRequest(result.userId);
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.searchFriendsTitle, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          DayPilotTextField(
            label: l10n.searchFriendsHint,
            prefixIcon: Icons.search_rounded,
            controller: _queryController,
            onChanged: _onQueryChanged,
          ),
          const SizedBox(height: 16),
          if (_searching) const Center(child: CircularProgressIndicator()),
          ..._results.map((r) => Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: UserSearchCard(
                  name: r.name,
                  email: r.username,
                  avatarUrl: r.avatarUrl,
                  isFriend: r.isFriend,
                  isPending: r.isPending,
                  onAdd: () => _addFriend(r),
                ),
              )),
        ],
      ),
    );
  }
}
