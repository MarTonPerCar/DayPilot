import 'package:flutter/material.dart';
import '../../components/basic/text_field.dart';
import '../../components/basic/top_bar.dart';
import '../../components/basic/task_dot.dart';
import '../../components/forms/form_section.dart';
import '../../components/forms/chip_group.dart';
import '../../components/forms/date_field.dart';
import '../../components/forms/time_field.dart';
import '../../components/forms/switch_tile.dart';
import '../../components/forms/select_field.dart';

class TaskFormScreen extends StatefulWidget {
  const TaskFormScreen({super.key});

  @override
  State<TaskFormScreen> createState() => _TaskFormScreenState();
}

class _TaskFormScreenState extends State<TaskFormScreen> {
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();

  List<TaskPriority> _priority = [TaskPriority.medium];
  List<String> _categories = [];
  DateTime? _dueDate;
  TimeOfDay? _dueTime;
  bool _repeat = false;
  String _frequency = 'Diario';
  bool _hasTitleError = false;

  static const _categoryOptions = ['Personal', 'Trabajo', 'Salud', 'Estudio', 'Otro'];
  static const _frequencyOptions = ['Diario', 'Semanal', 'Mensual', 'Días laborables'];

  void _save() {
    if (_titleCtrl.text.trim().isEmpty) {
      setState(() => _hasTitleError = true);
      return;
    }
    Navigator.pop(context);
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Nueva tarea',
        showBack: true,
        onBack: () => Navigator.pop(context),
        actions: [
          TextButton(
            onPressed: _save,
            child: Text(
              'Guardar',
              style: TextStyle(
                color: colors.primary,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 40),
        children: [
          // Title
          DayPilotTextField(
            controller: _titleCtrl,
            label: 'Título',
            hint: 'Ej: Revisar el informe',
            errorText: _hasTitleError ? 'El título es obligatorio' : null,
            onChanged: (_) {
              if (_hasTitleError) setState(() => _hasTitleError = false);
            },
          ),
          const SizedBox(height: 12),

          // Description
          DayPilotTextField(
            controller: _descCtrl,
            label: 'Descripción (opcional)',
            hint: 'Añade más detalles...',
            maxLines: 3,
          ),
          const SizedBox(height: 20),

          // Priority
          DayPilotFormSection(
            title: 'Prioridad',
            children: [
              DayPilotChipGroup<TaskPriority>(
                options: TaskPriority.values,
                selected: _priority,
                display: (p) => switch (p) {
                  TaskPriority.low    => 'Baja',
                  TaskPriority.medium => 'Media',
                  TaskPriority.high   => 'Alta',
                },
                singleSelect: true,
                onChanged: (v) => setState(() => _priority = v),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Category
          DayPilotFormSection(
            title: 'Categoría',
            children: [
              DayPilotChipGroup<String>(
                options: _categoryOptions,
                selected: _categories,
                display: (s) => s,
                onChanged: (v) => setState(() => _categories = v),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Date & time
          DayPilotFormSection(
            title: 'Fecha límite',
            children: [
              Padding(
                padding: const EdgeInsets.all(12),
                child: DayPilotDateField(
                  label: 'Fecha',
                  value: _dueDate,
                  onChanged: (d) => setState(() => _dueDate = d),
                ),
              ),
              Padding(
                padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
                child: DayPilotTimeField(
                  label: 'Hora (opcional)',
                  value: _dueTime,
                  onChanged: (t) => setState(() => _dueTime = t),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Options
          DayPilotFormSection(
            title: 'Opciones',
            children: [
              DayPilotSwitchTile(
                label: 'Repetir tarea',
                subtitle: 'La tarea se volverá a crear automáticamente',
                icon: Icons.repeat_rounded,
                value: _repeat,
                onChanged: (v) => setState(() => _repeat = v),
              ),
              if (_repeat)
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 4, 16, 12),
                  child: DayPilotSelectField<String>(
                    label: 'Frecuencia',
                    value: _frequency,
                    options: _frequencyOptions,
                    display: (s) => s,
                    onChanged: (v) => setState(() => _frequency = v),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
