import 'package:flutter/material.dart';
import '../../components/basic/text_field.dart';
import '../../components/basic/button.dart';
import '../../components/basic/divider.dart';
import '../main_shell.dart';
import 'register_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  bool _loading = false;

  void _login() async {
    setState(() => _loading = true);
    await Future.delayed(const Duration(milliseconds: 800));
    if (!mounted) return;
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => const MainShell()),
    );
  }

  void _demo() {
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => const MainShell()),
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

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
                  // Logo
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
                  const SizedBox(height: 20),
                  Text(
                    'DayPilot',
                    style: text.headlineMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                      color: colors.onSurface,
                    ),
                  ),
                  Text(
                    'Vuela hacia tus metas',
                    style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                  ),
                  const SizedBox(height: 40),

                  // Form
                  const DayPilotTextField(
                    label: 'Correo electrónico',
                    hint: 'tu@correo.com',
                    prefixIcon: Icons.email_outlined,
                    keyboardType: TextInputType.emailAddress,
                  ),
                  const SizedBox(height: 14),
                  const DayPilotPasswordField(label: 'Contraseña'),
                  const SizedBox(height: 8),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton(
                      onPressed: () {},
                      child: const Text('¿Olvidaste tu contraseña?'),
                    ),
                  ),
                  const SizedBox(height: 20),

                  DayPilotButton(
                    label: 'Iniciar sesión',
                    isLoading: _loading,
                    onPressed: _login,
                  ),
                  const SizedBox(height: 16),

                  const DayPilotDivider(label: 'o'),
                  const SizedBox(height: 16),

                  DayPilotButton(
                    label: 'Crear cuenta',
                    variant: DayPilotButtonVariant.outline,
                    onPressed: () => Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => const RegisterScreen()),
                    ),
                  ),
                  const SizedBox(height: 40),

                  TextButton.icon(
                    onPressed: _demo,
                    icon: const Icon(Icons.play_arrow_rounded, size: 18),
                    label: const Text('Entrar en modo demo'),
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
