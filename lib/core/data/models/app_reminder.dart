class AppReminder {
  const AppReminder({
    required this.id,
    required this.title,
    required this.dateTime,
    this.frequency = 'once',
    this.notifyBefore = false,
    this.enabled = true,
  });

  final String id;
  final String title;
  final DateTime dateTime;
  final String frequency;
  final bool notifyBefore;
  final bool enabled;

  AppReminder copyWith({
    DateTime? dateTime,
    bool? enabled,
  }) {
    return AppReminder(
      id: id,
      title: title,
      dateTime: dateTime ?? this.dateTime,
      frequency: frequency,
      notifyBefore: notifyBefore,
      enabled: enabled ?? this.enabled,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'dateTime': dateTime.toIso8601String(),
        'frequency': frequency,
        'notifyBefore': notifyBefore,
        'enabled': enabled,
      };

  factory AppReminder.fromJson(Map<String, dynamic> json) => AppReminder(
        id: json['id'] as String,
        title: json['title'] as String,
        dateTime: DateTime.parse(json['dateTime'] as String),
        frequency: json['frequency'] as String? ?? 'once',
        notifyBefore: json['notifyBefore'] as bool? ?? false,
        enabled: json['enabled'] as bool? ?? true,
      );
}
