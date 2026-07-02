// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:daypilot/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('DayPilot home screen renders', (WidgetTester tester) async {
    await tester.pumpWidget(const DayPilotApp());

    expect(find.text('DayPilot'), findsWidgets);
    expect(find.byIcon(Icons.flight_takeoff_rounded), findsOneWidget);
  });
}
