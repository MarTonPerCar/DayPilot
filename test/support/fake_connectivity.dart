import 'package:daypilot/core/connectivity/connectivity_service.dart';

/// Swaps out the real [ConnectivityService], which does a live DNS lookup,
/// for a settable in-memory flag — tests control "online"/"offline" directly
/// instead of depending on the test runner's actual network state.
class FakeConnectivityService implements ConnectivityService {
  bool online = true;

  @override
  Future<bool> hasInternetConnection() async => online;
}
