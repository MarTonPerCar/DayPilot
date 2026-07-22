import 'package:flutter_riverpod/flutter_riverpod.dart';

class OfflineNotifier extends Notifier<bool> {
  @override
  bool build() => false;

  void setOffline(bool value) {
    if (state != value) state = value;
  }
}

final isOfflineProvider = NotifierProvider<OfflineNotifier, bool>(OfflineNotifier.new);
