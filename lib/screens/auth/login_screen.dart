import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/button.dart';
import '../../components/basic/text_field.dart';
import '../../components/forms/select_field.dart';
import '../../data/app_data.dart';
import '../../features/auth/auth_error.dart';
import '../../features/auth/auth_notifier.dart';
import '../../features/auth/auth_session.dart';
import '../../l10n/app_localizations.dart';
import '../main_shell.dart';
import 'forgot_password_screen.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  bool _showRegister = false;
  bool _registerLoading = false;
  String? _timezone = AppData.timezoneOptions.first;
  final _loginEmailController = TextEditingController();
  final _loginPasswordController = TextEditingController();

  @override
  void dispose() {
    _loginEmailController.dispose();
    _loginPasswordController.dispose();
    super.dispose();
  }

  Future<void> _submitLogin() async {
    await ref.read(authNotifierProvider.notifier).login(
          email: _loginEmailController.text.trim(),
          password: _loginPasswordController.text,
        );
    if (!mounted) return;
    if (ref.read(authNotifierProvider).status == AuthStatus.authenticated) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainShell()),
      );
    }
  }

  void _submitRegister() async {
    setState(() => _registerLoading = true);
    await Future.delayed(const Duration(milliseconds: 800));
    if (!mounted) return;
    setState(() => _registerLoading = false);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final session = ref.watch(authNotifierProvider);

    ref.listen(authNotifierProvider, (previous, next) {
      if (next.status == AuthStatus.unauthenticated && next.error != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(friendlyAuthError(next.error!, l10n))),
        );
      }
    });

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 32),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    width: 80,
                    height: 80,
                    decoration: BoxDecoration(
                      color: colors.primaryContainer,
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: Icon(
                      Icons.flight_takeoff_rounded,
                      size: 44,
                      color: colors.onPrimaryContainer,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'DayPilot',
                    style: text.headlineMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                      color: colors.onSurface,
                    ),
                  ),
                  Text(
                    l10n.loginTagline,
                    style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                  ),
                  const SizedBox(height: 28),

                  _AuthToggle(
                    showRegister: _showRegister,
                    onChanged: (v) => setState(() => _showRegister = v),
                  ),
                  const SizedBox(height: 20),

                  Card(
                    elevation: 3,
                    shadowColor: colors.shadow.withAlpha(60),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                    clipBehavior: Clip.hardEdge,
                    margin: EdgeInsets.zero,
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: _FlipCard(
                        showBack: _showRegister,
                        front: _LoginForm(
                          loading: session.status == AuthStatus.authenticating,
                          emailController: _loginEmailController,
                          passwordController: _loginPasswordController,
                          onSubmit: _submitLogin,
                        ),
                        back: _RegisterForm(
                          loading: _registerLoading,
                          timezone: _timezone,
                          onTimezoneChanged: (v) => setState(() => _timezone = v),
                          onSubmit: _submitRegister,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _AuthToggle extends StatelessWidget {
  final bool showRegister;
  final ValueChanged<bool> onChanged;

  const _AuthToggle({required this.showRegister, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final l10n = AppLocalizations.of(context);

    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: colors.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(30),
      ),
      child: Row(
        children: [
          _segment(context, l10n.loginSignInTab, !showRegister, () => onChanged(false)),
          _segment(context, l10n.loginSignUpTab, showRegister, () => onChanged(true)),
        ],
      ),
    );
  }

  Widget _segment(BuildContext context, String label, bool active, VoidCallback onTap) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeOut,
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: active ? colors.primaryContainer : Colors.transparent,
            borderRadius: BorderRadius.circular(26),
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: text.labelLarge?.copyWith(
              fontWeight: FontWeight.w700,
              color: active ? colors.onPrimaryContainer : colors.onSurfaceVariant,
            ),
          ),
        ),
      ),
    );
  }
}

class _FlipCard extends StatefulWidget {
  final bool showBack;
  final Widget front;
  final Widget back;

  const _FlipCard({required this.showBack, required this.front, required this.back});

  @override
  State<_FlipCard> createState() => _FlipCardState();
}

class _FlipCardState extends State<_FlipCard> with SingleTickerProviderStateMixin {
  late final _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 500),
    value: widget.showBack ? 1 : 0,
  );

  @override
  void didUpdateWidget(covariant _FlipCard oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.showBack != oldWidget.showBack) {
      widget.showBack ? _controller.forward() : _controller.reverse();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, _) {
        final angle = _controller.value * math.pi;
        final isBack = angle > math.pi / 2;
        return Transform(
          alignment: Alignment.center,
          transform: Matrix4.identity()
            ..setEntry(3, 2, 0.001)
            ..rotateY(isBack ? angle - math.pi : angle),
          child: AnimatedSize(
            duration: const Duration(milliseconds: 200),
            alignment: Alignment.topCenter,
            child: isBack ? widget.back : widget.front,
          ),
        );
      },
    );
  }
}

class _LoginForm extends StatelessWidget {
  final bool loading;
  final TextEditingController emailController;
  final TextEditingController passwordController;
  final VoidCallback onSubmit;

  const _LoginForm({
    required this.loading,
    required this.emailController,
    required this.passwordController,
    required this.onSubmit,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(l10n.loginSignInTab, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 20),
        DayPilotTextField(
          label: l10n.profileInfoEmail,
          controller: emailController,
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 14),
        DayPilotPasswordField(label: l10n.commonPassword, controller: passwordController),
        const SizedBox(height: 8),
        Align(
          alignment: Alignment.centerRight,
          child: TextButton(
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const ForgotPasswordScreen()),
            ),
            child: Text(l10n.loginForgotPassword),
          ),
        ),
        const SizedBox(height: 12),
        DayPilotButton(label: l10n.loginSubmit, isLoading: loading, onPressed: onSubmit),
      ],
    );
  }
}

class _RegisterForm extends StatelessWidget {
  final bool loading;
  final String? timezone;
  final ValueChanged<String> onTimezoneChanged;
  final VoidCallback onSubmit;

  const _RegisterForm({
    required this.loading,
    required this.timezone,
    required this.onTimezoneChanged,
    required this.onSubmit,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(l10n.loginSignUpTab, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 20),
        DayPilotTextField(label: l10n.loginNameLabel),
        const SizedBox(height: 14),
        DayPilotTextField(label: l10n.loginUsernameLabel),
        const SizedBox(height: 14),
        DayPilotSelectField<String>(
          label: l10n.loginTimezoneLabel,
          value: timezone,
          options: AppData.timezoneOptions,
          display: (s) => s,
          onChanged: onTimezoneChanged,
        ),
        const SizedBox(height: 14),
        DayPilotTextField(
          label: l10n.profileInfoEmail,
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 14),
        DayPilotPasswordField(label: l10n.commonPassword),
        const SizedBox(height: 20),
        DayPilotButton(label: l10n.loginRegisterSubmit, isLoading: loading, onPressed: onSubmit),
      ],
    );
  }
}
