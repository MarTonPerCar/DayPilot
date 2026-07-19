import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'repositories/providers.dart';

class FriendStatsBroadcast {
  FriendStatsBroadcast(this._ref);

  final Ref _ref;
  RealtimeChannel? _channel;
  final _listeners = <void Function()>[];

  Future<void> addListener(void Function() onChange) async {
    _listeners.add(onChange);
    await _ensureSubscribed();
  }

  void removeListener(void Function() onChange) {
    _listeners.remove(onChange);
  }

  Future<void> _ensureSubscribed() async {
    if (_channel != null) return;
    final client = _ref.read(supabaseClientProvider);
    final uid = client.auth.currentUser?.id;
    if (uid == null) return;

    await client.realtime.setAuth(client.auth.currentSession?.accessToken);

    void notifyAll() {
      for (final l in List.of(_listeners)) {
        l();
      }
    }

    _channel = client
        .channel('friend-stats:$uid', opts: const RealtimeChannelConfig(private: true))
        .onBroadcast(event: 'INSERT', callback: (payload) => notifyAll())
        .onBroadcast(event: 'UPDATE', callback: (payload) => notifyAll())
        .onBroadcast(event: 'DELETE', callback: (payload) => notifyAll())
        .subscribe();
  }
}

final friendStatsBroadcastProvider = Provider<FriendStatsBroadcast>((ref) {
  return FriendStatsBroadcast(ref);
});
