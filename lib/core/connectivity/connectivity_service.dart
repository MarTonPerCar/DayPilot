import 'dart:async';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'offline_notifier.dart';

class ConnectivityService {
  const ConnectivityService();

  Future<bool> hasInternetConnection() async {
    try {
      final result = await InternetAddress.lookup('one.one.one.one').timeout(const Duration(seconds: 4));
      return result.isNotEmpty && result.first.rawAddress.isNotEmpty;
    } on SocketException {
      return false;
    } on TimeoutException {
      return false;
    }
  }
}

final connectivityServiceProvider = Provider<ConnectivityService>((ref) => const ConnectivityService());

bool isConnectivityError(Object error) {
  return error is SocketException || error is TimeoutException || error is AuthRetryableFetchException;
}

/// Actively probes for a real connection before a notifier attempts a network call, instead of
/// only reacting after the call fails. A dead/blackholed connection can leave an http request
/// hanging or silently succeeding later once connectivity returns, rather than throwing promptly.
Future<bool> ensureOnline(Ref ref) async {
  final hasInternet = await ref.read(connectivityServiceProvider).hasInternetConnection();
  if (!hasInternet) {
    ref.read(isOfflineProvider.notifier).setOffline(true);
  }
  return hasInternet;
}

/// Same as [ensureOnline], for call sites (screens) that only have a [WidgetRef].
Future<bool> ensureOnlineFromWidget(WidgetRef ref) async {
  final hasInternet = await ref.read(connectivityServiceProvider).hasInternetConnection();
  if (!hasInternet) {
    ref.read(isOfflineProvider.notifier).setOffline(true);
  }
  return hasInternet;
}
