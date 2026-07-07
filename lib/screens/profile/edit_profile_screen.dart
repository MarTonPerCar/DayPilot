import 'dart:io' show Platform;
import 'dart:typed_data';

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:window_manager/window_manager.dart';

import '../../components/basic/avatar.dart';
import '../../components/basic/text_field.dart';
import '../../components/basic/top_bar.dart';
import '../../components/forms/select_field.dart';
import '../../core/data/models/app_profile_stats.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/window/desktop_window.dart';
import '../../data/app_data.dart';
import '../../features/profile/profile_notifier.dart';
import '../../l10n/app_localizations.dart';
import 'photo_crop_screen.dart';

class EditProfileScreen extends ConsumerStatefulWidget {
  const EditProfileScreen({super.key, required this.stats});

  final AppProfileStats stats;

  @override
  ConsumerState<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends ConsumerState<EditProfileScreen> {
  late final _nameController = TextEditingController(text: widget.stats.name);
  late final _usernameController = TextEditingController(text: widget.stats.username);
  late String _region = AppData.timezoneOptions.contains(widget.stats.region)
      ? widget.stats.region
      : AppData.timezoneOptions.first;
  late String? _avatarUrl = widget.stats.avatarUrl;
  bool _uploadingAvatar = false;
  bool _saving = false;

  @override
  void dispose() {
    _nameController.dispose();
    _usernameController.dispose();
    super.dispose();
  }

  bool get _supportsCamera => kIsWeb || Platform.isAndroid || Platform.isIOS;

  Future<ImageSource?> _chooseImageSource() {
    final l10n = AppLocalizations.of(context);
    final colors = Theme.of(context).colorScheme;

    Widget option(IconData icon, String label, ImageSource source) {
      return TextButton.icon(
        onPressed: () => Navigator.pop(context, source),
        icon: Icon(icon, color: colors.primary),
        label: Text(label, style: TextStyle(color: colors.primary)),
      );
    }

    return showDialog<ImageSource>(
      context: context,
      builder: (_) => AlertDialog(
        title: Text(l10n.settingsChangePhoto),
        content: Text(l10n.settingsChangePhotoPrompt),
        actionsAlignment: MainAxisAlignment.center,
        actions: [
          option(Icons.photo_library_outlined, l10n.settingsGallery, ImageSource.gallery),
          if (_supportsCamera) option(Icons.camera_alt_outlined, l10n.settingsCamera, ImageSource.camera),
        ],
      ),
    );
  }

  Future<void> _pickAndUploadAvatar() async {
    final l10n = AppLocalizations.of(context);

    final source = await _chooseImageSource();
    if (source == null || !mounted) return;

    isPickingFileNotifier.value = true;
    final XFile? picked;
    try {
      picked = await ImagePicker().pickImage(source: source);
    } finally {
      if (isDesktopPlatform) {
        await windowManager.focus();
        await Future.delayed(const Duration(milliseconds: 100));
      }
      isPickingFileNotifier.value = false;
    }
    if (picked == null || !mounted) return;

    final bytes = await picked.readAsBytes();
    if (!mounted) return;

    final cropped = await Navigator.of(context).push<Uint8List>(
      MaterialPageRoute(builder: (_) => PhotoCropScreen(imageBytes: bytes)),
    );
    if (cropped == null || !mounted) return;

    setState(() => _uploadingAvatar = true);
    try {
      final url = await ref.read(profileRepositoryProvider).uploadAvatar(
            bytes: cropped,
            fileExtension: 'png',
          );
      if (!mounted) return;
      setState(() {
        _avatarUrl = url;
        _uploadingAvatar = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _uploadingAvatar = false);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(l10n.settingsAvatarUploadError)));
    }
  }

  Future<void> _save() async {
    final name = _nameController.text.trim();
    final username = _usernameController.text.trim();
    if (name.isEmpty || username.isEmpty) return;

    setState(() => _saving = true);
    await ref.read(profileRepositoryProvider).updateProfile(
          name: name,
          username: username,
          region: _region,
        );
    await ref.read(profileStatsNotifierProvider.notifier).refresh();
    if (!mounted) return;
    Navigator.pop(context);
  }

