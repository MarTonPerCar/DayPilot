import 'package:supabase_flutter/supabase_flutter.dart';

import '../../l10n/app_localizations.dart';

/// Never reaches the network — caught before calling Supabase.
class EmptyCredentialsError implements Exception {
  const EmptyCredentialsError();
}

String friendlyAuthError(Object error, AppLocalizations l10n) {
  if (error is EmptyCredentialsError) {
    return l10n.authErrorInvalidCredentials;
  }
  final raw = error is AuthException ? error.message : error.toString();
  final lower = raw.toLowerCase();
  if (lower.contains('invalid login credentials')) {
    return l10n.authErrorInvalidCredentials;
  }
  if (lower.contains('email not confirmed')) {
    return l10n.authErrorEmailNotConfirmed;
  }
  if (lower.contains('user already registered')) {
    return l10n.authErrorAlreadyRegistered;
  }
  if (lower.contains('password should be')) {
    return l10n.authErrorWeakPassword;
  }
  if (lower.contains('unable to validate email')) {
    return l10n.authErrorInvalidEmail;
  }
  return l10n.authErrorUnknown;
}
