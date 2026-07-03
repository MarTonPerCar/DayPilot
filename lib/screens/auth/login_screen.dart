import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../../components/basic/button.dart';
import '../../components/basic/text_field.dart';
import '../../components/forms/select_field.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../main_shell.dart';
import 'forgot_password_screen.dart';

/// Pantalla de acceso: login y registro son dos caras de una misma tarjeta
/// que gira en 3D al cambiar el interruptor de arriba.
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  bool _showRegister = false;
  bool _loading = false;
  String? _timezone = AppData.timezoneOptions.first;

  void _submit() async {
    setState(() => _loading = true);
    await Future.delayed(const Duration(milliseconds: 800));
    if (!mounted) return;
    Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const MainShell()));
  }

  void _demo() {
    Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const MainShell()));
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

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
                        front: _LoginForm(loading: _loading, onSubmit: _submit),
                        back: _RegisterForm(
                          loading: _loading,
                          timezone: _timezone,
                          onTimezoneChanged: (v) => setState(() => _timezone = v),
                          onSubmit: _submit,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 28),

                  TextButton.icon(
                    onPressed: _demo,
                    icon: const Icon(Icons.play_arrow_rounded, size: 18),
                    label: Text(l10n.loginDemoMode),
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

/// Interruptor tipo pill "Iniciar sesión" / "Crear cuenta" con franja que
/// se desliza al lado activo.
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

/// Tarjeta que gira en 3D (eje Y) entre [front] y [back] cuando cambia
/// [showBack]; se redimensiona con suavidad si las caras tienen alturas
/// distintas.
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
  final VoidCallback onSubmit;

  const _LoginForm({required this.loading, required this.onSubmit});

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
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 14),
        DayPilotPasswordField(label: l10n.commonPassword),
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
