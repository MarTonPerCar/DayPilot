import 'package:flutter/material.dart';

class DayPilotSelectField<T> extends StatelessWidget {
  final String label;
  final T? value;
  final List<T> options;
  final String Function(T) display;
  final ValueChanged<T> onChanged;
  final IconData? prefixIcon;
  final String? hint;

  const DayPilotSelectField({
    super.key,
    required this.label,
    this.value,
    required this.options,
    required this.display,
    required this.onChanged,
    this.prefixIcon,
    this.hint,
  });

  Future<void> _open(BuildContext context) async {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    final result = await showModalBottomSheet<T>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 36,
                height: 4,
                margin: const EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(
                  color: colors.outlineVariant,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              Padding(
                padding: const EdgeInsets.only(bottom: 8, left: 16),
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: Text(label, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                ),
              ),
              ...options.map((opt) {
                final selected = opt == value;
                return ListTile(
                  title: Text(display(opt)),
                  leading: selected
                      ? Icon(Icons.check_rounded, color: colors.primary)
                      : const SizedBox(width: 24),
                  onTap: () => Navigator.pop(ctx, opt),
                );
              }),
              const SizedBox(height: 8),
            ],
          ),
        );
      },
    );

    if (result != null) onChanged(result);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return InkWell(
      onTap: () => _open(context),
      child: InputDecorator(
        decoration: InputDecoration(
          labelText: label,
          hintText: hint,
          prefixIcon: prefixIcon != null ? Icon(prefixIcon) : null,
          suffixIcon: const Icon(Icons.expand_more_rounded),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
          filled: true,
          fillColor: colors.surfaceContainerHighest.withAlpha(80),
        ),
        child: value != null
            ? Text(display(value as T),
                style: TextStyle(color: colors.onSurface))
            : const SizedBox.shrink(),
      ),
    );
  }
}
