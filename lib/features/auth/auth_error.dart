import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/models/auth_exceptions.dart';
import '../../l10n/app_localizations.dart';

class EmptyCredentialsError implements Exception {
  const EmptyCredentialsError();
}

class EmptyRegisterFieldsError implements Exception {
  const EmptyRegisterFieldsError();
}

String friendlyAuthError(Object error, AppLocalizations l10n) {
  if (error is EmptyCredentialsError) {
    return l10n.authErrorInvalidCredentials;
  }
  if (error is EmptyRegisterFieldsError) {
    return l10n.authErrorFillAllFields;
  }
  if (error is EmailConfirmationRequiredError) {
    return l10n.authRegisterCheckEmail;
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
