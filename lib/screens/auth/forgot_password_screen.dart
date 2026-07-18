import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../components/basic/button.dart';
import '../../components/basic/text_field.dart';
import '../../components/basic/top_bar.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../../l10n/app_localizations.dart';

class ForgotPasswordScreen extends ConsumerStatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  ConsumerState<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends ConsumerState<ForgotPasswordScreen> {
  final _emailController = TextEditingController();
  bool _loading = false;
  bool _sent = false;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  bool _isValidEmail(String value) => RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(value);

  Future<void> _send() async {
    final l10n = AppLocalizations.of(context);
    final email = _emailController.text.trim();

    if (!_isValidEmail(email)) {
      setState(() => _error = l10n.authErrorInvalidEmail);
      return;
    }

    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      await ref.read(authRepositoryProvider).sendPasswordResetEmail(email);
      if (!mounted) return;
      setState(() {
        _loading = false;
        _sent = true;
      });
    } catch (e, st) {
      AppLogger.logError('ForgotPasswordScreen.send', e, st);
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = l10n.authErrorUnknown;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.forgotPasswordTitle, showBack: true),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Column(
                children: [
                  const SizedBox(height: 20),
                  Container(
                    width: 88,
                    height: 88,
                    decoration: BoxDecoration(
                      color: colors.primaryContainer,
                      borderRadius: BorderRadius.circular(24),
                    ),
                    alignment: Alignment.center,
                    child: const Text('🔐', style: TextStyle(fontSize: 36)),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    _sent ? l10n.forgotPasswordSentBody : l10n.forgotPasswordBody,
                    textAlign: TextAlign.center,
                    style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                  ),
                  const SizedBox(height: 28),
                  if (!_sent) ...[
                    DayPilotTextField(
                      label: l10n.profileInfoEmail,
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      errorText: _error,
                    ),
                    const SizedBox(height: 20),
                    DayPilotButton(label: l10n.forgotPasswordSendButton, isLoading: _loading, onPressed: _send),
                  ],
                  const SizedBox(height: 16),
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: Text(l10n.forgotPasswordBackToLogin),
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
