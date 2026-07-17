import 'package:flutter/material.dart';

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
  late final AnimationController _controller;
  final LayerLink _layerLink = LayerLink();
  OverlayEntry? _overlayEntry;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 200),
    );
  }

  @override
  void dispose() {
    _removeOverlay();
    _controller.dispose();
    super.dispose();
  }

  void _toggle() {
    if (_overlayEntry != null) {
      _closeOverlay();
    } else {
      _openOverlay();
    }
  }

  void _openOverlay() {
    final overlay = Overlay.of(context);
    _overlayEntry = OverlayEntry(builder: _buildOverlayMenu);
    overlay.insert(_overlayEntry!);
    _controller.forward(from: 0);
    setState(() {});
  }

  void _closeOverlay() {
    _controller.reverse().whenComplete(() {
      _removeOverlay();
      if (mounted) setState(() {});
    });
    setState(() {});
  }

  void _removeOverlay() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }

  void _select(String emoji) {
    widget.onReact(emoji);
    _closeOverlay();
  }

  Widget _buildOverlayMenu(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Positioned(
      left: 0,
      top: 0,
      child: CompositedTransformFollower(
        link: _layerLink,
        showWhenUnlinked: false,
        targetAnchor: Alignment.topRight,
        followerAnchor: Alignment.bottomRight,
        offset: const Offset(0, -8),
        child: ScaleTransition(
          scale: CurvedAnimation(parent: _controller, curve: Curves.easeOutBack),
          alignment: Alignment.bottomRight,
          child: FadeTransition(
            opacity: _controller,
            child: Material(
              color: Colors.transparent,
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
    );
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

    return CompositedTransformTarget(
      link: _layerLink,
      child: _TriggerButton(
        icon: _overlayEntry != null ? Icons.close_rounded : Icons.add_reaction_outlined,
        background: colors.surfaceContainerHighest,
        iconColor: colors.onSurfaceVariant,
        onTap: _toggle,
      ),
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