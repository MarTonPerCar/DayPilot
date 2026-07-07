import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show SystemSound, SystemSoundType;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../features/progress/progress_notifier.dart';
import '../../l10n/app_localizations.dart';

class TimerRunningScreen extends ConsumerStatefulWidget {
  final String title;
  final Color color;
  final bool isPomodoro;
  final int workMinutes;
  final int restMinutes;
  final int totalSessions;

  const TimerRunningScreen({
    super.key,
    required this.title,
    required this.color,
    this.isPomodoro = false,
    required this.workMinutes,
    this.restMinutes = 0,
    this.totalSessions = 1,
  });

  @override
  ConsumerState<TimerRunningScreen> createState() => _TimerRunningScreenState();
}

class _TimerRunningScreenState extends ConsumerState<TimerRunningScreen> with WidgetsBindingObserver {
  Timer? _ticker;
  bool _running = false;
  int _currentSession = 1;
  bool _isRestPhase = false;
  late Duration _remaining = Duration(minutes: widget.workMinutes);

  int get _phaseMinutes => _isRestPhase ? widget.restMinutes : widget.workMinutes;
  Duration get _phaseDuration => Duration(minutes: _phaseMinutes);

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  // The desktop flyout window hides itself on focus loss (see
  // DesktopFlyoutScope.onWindowBlur) — a continuously-ticking per-second
  // Timer while that happens seems to confuse window_manager's hide/refocus
  // on Linux, leaving the window stuck open. Stopping the ticker whenever
  // the app isn't in the foreground avoids that entirely. Also flips the
  // play/pause button back to "paused" so it's immediately correct — and
  // immediately tappable to resume — once the app is reopened, instead of
  // showing "running" for a timer that's actually frozen.
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state != AppLifecycleState.resumed && _running) {
      _ticker?.cancel();
      _ticker = null;
      setState(() => _running = false);
    }
  }

  Timer _startTicker() {
    return Timer.periodic(const Duration(seconds: 1), (_) {
      if (_remaining.inSeconds <= 0) {
        _advance();
      } else {
        setState(() => _remaining -= const Duration(seconds: 1));
      }
    });
  }

  void _toggle() {
    if (_running) {
      _ticker?.cancel();
      _ticker = null;
      setState(() => _running = false);
      return;
    }
    setState(() => _running = true);
    _ticker = _startTicker();
  }

  void _advance() {
    if (widget.isPomodoro) {
      if (!_isRestPhase && widget.restMinutes > 0) {
        setState(() {
          _isRestPhase = true;
          _remaining = Duration(minutes: widget.restMinutes);
        });
        return;
      }
      if (_currentSession < widget.totalSessions) {
        setState(() {
          _currentSession++;
          _isRestPhase = false;
          _remaining = Duration(minutes: widget.workMinutes);
        });
        return;
      }
    }
    _ticker?.cancel();
    _ticker = null;
    setState(() {
      _running = false;
      _remaining = Duration.zero;
    });
    SystemSound.play(SystemSoundType.alert);
    ref.read(progressNotifierProvider.notifier).completeTimerSession();
  }

  void _skip() {
    setState(() => _remaining = Duration.zero);
    _advance();
  }

  void _reset() {
    _ticker?.cancel();
    _ticker = null;
    setState(() {
      _running = false;
      _currentSession = 1;
      _isRestPhase = false;
      _remaining = Duration(minutes: widget.workMinutes);
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _ticker?.cancel();
    super.dispose();
  }

  String _fmt(Duration d) {
    final m = d.inMinutes.remainder(1000).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final total = _phaseDuration.inSeconds;
    final progress = total == 0 ? 0.0 : _remaining.inSeconds / total;
    // Covers both "already earned earlier today" and "just earned this
    // session" in one check, since completeTimerSession() refreshes this.
    final pointEarnedToday = (ref.watch(progressNotifierProvider)?.pointsFromTimer ?? 0) > 0;

    return Scaffold(
      appBar: DayPilotTopBar(title: widget.title, showBack: true),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 24),
            if (widget.isPomodoro) ...[
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(widget.totalSessions, (i) {
                  final done = i + 1 < _currentSession;
                  final current = i + 1 == _currentSession;
                  return Container(
                    margin: const EdgeInsets.symmetric(horizontal: 4),
                    width: 10,
                    height: 10,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: (done || current) ? widget.color : colors.surfaceContainerHighest,
                    ),
                  );
                }),
              ),
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: widget.color.withAlpha(30),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(width: 10, height: 10, decoration: BoxDecoration(color: widget.color, shape: BoxShape.circle)),
                    const SizedBox(width: 8),
                    Text(
                      '${l10n.timerSessionOf(_currentSession, widget.totalSessions)} — '
                      '${_isRestPhase ? l10n.timerRest : l10n.timerWork}',
                      style: text.labelLarge?.copyWith(color: widget.color, fontWeight: FontWeight.w700),
                    ),
                  ],
                ),
              ),
            ],
            const Spacer(),
            SizedBox(
              width: 280,
              height: 280,
              child: CustomPaint(
                painter: _RingPainter(progress: progress, color: widget.color, trackColor: colors.surfaceContainerHighest),
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(_fmt(_remaining), style: text.displayMedium?.copyWith(fontWeight: FontWeight.w700)),
                      const SizedBox(height: 4),
                      Text(
                        widget.isPomodoro
                            ? l10n.timerPhaseMinutes(
                                _phaseMinutes, _isRestPhase ? l10n.timerRest : l10n.timerWork)
                            : l10n.timerMinValue(_phaseMinutes),
                        style: text.bodyMedium?.copyWith(color: widget.color),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.info_outline_rounded, size: 14, color: colors.onSurfaceVariant),
                const SizedBox(width: 6),
                Flexible(
                  child: Text(
                    l10n.timerClosedAppWarning,
                    textAlign: TextAlign.center,
                    style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ),
              ],
            ),
            const Spacer(),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _RoundButton(
                  icon: Icons.refresh_rounded,
                  background: colors.surfaceContainerHighest,
                  iconColor: colors.onSurfaceVariant,
                  size: 56,
                  onTap: _reset,
                ),
                const SizedBox(width: 20),
                _RoundButton(
                  icon: _running ? Icons.pause_rounded : Icons.play_arrow_rounded,
                  background: widget.color,
                  iconColor: Colors.white,
                  size: 72,
                  onTap: _toggle,
                ),
                if (widget.isPomodoro) ...[
                  const SizedBox(width: 20),
                  _RoundButton(
                    icon: Icons.skip_next_rounded,
                    background: widget.color.withAlpha(30),
                    iconColor: widget.color,
                    size: 56,
                    onTap: _skip,
                  ),
                ],
              ],
            ),
            if (pointEarnedToday) ...[
              const SizedBox(height: 20),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: colors.primary.withAlpha(38),
                  borderRadius: BorderRadius.circular(50),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text('⭐', style: TextStyle(fontSize: 16)),
                    const SizedBox(width: 8),
                    Text(
                      l10n.timerPointEarned,
                      style: text.labelMedium?.copyWith(color: colors.primary, fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}

class _RoundButton extends StatelessWidget {
  final IconData icon;
  final Color background;
  final Color iconColor;
  final double size;
  final VoidCallback onTap;

  const _RoundButton({
    required this.icon,
    required this.background,
    required this.iconColor,
    required this.size,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: background,
      shape: const CircleBorder(),
      child: InkWell(
        onTap: onTap,
        customBorder: const CircleBorder(),
        child: SizedBox(
          width: size,
          height: size,
          child: Icon(icon, color: iconColor, size: size * 0.45),
        ),
      ),
    );
  }
}

class _RingPainter extends CustomPainter {
  final double progress;
  final Color color;
  final Color trackColor;

  _RingPainter({required this.progress, required this.color, required this.trackColor});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 8;
    const strokeWidth = 14.0;

    canvas.drawCircle(
      center,
      radius,
      Paint()
        ..color = trackColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = strokeWidth,
    );

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      -3.14159 / 2,
      2 * 3.14159 * progress.clamp(0.0, 1.0),
      false,
      Paint()
        ..color = color
        ..style = PaintingStyle.stroke
        ..strokeWidth = strokeWidth
        ..strokeCap = StrokeCap.round,
    );
  }

  @override
  bool shouldRepaint(covariant _RingPainter oldDelegate) => oldDelegate.progress != progress;
}
