import 'package:flutter/material.dart';

class DayPilotTimeField extends StatelessWidget {
  final String label;
  final TimeOfDay? value;
  final ValueChanged<TimeOfDay> onChanged;

  const DayPilotTimeField({
    super.key,
    required this.label,
    this.value,
    required this.onChanged,
  });

  Future<void> _pick(BuildContext context) async {
    final result = await showTimePicker(
      context: context,
      initialTime: value ?? TimeOfDay.now(),
    );
    if (result != null) onChanged(result);
  }

  String _format(TimeOfDay t) {
    final h = t.hour.toString().padLeft(2, '0');
    final m = t.minute.toString().padLeft(2, '0');
    return '$h:$m';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return InkWell(
      onTap: () => _pick(context),
      child: InputDecorator(
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: const Icon(Icons.access_time_rounded),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
          filled: true,
          fillColor: colors.surfaceContainerHighest.withAlpha(80),
        ),
        child: value != null
            ? Text(_format(value!), style: TextStyle(color: colors.onSurface))
            : const SizedBox.shrink(),
      ),
    );
  }
}
