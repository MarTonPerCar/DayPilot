import 'package:daypilot/core/data/models/app_notification_item.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/notifications/notifications_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockNotificationsRepository notificationsRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  final itemA = AppNotificationItem(
    id: 'n1',
    type: AppNotificationType.friendAccepted,
    title: 'Carlos accepted your request',
    body: '',
    isRead: false,
    createdAt: DateTime(2026, 7, 24),
  );
  final itemB = AppNotificationItem(
    id: 'n2',
    type: AppNotificationType.levelUp,
    title: 'Level up!',
    body: '',
    isRead: false,
    createdAt: DateTime(2026, 7, 24),
  );

  setUp(() {
    notificationsRepo = MockNotificationsRepository();
    connectivity = FakeConnectivityService();
    when(() => notificationsRepo.getNotifications()).thenAnswer((_) async => [itemA, itemB]);
    container = ProviderContainer(
      overrides: [
        notificationsRepositoryProvider.overrideWithValue(notificationsRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  test('refresh populates state from the repository', () async {
    await container.read(notificationsNotifierProvider.notifier).refresh();

    expect(container.read(notificationsNotifierProvider), [itemA, itemB]);
  });

  test('markAsRead flips only the matching notification', () async {
    await container.read(notificationsNotifierProvider.notifier).refresh();
    when(() => notificationsRepo.markAsRead('n1')).thenAnswer((_) async {});

    await container.read(notificationsNotifierProvider.notifier).markAsRead('n1');

    final state = container.read(notificationsNotifierProvider);
    expect(state.firstWhere((n) => n.id == 'n1').isRead, isTrue);
    expect(state.firstWhere((n) => n.id == 'n2').isRead, isFalse);
  });

  test('markAllAsRead flips every notification', () async {
    await container.read(notificationsNotifierProvider.notifier).refresh();
    when(() => notificationsRepo.markAllAsRead()).thenAnswer((_) async {});

    await container.read(notificationsNotifierProvider.notifier).markAllAsRead();

    final state = container.read(notificationsNotifierProvider);
    expect(state.every((n) => n.isRead), isTrue);
  });
}