  void _openChangePassword() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => const _ChangePasswordSheet(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBar(title: l10n.settingsEditProfile, showBack: true),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 40),
        children: [
          Center(
            child: GestureDetector(
              onTap: _uploadingAvatar ? null : _pickAndUploadAvatar,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  DayPilotAvatar(name: _nameController.text, imageUrl: _avatarUrl, size: 96),
                  if (_uploadingAvatar)
                    const CircularProgressIndicator()
                  else
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: Container(
                        padding: const EdgeInsets.all(6),
                        decoration: BoxDecoration(color: colors.primary, shape: BoxShape.circle),
                        child: Icon(Icons.camera_alt_rounded, size: 16, color: colors.onPrimary),
                      ),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          Center(
            child: TextButton(
              onPressed: _uploadingAvatar ? null : _pickAndUploadAvatar,
              child: Text(l10n.settingsChangePhoto),
            ),
          ),
          const SizedBox(height: 20),
          Text(
            l10n.settingsPersonalInfo.toUpperCase(),
            style: text.labelSmall?.copyWith(
              color: colors.primary,
              letterSpacing: 1.1,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 12),
          DayPilotTextField(label: l10n.loginNameLabel, controller: _nameController),
          const SizedBox(height: 14),
          DayPilotTextField(label: l10n.loginUsernameLabel, controller: _usernameController),
          const SizedBox(height: 14),
          DayPilotSelectField<String>(
            label: l10n.loginTimezoneLabel,
            value: _region,
            options: AppData.timezoneOptions,
            display: (s) => s,
            onChanged: (v) => setState(() => _region = v),
          ),
          const SizedBox(height: 24),
          Text(
            l10n.settingsSecurity.toUpperCase(),
            style: text.labelSmall?.copyWith(
              color: colors.primary,
              letterSpacing: 1.1,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: _openChangePassword,
            style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(52)),
            child: Text(l10n.settingsChangePassword),
          ),
          const SizedBox(height: 28),
          FilledButton(
            onPressed: _saving ? null : _save,
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(52),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
            ),
            child: _saving
                ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : Text(l10n.commonSaveChanges),
          ),
        ],
      ),
    );
  }
}

class _ChangePasswordSheet extends ConsumerStatefulWidget {
  const _ChangePasswordSheet();

  @override
  ConsumerState<_ChangePasswordSheet> createState() => _ChangePasswordSheetState();
}

class _ChangePasswordSheetState extends ConsumerState<_ChangePasswordSheet> {
  final _newPasswordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  String? _error;
  bool _saving = false;

  @override
  void dispose() {
    _newPasswordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final l10n = AppLocalizations.of(context);
    if (_newPasswordController.text != _confirmPasswordController.text) {
      setState(() => _error = l10n.settingsPasswordMismatch);
      return;
    }
    if (_newPasswordController.text.length < 6) {
      setState(() => _error = l10n.authErrorWeakPassword);
      return;
    }

    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await ref.read(profileRepositoryProvider).changePassword(_newPasswordController.text);
      if (!mounted) return;
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(l10n.settingsPasswordChanged)));
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _saving = false;
        _error = l10n.authErrorUnknown;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(l10n.settingsChangePassword, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 20),
              DayPilotPasswordField(label: l10n.settingsNewPassword, controller: _newPasswordController),
              const SizedBox(height: 14),
              DayPilotPasswordField(label: l10n.settingsConfirmPassword, controller: _confirmPasswordController),
              if (_error != null) ...[
                const SizedBox(height: 10),
                Text(_error!, style: text.bodySmall?.copyWith(color: Theme.of(context).colorScheme.error)),
              ],
              const SizedBox(height: 20),
              FilledButton(
                onPressed: _saving ? null : _submit,
                style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(52)),
                child: _saving
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                    : Text(l10n.commonSave),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
