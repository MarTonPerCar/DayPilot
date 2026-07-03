import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

/// Selector de tema estilo "paleta": franjas de color unidas donde la
/// seleccionada se expande y muestra su check, una tira de matices del
/// color activo, y un pie con el nombre del tema — todo dentro de una
/// única tarjeta con sombra.
class DayPilotThemeSwatchPicker<T> extends StatelessWidget {
  final T value;
  final List<T> options;
  final Color Function(T) colorOf;
  final String Function(T) nameOf;
  final ValueChanged<T> onChanged;

  const DayPilotThemeSwatchPicker({
    super.key,
    required this.value,
    required this.options,
    required this.colorOf,
    required this.nameOf,
    required this.onChanged,
  });

  Color _onColor(Color c) => c.computeLuminance() > 0.5 ? Colors.black : Colors.white;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final activeColor = colorOf(value);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          AppLocalizations.of(context).settingsColorTheme,
          style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 12),
        Container(
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: colors.shadow.withAlpha(40),
                blurRadius: 16,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(20),
            child: Column(
              children: [
                // ── Franjas de color: la seleccionada se expande
                LayoutBuilder(
                  builder: (context, constraints) {
                    final unit = constraints.maxWidth / (options.length + 1);
                    return Row(
                      children: options.map((opt) {
                        final optColor = colorOf(opt);
                        final isSelected = opt == value;
                        return GestureDetector(
                          onTap: () => onChanged(opt),
                          child: AnimatedContainer(
                            duration: const Duration(milliseconds: 250),
                            curve: Curves.easeOut,
                            width: isSelected ? unit * 2 : unit,
                            height: 56,
                            color: optColor,
                            alignment: Alignment.center,
                            child: isSelected
                                ? Icon(Icons.check_rounded, color: _onColor(optColor))
                                : null,
                          ),
                        );
                      }).toList(),
                    );
                  },
                ),
                // ── Tira de matices del color activo
                Row(
                  children: List.generate(5, (i) {
                    return Expanded(
                      child: Container(
                        height: 18,
                        color: Color.lerp(activeColor, Colors.white, 0.15 + i * 0.18),
                      ),
                    );
                  }),
                ),
                // ── Pie: nombre del tema + muestra de color
                Container(
                  color: colors.surface,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(nameOf(value), style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w600)),
                      Container(
                        width: 16,
                        height: 16,
                        decoration: BoxDecoration(color: activeColor, shape: BoxShape.circle),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
