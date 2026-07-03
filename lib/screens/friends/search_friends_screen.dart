import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/text_field.dart';
import '../../components/cards/friend_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class SearchFriendsScreen extends StatelessWidget {
  const SearchFriendsScreen({super.key});

  static const _results = AppData.friendSearchResults;

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
          ),
          const SizedBox(height: 16),
          ..._results.map((r) => Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: UserSearchCard(
                  name: r.name,
                  email: r.email,
                  isFriend: r.isFriend,
                  isPending: r.isPending,
                  onAdd: () {},
                ),
              )),
        ],
      ),
    );
  }
}
