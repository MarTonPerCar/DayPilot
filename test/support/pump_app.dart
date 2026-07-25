import 'package:daypilot/l10n/app_localizations.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Wraps [providerScopedChild] (already a [ProviderScope] with whatever
/// overrides the test needs — its `overrides:` list literal gets its type
/// inferred from context, sidestepping riverpod 3's `Override` type not
/// being part of its public export surface) in the same [MaterialApp] +
/// localization setup `main.dart` uses, minus the theme/locale listenables
/// a focused screen test doesn't need.
Future<void> pumpApp(WidgetTester tester, Widget providerScopedChild) async {
  await tester.pumpWidget(
    MaterialApp(
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
      home: providerScopedChild,
    ),
  );
}
