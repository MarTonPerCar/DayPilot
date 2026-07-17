import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

import '../../l10n/app_localizations.dart';

class PhotoCropScreen extends StatefulWidget {
  const PhotoCropScreen({super.key, required this.imageBytes});

  final Uint8List imageBytes;

  @override
  State<PhotoCropScreen> createState() => _PhotoCropScreenState();
}

class _PhotoCropScreenState extends State<PhotoCropScreen> {
  final _boundaryKey = GlobalKey();
  final _transformController = TransformationController();

  ui.Image? _decodedImage;
  bool _saving = false;
  bool _didInitTransform = false;

  // Captured on every build from the LayoutBuilder below, so the AppBar's
  // confirm button (outside that scope) can still reach the current layout.
  double _holeSize = 0;
  double _viewportW = 0;
  double _viewportH = 0;

  @override
  void initState() {
    super.initState();
    _decodeImage();
  }

  Future<void> _decodeImage() async {
    final codec = await ui.instantiateImageCodec(widget.imageBytes);
    final frame = await codec.getNextFrame();
    if (!mounted) return;
    setState(() => _decodedImage = frame.image);
  }

  @override
  void dispose() {
    _transformController.dispose();
    _decodedImage?.dispose();
    super.dispose();
  }

  /// Centers the image and scales it so its shortest side exactly covers
  /// the crop square — the framing users expect on first opening the
  /// cropper, instead of the image dumped at raw pixel size unrelated to
  /// the crop hole.
  void _resetTransform() {
    final image = _decodedImage;
    if (image == null) return;
    final imgW = image.width.toDouble();
    final imgH = image.height.toDouble();
    final scale = _holeSize / (imgW < imgH ? imgW : imgH);
    final scaledW = imgW * scale;
    final scaledH = imgH * scale;
    final dx = (_viewportW - scaledW) / 2;
    final dy = (_viewportH - scaledH) / 2;
    _transformController.value = Matrix4.identity()
      ..translateByDouble(dx, dy, 0.0, 1.0)
      ..scaleByDouble(scale, scale, scale, 1.0);
  }

  Future<void> _confirm() async {
    setState(() => _saving = true);
    try {
      final boundary =
          _boundaryKey.currentContext!.findRenderObject() as RenderRepaintBoundary;
      // Capture the whole (unmasked) interactive viewport at high density,
      // then crop just the hole's rect out of it in-memory. The dark mask
      // overlay is a sibling widget, not a child of this boundary, so it
      // never ends up baked into the captured image.
      const pixelRatio = 3.0;
      final full = await boundary.toImage(pixelRatio: pixelRatio);

      final holeLeft = (_viewportW - _holeSize) / 2 * pixelRatio;
      final holeTop = (_viewportH - _holeSize) / 2 * pixelRatio;
      final holeSidePx = _holeSize * pixelRatio;

      final recorder = ui.PictureRecorder();
      final canvas = Canvas(recorder);
      final srcRect = Rect.fromLTWH(holeLeft, holeTop, holeSidePx, holeSidePx);
      final dstRect = Rect.fromLTWH(0, 0, holeSidePx, holeSidePx);
      canvas.drawImageRect(full, srcRect, dstRect, Paint());
      final cropped =
          await recorder.endRecording().toImage(holeSidePx.round(), holeSidePx.round());

      final byteData = await cropped.toByteData(format: ui.ImageByteFormat.png);
      full.dispose();
      cropped.dispose();
      if (!mounted) return;
      Navigator.pop(context, byteData!.buffer.asUint8List());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final ready = _decodedImage != null;

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.close_rounded),
          onPressed: _saving ? null : () => Navigator.pop(context),
        ),
        title: Text(l10n.settingsAdjustPhoto),
        actions: [
          if (_saving)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
              ),
            )
          else if (ready)
            IconButton(
              icon: const Icon(Icons.check_rounded),
              onPressed: _confirm,
            ),
        ],
      ),
      body: !ready
          ? const Center(child: CircularProgressIndicator())
          : LayoutBuilder(
              builder: (context, constraints) {
                _viewportW = constraints.maxWidth;
                _viewportH = constraints.maxHeight;
                _holeSize = (_viewportW < _viewportH ? _viewportW : _viewportH) * 0.8;

                if (!_didInitTransform) {
                  _didInitTransform = true;
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    if (mounted) _resetTransform();
                  });
                }

                return Stack(
                  fit: StackFit.expand,
                  children: [
                    RepaintBoundary(
                      key: _boundaryKey,
                      child: InteractiveViewer(
                        transformationController: _transformController,
                        constrained: false,
                        boundaryMargin: const EdgeInsets.all(double.infinity),
                        minScale: 0.1,
                        maxScale: 5,
                        child: SizedBox(
                          width: _decodedImage!.width.toDouble(),
                          height: _decodedImage!.height.toDouble(),
                          child: Image.memory(widget.imageBytes, fit: BoxFit.fill),
                        ),
                      ),
                    ),
                    // Purely visual — IgnorePointer keeps pan/zoom gestures
                    // reaching the InteractiveViewer underneath.
                    IgnorePointer(
                      child: CustomPaint(
                        size: Size(_viewportW, _viewportH),
                        painter: _CropMaskPainter(holeSize: _holeSize),
                      ),
                    ),
                  ],
                );
              },
            ),
    );
  }
}

class _CropMaskPainter extends CustomPainter {
  const _CropMaskPainter({required this.holeSize});

  final double holeSize;

  @override
  void paint(Canvas canvas, Size size) {
    final holeRect = Rect.fromCenter(
      center: size.center(Offset.zero),
      width: holeSize,
      height: holeSize,
    );

    final outerPath = Path()..addRect(Offset.zero & size);
    final holePath = Path()..addRect(holeRect);
    final maskPath = Path.combine(PathOperation.difference, outerPath, holePath);

    canvas.drawPath(maskPath, Paint()..color = const Color(0xB3000000));
    canvas.drawRect(
      holeRect,
      Paint()
        ..color = Colors.white
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2,
    );
  }

  @override
  bool shouldRepaint(covariant _CropMaskPainter oldDelegate) =>
      oldDelegate.holeSize != holeSize;
}