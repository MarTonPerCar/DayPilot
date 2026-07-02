import 'package:flutter/material.dart';

class DayPilotFormSection extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const DayPilotFormSection({
    super.key,
    required this.title,
    required this.children,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(bottom: 8, left: 4),
          child: Text(
            title.toUpperCase(),
            style: text.labelSmall?.copyWith(
              color: colors.primary,
              letterSpacing: 1.1,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        Card.filled(
          clipBehavior: Clip.hardEdge,
          margin: EdgeInsets.zero,
          child: Column(
            children: List.generate(children.length, (i) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  children[i],
                  if (i < children.length - 1)
                    Divider(
                      height: 1,
                      indent: 16,
                      endIndent: 16,
                      color: colors.outlineVariant,
                    ),
                ],
              );
            }),
          ),
        ),
      ],
    );
  }
}
