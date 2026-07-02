import 'package:flutter/material.dart';
import 'components/basic/avatar.dart';
import 'components/basic/button.dart';
import 'components/basic/divider.dart';
import 'components/basic/empty_state.dart';
import 'components/basic/filter_selector.dart';
import 'components/basic/reactions.dart';
import 'components/basic/section_indicator.dart';
import 'components/basic/task_dot.dart';
import 'components/basic/text_field.dart';
import 'components/basic/top_bar.dart';

class ComponentCatalog extends StatefulWidget {
  const ComponentCatalog({super.key});

  @override
  State<ComponentCatalog> createState() => _ComponentCatalogState();
}

class _ComponentCatalogState extends State<ComponentCatalog> {
  String _filter = 'Todos';
  String? _reaction;
  int _activeSection = 0;

  static const _filterOptions = ['Todos', 'Tareas', 'Hábitos', 'Social'];
  static const _sections = [
    (label: 'Inicio',    icon: Icons.home_rounded),
    (label: 'Hábitos',  icon: Icons.self_improvement_rounded),
    (label: 'Progreso', icon: Icons.bar_chart_rounded),
    (label: 'Ranking',  icon: Icons.emoji_events_rounded),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: 'Catálogo',
        actions: [
          IconButton(
            icon: const Icon(Icons.palette_outlined),
            onPressed: () {},
            tooltip: 'Temas',
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 40),
        children: [

          // ── TopBar ──────────────────────────────────────────────
          _SectionHeader('TopBar'),
          _Preview(child: DayPilotTopBar(title: 'Sin botón atrás')),
          const SizedBox(height: 8),
          _Preview(
            child: DayPilotTopBar(
              title: 'Con botón atrás',
              showBack: true,
              onBack: () {},
            ),
          ),
          const SizedBox(height: 8),
          _Preview(
            child: DayPilotTopBarWithActions(
              title: 'Con acciones',
              showBack: true,
              onBack: () {},
              actions: [
                IconButton(icon: const Icon(Icons.search), onPressed: () {}),
                IconButton(icon: const Icon(Icons.more_vert), onPressed: () {}),
              ],
            ),
          ),

          // ── Botones ─────────────────────────────────────────────
          _SectionHeader('Botones'),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              DayPilotButton(label: 'Primario', onPressed: () {}),
              DayPilotButton(
                label: 'Outline',
                variant: DayPilotButtonVariant.outline,
                onPressed: () {},
              ),
              const DayPilotButton(label: 'Cargando', isLoading: true),
              const DayPilotButton(label: 'Deshabilitado'),
              DayPilotButton(
                label: 'Con icono',
                icon: Icons.add_rounded,
                onPressed: () {},
              ),
              DayPilotButton(
                label: 'Outline icono',
                icon: Icons.edit_outlined,
                variant: DayPilotButtonVariant.outline,
                onPressed: () {},
              ),
            ],
          ),

          // ── Campos de texto ─────────────────────────────────────
          _SectionHeader('Campos de texto'),
          const DayPilotTextField(
            label: 'Nombre de usuario',
            hint: 'p.ej. mario_garcia',
            prefixIcon: Icons.person_outline_rounded,
          ),
          const SizedBox(height: 10),
          const DayPilotTextField(
            label: 'Email',
            hint: 'ejemplo@correo.com',
            prefixIcon: Icons.email_outlined,
            keyboardType: TextInputType.emailAddress,
          ),
          const SizedBox(height: 10),
          const DayPilotTextField(
            label: 'Descripción',
            hint: 'Escribe una descripción...',
            maxLines: 3,
          ),
          const SizedBox(height: 10),
          const DayPilotPasswordField(),

          // ── Avatar ──────────────────────────────────────────────
          _SectionHeader('Avatar'),
          Row(
            children: [
              const DayPilotAvatar(name: 'Mario García', size: 32),
              const SizedBox(width: 12),
              const DayPilotAvatar(name: 'Ana López', size: 44),
              const SizedBox(width: 12),
              const DayPilotAvatar(name: 'Carlos Ruiz', size: 56),
              const SizedBox(width: 12),
              const DayPilotAvatar(name: 'X', size: 44),
              const SizedBox(width: 12),
              const DayPilotAvatar(size: 44),
            ],
          ),

          // ── Estado vacío ─────────────────────────────────────────
          _SectionHeader('Estado vacío'),
          const DayPilotEmptyState(
            icon: Icons.task_alt_outlined,
            title: 'Sin tareas por hoy',
            subtitle: 'Añade tu primera tarea para empezar a ganar puntos.',
          ),
          const SizedBox(height: 8),
          const DayPilotEmptyState(
            icon: Icons.people_outline_rounded,
            title: 'Sin amigos aún',
          ),

          // ── Separador ────────────────────────────────────────────
          _SectionHeader('Separador'),
          const DayPilotDivider(),
          const SizedBox(height: 8),
          const DayPilotDivider(label: 'o continúa con'),
          const SizedBox(height: 8),
          const DayPilotDivider(label: 'hoy'),

          // ── Filtros ──────────────────────────────────────────────
          _SectionHeader('Filtros'),
          DayPilotFilterSelector<String>(
            options: _filterOptions,
            selected: _filter,
            label: (s) => s,
            onSelected: (s) => setState(() => _filter = s),
          ),

          // ── Reacciones ───────────────────────────────────────────
          _SectionHeader('Reacciones'),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DayPilotReactions(
              selected: _reaction,
              onReact: (e) => setState(
                () => _reaction = _reaction == e ? null : e,
              ),
            ),
          ),

          // ── Punto de prioridad ───────────────────────────────────
          _SectionHeader('Punto de prioridad'),
          Row(
            children: [
              const TaskDot(priority: TaskPriority.low),
              const SizedBox(width: 6),
              Text('Baja', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskPriority.medium),
              const SizedBox(width: 6),
              Text('Media', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskPriority.high),
              const SizedBox(width: 6),
              Text('Alta', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskPriority.high, size: 14),
              const SizedBox(width: 6),
              Text('Alta ×1.4', style: Theme.of(context).textTheme.labelMedium),
            ],
          ),

          // ── Indicador de sección ─────────────────────────────────
          _SectionHeader('Indicador de sección'),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: _sections.indexed.map(((int, ({IconData icon, String label})) entry) {
                final (i, section) = entry;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: GestureDetector(
                    onTap: () => setState(() => _activeSection = i),
                    child: HomeSectionIndicator(
                      label: section.label,
                      icon: section.icon,
                      isActive: _activeSection == i,
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

class _SectionHeader extends StatelessWidget {
  final String title;
  const _SectionHeader(this.title);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 28, bottom: 12),
      child: Text(
        title.toUpperCase(),
        style: Theme.of(context).textTheme.labelSmall?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              letterSpacing: 1.2,
              fontWeight: FontWeight.w600,
            ),
      ),
    );
  }
}

// Wraps an AppBar-style widget in a clipped card so it previews correctly inline
class _Preview extends StatelessWidget {
  final Widget child;
  const _Preview({required this.child});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: Material(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        child: child,
      ),
    );
  }
}
