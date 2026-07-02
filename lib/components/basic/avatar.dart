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
    if (name == null || name!.isEmpty) return '?';
    final parts = name!.trim().split(' ');
    if (parts.length >= 2) return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    return name![0].toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return CircleAvatar(
      radius: size / 2,
      backgroundColor: colors.primaryContainer,
      backgroundImage: imageUrl != null ? NetworkImage(imageUrl!) : null,
      child: imageUrl == null
          ? Text(
              _initials,
              style: TextStyle(
                color: colors.onPrimaryContainer,
                fontWeight: FontWeight.w600,
                fontSize: size * 0.36,
              ),
            )
          : null,
    );
  }
}
