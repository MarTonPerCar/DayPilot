import 'package:flutter/material.dart';

class DayPilotReactions extends StatelessWidget {
  final String? selected;
  final void Function(String) onReact;

  static const _emojis = ['👍', '❤️', '🔥', '⭐', '😮', '😢'];

  const DayPilotReactions({
    super.key,
    this.selected,
    required this.onReact,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: _emojis.map((emoji) {
        final isSelected = emoji == selected;
        return GestureDetector(
          onTap: () => onReact(emoji),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            margin: const EdgeInsets.only(right: 8),
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: isSelected
                  ? colors.primaryContainer
                  : colors.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text(emoji, style: const TextStyle(fontSize: 20)),
          ),
        );
      }).toList(),
    );
  }
}

/// Botón circular que despliega una píldora con 4 emojis al pulsarlo y,
/// tras elegir uno, se convierte en una marca de "ya reaccionado".
/// Solo se permite una reacción por usuario.
class DayPilotReactionPicker extends StatefulWidget {
  final String? selected;
  final void Function(String) onReact;

  static const _emojis = ['🔥', '👏', '💪', '⭐'];

  const DayPilotReactionPicker({
    super.key,
    this.selected,
    required this.onReact,
  });

  @override
  State<DayPilotReactionPicker> createState() => _DayPilotReactionPickerState();
}

class _DayPilotReactionPickerState extends State<DayPilotReactionPicker>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 200),
  );
  bool _open = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggle() {
    setState(() => _open = !_open);
    if (_open) {
      _controller.forward(from: 0);
    } else {
      _controller.reverse();
    }
  }

  void _select(String emoji) {
    widget.onReact(emoji);
    _controller.reverse();
    setState(() => _open = false);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    if (widget.selected != null) {
      return _TriggerButton(
        icon: Icons.check_rounded,
        background: colors.primaryContainer,
        iconColor: colors.onPrimaryContainer,
        onTap: null,
      );
    }

    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.bottomCenter,
      children: [
        Positioned(
          bottom: 44,
          right: -8,
          child: IgnorePointer(
            ignoring: !_open,
            child: ScaleTransition(
              scale: CurvedAnimation(parent: _controller, curve: Curves.easeOutBack),
              alignment: Alignment.bottomRight,
              child: FadeTransition(
                opacity: _controller,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                  decoration: BoxDecoration(
                    color: colors.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(color: colors.outlineVariant),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withAlpha(40),
                        blurRadius: 8,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: DayPilotReactionPicker._emojis.map((emoji) {
                      return GestureDetector(
                        onTap: () => _select(emoji),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 4),
                          child: Text(emoji, style: const TextStyle(fontSize: 20)),
                        ),
                      );
                    }).toList(),
                  ),
                ),
              ),
            ),
          ),
        ),
        _TriggerButton(
          icon: _open ? Icons.close_rounded : Icons.add_reaction_outlined,
          background: colors.surfaceContainerHighest,
          iconColor: colors.onSurfaceVariant,
          onTap: _toggle,
        ),
      ],
    );
  }
}

class _TriggerButton extends StatelessWidget {
  final IconData icon;
  final Color background;
  final Color iconColor;
  final VoidCallback? onTap;

  const _TriggerButton({
    required this.icon,
    required this.background,
    required this.iconColor,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 36,
        height: 36,
        alignment: Alignment.center,
        decoration: BoxDecoration(color: background, shape: BoxShape.circle),
        child: Icon(icon, size: 18, color: iconColor),
      ),
    );
  }
}
