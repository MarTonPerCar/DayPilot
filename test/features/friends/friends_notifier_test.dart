import 'package:daypilot/core/data/models/app_friend.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/friends/friends_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockFriendsRepository friendsRepo;
  late MockRankingRepository rankingRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  const incomingRequest = AppFriendRequest(requestId: 'r1', fromUserId: 'u2', name: 'Carlos', username: 'carlos');
  const acceptedFriend = AppFriend(
    friendRowId: 'row1',
    userId: 'u2',
    name: 'Carlos',
    username: 'carlos',
    points: 50,
    streak: 2,
  );

  setUp(() {
    friendsRepo = MockFriendsRepository();
    rankingRepo = MockRankingRepository();
    connectivity = FakeConnectivityService();
    when(() => friendsRepo.getFriends()).thenAnswer((_) async => []);
    when(() => friendsRepo.getIncomingRequests()).thenAnswer((_) async => [incomingRequest]);
    when(() => rankingRepo.getRanking()).thenAnswer((_) async => []);
    container = ProviderContainer(
      overrides: [
        friendsRepositoryProvider.overrideWithValue(friendsRepo),
        rankingRepositoryProvider.overrideWithValue(rankingRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('accepting a request refreshes into the new friends/requests lists', () async {
    await container.read(friendsNotifierProvider.notifier).refresh();
    expect(container.read(friendsNotifierProvider).requests, [incomingRequest]);

    when(() => friendsRepo.acceptRequest(requestId: 'r1', fromUserId: 'u2')).thenAnswer((_) async {});
    when(() => friendsRepo.getFriends()).thenAnswer((_) async => [acceptedFriend]);
    when(() => friendsRepo.getIncomingRequests()).thenAnswer((_) async => []);

    await container.read(friendsNotifierProvider.notifier).acceptRequest(incomingRequest);

    final state = container.read(friendsNotifierProvider);
    expect(state.friends, [acceptedFriend]);
    expect(state.requests, isEmpty);
  });

  test('declining a request that fails server-side leaves the request list untouched', () async {
    await container.read(friendsNotifierProvider.notifier).refresh();

    when(() => friendsRepo.declineRequest('r1')).thenThrow(Exception('decline failed'));

    await container.read(friendsNotifierProvider.notifier).declineRequest('r1');

    expect(container.read(friendsNotifierProvider).requests, [incomingRequest]);
  });

  test('removing a friend also refreshes the ranking, since points recompute together', () async {
    await container.read(friendsNotifierProvider.notifier).refresh();

    when(() => friendsRepo.removeFriend('row1')).thenAnswer((_) async {});

    await container.read(friendsNotifierProvider.notifier).removeFriend('row1');

    verify(() => rankingRepo.getRanking()).called(greaterThanOrEqualTo(1));
  });
}
