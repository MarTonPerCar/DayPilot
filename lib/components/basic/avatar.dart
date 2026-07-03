import 'package:flutter/material.dart';

class DayPilotAvatar extends StatelessWidget {
  final String? imageUrl;
  final String? name;
  final double size;

  const DayPilotAvatar({
    super.key,
    this.imageUrl,
    this.name,
    this.size = 40,
  });

  String get _initials {
    if (name == null || name!.trim().isEmpty) return '?';
    final parts = name!.trim().split(' ');
    if (parts.length >= 2 && parts[1].isNotEmpty) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return parts[0][0].toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final radius = BorderRadius.circular(size * 0.28);

    return ClipRRect(
      borderRadius: radius,
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: colors.primaryContainer,
          borderRadius: radius,
        ),
        alignment: Alignment.center,
        child: imageUrl != null
            ? Image.network(
                imageUrl!,
                width: size,
                height: size,
                fit: BoxFit.cover,
              )
            : Text(
                _initials,
                style: TextStyle(
                  color: colors.onPrimaryContainer,
                  fontWeight: FontWeight.w600,
                  fontSize: size * 0.4,
                ),
              ),
      ),
    );
  }
}
