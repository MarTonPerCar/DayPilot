import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_tech_restriction.dart';
import 'tech_health_repository.dart';

class SupabaseTechHealthRepository implements TechHealthRepository {
  SupabaseTechHealthRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<List<AppTechRestriction>> getRestrictions() async {
    final uid = _userId;
    if (uid == null) return [];

    final rows = await _client
        .from('tech_health_config')
        .select('app_package, app_name, limit_hours, is_active')
        .eq('user_id', uid)
        .eq('pending_delete', false)
        .order('created_at');

    return [
      for (final r in rows)
        AppTechRestriction(
          appPackage: r['app_package'] as String,
          appName: r['app_name'] as String,
          limitMinutes: ((r['limit_hours'] as num) * 60).round(),
          isActive: r['is_active'] as bool,
        ),
    ];
  }

  @override
  Future<void> saveRestriction({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  }) async {
    final uid = _userId;
    if (uid == null) return;

    await _client.from('tech_health_config').upsert(
      {
        'user_id': uid,
        'app_package': appPackage,
        'app_name': appName,
        'limit_hours': limitMinutes / 60.0,
        'is_active': true,
      },
      onConflict: 'user_id, app_package',
    );

    await _client
        .from('tech_health_config')
        .update({'pending_delete': false})
        .eq('user_id', uid)
        .eq('app_package', appPackage);
  }

  @override
  Future<void> toggleRestriction(String appPackage, bool isActive) async {
    final uid = _userId;
    if (uid == null) return;
    await _client
        .from('tech_health_config')
        .update({'is_active': isActive})
        .eq('user_id', uid)
        .eq('app_package', appPackage);
  }

  @override
  Future<void> deleteRestriction(String appPackage) async {
    final uid = _userId;
    if (uid == null) return;
    await _client
        .from('tech_health_config')
        .update({'pending_delete': true})
        .eq('user_id', uid)
        .eq('app_package', appPackage);
  }

  @override
  Future<bool> getPointEarnedToday() async {
    final uid = _userId;
    if (uid == null) return false;

    final rows = await _client
        .from('tech_health_config')
        .select('is_violated_today')
        .eq('user_id', uid)
        .eq('is_active', true);

    if (rows.length < 3) return false;
    return !rows.any((r) => r['is_violated_today'] == true);
  }
}
