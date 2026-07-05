import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../screens/auth/login_screen.dart';
import '../../screens/main_shell.dart';

/// Supabase restores a persisted session from local storage asynchronously
/// on startup; this decides the initial screen (login vs. already signed
/// in), mirroring the Android app's `AppSessionViewModel`. The interactive
/// login flow itself navigates directly (see `LoginScreen._submitLogin`)
/// rather than relying on this rebuilding — it only needs to fire once per
/// cold start.
final authStateChangesProvider = StreamProvider<AuthState>((ref) {
  return Supabase.instance.client.auth.onAuthStateChange;
});

/// App root once past `main()`: shows the login screen or the app shell
/// depending on session state, with a brief loading screen while Supabase
/// checks local storage for a persisted session.
class AuthGate extends ConsumerWidget {
  const AuthGate({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authStateChangesProvider);
    return authState.when(
      data: (state) => state.session != null ? const MainShell() : const LoginScreen(),
      loading: () => const _AuthLoadingScreen(),
      error: (_, _) => const LoginScreen(),
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
