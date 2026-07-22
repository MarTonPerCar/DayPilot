import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../screens/auth/login_screen.dart';
import '../../screens/main_shell.dart';
import '../connectivity/connectivity_service.dart';
import '../connectivity/offline_notifier.dart';

final authStateChangesProvider = StreamProvider<AuthState>((ref) {
  return Supabase.instance.client.auth.onAuthStateChange;
});

class AuthGate extends ConsumerStatefulWidget {
  const AuthGate({super.key});

  @override
  ConsumerState<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends ConsumerState<AuthGate> {
  @override
  void initState() {
    super.initState();
    Future.microtask(_checkInitialConnectivity);
  }

  Future<void> _checkInitialConnectivity() async {
    final hasInternet = await ref.read(connectivityServiceProvider).hasInternetConnection();
    if (!hasInternet && mounted) {
      ref.read(isOfflineProvider.notifier).setOffline(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authStateChangesProvider);
    return authState.when(
      data: (state) => state.session != null ? const MainShell() : const LoginScreen(),
      loading: () => const _AuthLoadingScreen(),
      error: (error, _) {
        if (isConnectivityError(error)) {
          Future.microtask(() => ref.read(isOfflineProvider.notifier).setOffline(true));
        }
        return const LoginScreen();
      },
    );
  }
}

class _AuthLoadingScreen extends StatelessWidget {
  const _AuthLoadingScreen();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(body: Center(child: CircularProgressIndicator()));
  }
}
