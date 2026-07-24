import 'dart:io';

import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/connectivity/offline_notifier.dart';
import 'package:daypilot/core/data/models/app_ranking_entry.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/rivalry/ranking_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockRankingRepository rankingRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  const entry = AppRankingEntry(
    userId: 'u1',
    name: 'Ana',
    username: 'ana',
    points: 100,
    streak: 3,
    isCurrentUser: true,
  );

  setUp(() {
    rankingRepo = MockRankingRepository();
    connectivity = FakeConnectivityService();
    container = ProviderContainer(
      overrides: [
        rankingRepositoryProvider.overrideWithValue(rankingRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('refresh populates state from the repository', () async {
    when(() => rankingRepo.getRanking()).thenAnswer((_) async => [entry]);

    await container.read(rankingNotifierProvider.notifier).refresh();

    expect(container.read(rankingNotifierProvider), [entry]);
  });

  test('refresh while offline never calls the repository and leaves state empty', () async {
    connectivity.online = false;

    await container.read(rankingNotifierProvider.notifier).refresh();

    expect(container.read(rankingNotifierProvider), isEmpty);
    verifyNever(() => rankingRepo.getRanking());
  });

  test('a connectivity error while refreshing flips the app into offline mode', () async {
    when(() => rankingRepo.getRanking()).thenThrow(const SocketException('no route'));

    await container.read(rankingNotifierProvider.notifier).refresh();

    expect(container.read(isOfflineProvider), isTrue);
  });
}
