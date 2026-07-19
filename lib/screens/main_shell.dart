import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/window/desktop_notifications.dart';
import '../features/notifications/notifications_notifier.dart';
import '../l10n/app_localizations.dart';
import 'home/home_screen.dart';
import 'friends/friends_screen.dart';
import 'notifications/notifications_screen.dart';
import 'profile/profile_screen.dart';

class MainShell extends ConsumerStatefulWidget {
  const MainShell({super.key});

  @override
  ConsumerState<MainShell> createState() => _MainShellState();
}

class _MainShellState extends ConsumerState<MainShell> {
  int _index = 0;

  static const _tabs = [
    HomeScreen(),
    FriendsScreen(),
    NotificationsScreen(),
    ProfileScreen(),
  ];

  @override
  void initState() {
    super.initState();

    startDesktopDailyNotifications();
  }

  @override
  void dispose() {
    stopDesktopDailyNotifications();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final unreadCount = ref.watch(notificationsNotifierProvider).where((n) => !n.isRead).length;

    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: _tabs,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: [
          NavigationDestination(
            icon: const Icon(Icons.home_outlined),
            selectedIcon: const Icon(Icons.home_rounded),
            label: l10n.navInicio,
          ),
          NavigationDestination(
            icon: const Icon(Icons.group_outlined),
            selectedIcon: const Icon(Icons.group_rounded),
            label: l10n.navAmigos,
          ),
          NavigationDestination(
            icon: Badge.count(
              count: unreadCount,
              isLabelVisible: unreadCount > 0,
              child: const Icon(Icons.notifications_outlined),
            ),
            selectedIcon: Badge.count(
              count: unreadCount,
              isLabelVisible: unreadCount > 0,
              child: const Icon(Icons.notifications_rounded),
            ),
            label: l10n.navAvisos,
          ),
          NavigationDestination(
            icon: const Icon(Icons.person_outline_rounded),
            selectedIcon: const Icon(Icons.person_rounded),
            label: l10n.navPerfil,
          ),
        ],
      ),
    );
  }
}
