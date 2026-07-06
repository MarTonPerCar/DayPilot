import 'package:flutter/material.dart';
import 'components/basic/avatar.dart';
import 'components/basic/button.dart';
import 'components/basic/divider.dart';
import 'components/basic/empty_state.dart';
import 'components/basic/filter_selector.dart';
import 'components/basic/reactions.dart';
import 'components/basic/dropdown_pill.dart';
import 'components/basic/section_indicator.dart';
import 'components/basic/task_category.dart';
import 'components/basic/task_dot.dart';
import 'components/basic/text_field.dart';
import 'components/basic/top_bar.dart';
import 'components/forms/category_chip_group.dart';
import 'components/forms/chip_group.dart';
import 'components/forms/collapsible_section.dart';
import 'components/forms/color_picker.dart';
import 'components/forms/date_field.dart';
import 'components/forms/difficulty_field.dart';
import 'components/forms/form_section.dart';
import 'components/forms/radio_group.dart';
import 'components/forms/select_field.dart';
import 'components/forms/slider_field.dart';
import 'components/forms/stepper_field.dart';
import 'components/forms/switch_tile.dart';
import 'components/forms/time_field.dart';
import 'components/cards/calendar_task_card.dart';
import 'components/cards/task_card.dart';
import 'components/cards/ranking_card.dart';
import 'components/cards/friend_card.dart';
import 'components/cards/habit_card.dart';
import 'components/cards/app_limit_card.dart';
import 'components/cards/notification_card.dart';
import 'components/cards/reminder_card.dart';
import 'components/cards/steps_progress_card.dart';
import 'components/cards/tech_restriction_card.dart';
import 'components/cards/timer_card.dart';
import 'components/cards/timer_preset_card.dart';
import 'components/cards/profile_stats_card.dart';
import 'components/cards/weekly_reaction_card.dart';
import 'components/cards/steps_card.dart';
import 'components/cards/calendar_day_card.dart';
import 'components/cards/daily_summary_card.dart';
import 'components/cards/month_calendar_card.dart';
import 'components/cards/progress_chart_card.dart';
import 'data/app_data.dart';

class ComponentCatalog extends StatefulWidget {
  const ComponentCatalog({super.key});

  @override
  State<ComponentCatalog> createState() => _ComponentCatalogState();
}

class _ComponentCatalogState extends State<ComponentCatalog> {
  String _filter = 'Todos';
  String? _reaction;
  int _activeSection = 0;

  bool _task1Done = false;
  bool _task2Done = false;
  int _swipeEpoch = 0;

  bool _appEnabled = true;
  bool _groupEnabled = true;

  bool _switchVal = true;
  bool _switchVal2 = false;
  String? _selectVal;
  double _sliderVal = 30;
  int _stepperVal = 25;
  Color _pickedColor = DayPilotColorPicker.defaultColors.first;
  DateTime? _pickedDate;
  TimeOfDay? _pickedTime;
  TaskDifficulty _radioVal = TaskDifficulty.medium;
  List<String> _chipSelected = ['Trabajo'];
  List<TaskDifficulty> _chipSingle = [TaskDifficulty.medium];

  TaskDifficulty? _dropdownDifficulty;
  TaskCategory? _dropdownCategory;
  TaskCategory _catalogCategory = TaskCategory.personal;
  TaskDifficulty _catalogDifficulty = TaskDifficulty.easy;
  bool _catalogTaskDone = false;
  DateTime _catalogMonth = DateTime(2026, 7);
  DateTime _catalogSelectedDay = DateTime(2026, 7, 3);

  int _catalogStepsGoal = AppData.stepsGoal;
  bool _catalogReminderEnabled = true;
  late final _catalogRestriction = AppData.newRestrictionList().first;

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

          _SectionHeader('Separador'),
          const DayPilotDivider(),
          const SizedBox(height: 8),
          const DayPilotDivider(label: 'o continúa con'),
          const SizedBox(height: 8),
          const DayPilotDivider(label: 'hoy'),

          _SectionHeader('Filtros'),
          DayPilotFilterSelector<String>(
            options: _filterOptions,
            selected: _filter,
            label: (s) => s,
            onSelected: (s) => setState(() => _filter = s),
          ),

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

          _SectionHeader('Punto de dificultad'),
          Row(
            children: [
              const TaskDot(priority: TaskDifficulty.easy),
              const SizedBox(width: 6),
              Text('Fácil', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskDifficulty.medium),
              const SizedBox(width: 6),
              Text('Media', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskDifficulty.hard),
              const SizedBox(width: 6),
              Text('Difícil', style: Theme.of(context).textTheme.labelMedium),
              const SizedBox(width: 20),
              const TaskDot(priority: TaskDifficulty.hard, size: 14),
              const SizedBox(width: 6),
              Text('Difícil ×1.4', style: Theme.of(context).textTheme.labelMedium),
            ],
          ),

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

          _SectionHeader('Tarjeta de tarea'),
          TaskCard(
            title: 'Diseñar pantalla de inicio',
            description: 'Crear wireframe y componentes base',
            dueDate: 'hoy',
            priority: TaskDifficulty.hard,
            category: 'Diseño',
            completed: _task1Done,
            onToggle: () => setState(() => _task1Done = !_task1Done),
          ),
          const SizedBox(height: 8),
          TaskCard(
            title: 'Revisar documentación de Flutter',
            priority: TaskDifficulty.medium,
            completed: _task2Done,
            onToggle: () => setState(() => _task2Done = !_task2Done),
          ),
          const SizedBox(height: 8),
          const TaskCard(
            title: 'Tarea completada de ejemplo',
            dueDate: 'ayer',
            priority: TaskDifficulty.easy,
            completed: true,
          ),

          _SectionHeader('Tarjeta de tarea (deslizar)'),
          Text(
            'Desliza hacia la izquierda para eliminar',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
          ),
          const SizedBox(height: 8),
          TaskSwipeCard(
            key: ValueKey(_swipeEpoch),
            id: 'swipe_demo_$_swipeEpoch',
            title: 'Completar el informe semanal',
            description: 'Incluir métricas de pasos y tareas',
            dueDate: 'mañana',
            priority: TaskDifficulty.medium,
            category: 'Trabajo',
            onDelete: () => setState(() => _swipeEpoch++),
          ),

          _SectionHeader('Ranking'),
          RankingCard(
            position: 1,
            username: 'mario_garcia',
            points: 2840,
            streak: 12,
            isCurrentUser: true,
          ),
          RankingCard(
            position: 2,
            username: 'ana_lopez',
            points: 2610,
            streak: 8,
          ),
          RankingCard(
            position: 3,
            username: 'carlos_ruiz',
            points: 2290,
            streak: 5,
          ),
          RankingCard(
            position: 4,
            username: 'lucia_fdez',
            points: 1870,
          ),

          _SectionHeader('Podio'),
          PodiumCard(
            firstName: 'Mario García',
            firstPoints: 2840,
            secondName: 'Ana López',
            secondPoints: 2610,
            thirdName: 'Carlos Ruiz',
            thirdPoints: 2290,
          ),

          _SectionHeader('Tarjeta de amigo'),
          FriendCard(
            name: 'Ana López',
            email: 'ana.lopez@daypilot.test',
            points: 2610,
            streak: 8,
            weeklyPoints: 480,
            weeklyTasks: 12,
            weeklySteps: 34200,
            weeklyStreak: 5,
            onReact: (_) {},
          ),
          const SizedBox(height: 8),
          const FriendCard(
            name: 'Lucía Fernández',
            email: 'lucia.fernandez@daypilot.test',
            points: 1870,
            streak: 3,
          ),
          const SizedBox(height: 8),
          FriendRequestCard(
            name: 'Pedro Gómez',
            email: 'pedro.gomez@daypilot.test',
            onAccept: () {},
            onDecline: () {},
          ),

          _SectionHeader('Búsqueda de usuario'),
          const UserSearchCard(
            name: 'Nueva Persona',
            email: 'nueva.persona@daypilot.test',
          ),
          const SizedBox(height: 8),
          const UserSearchCard(
            name: 'Ana López',
            email: 'ana.lopez@daypilot.test',
            isFriend: true,
          ),
          const SizedBox(height: 8),
          const UserSearchCard(
            name: 'Otro Usuario',
            email: 'otro.usuario@daypilot.test',
            isPending: true,
          ),

          _SectionHeader('Hub de hábitos'),
          HabitCard(
            icon: Icons.directions_walk_rounded,
            title: 'Pasos',
            subtitle: '7.432 / 10.000 pasos hoy',
            progress: 0.74,
            iconColor: Theme.of(context).colorScheme.tertiary,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.timer_rounded,
            title: 'Temporizadores',
            subtitle: '2 sesiones · 48 min totales',
            progress: 0.48,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          HabitCard(
            icon: Icons.health_and_safety_rounded,
            title: 'Salud tecnológica',
            subtitle: '3 apps con límite activo',
            iconColor: Theme.of(context).colorScheme.error,
            onTap: () {},
          ),

          _SectionHeader('Límite de app'),
          AppLimitCard(
            appName: 'Instagram',
            appIcon: Icons.photo_camera_outlined,
            usageMinutes: 52,
            limitMinutes: 60,
            enabled: _appEnabled,
            onToggle: () => setState(() => _appEnabled = !_appEnabled),
          ),
          const SizedBox(height: 8),
          const AppLimitCard(
            appName: 'YouTube',
            appIcon: Icons.play_circle_outline_rounded,
            usageMinutes: 30,
            limitMinutes: 120,
            enabled: true,
          ),

          _SectionHeader('Límite de grupo'),
          GroupLimitCard(
            groupName: 'Redes sociales',
            groupIcon: Icons.people_alt_outlined,
            appCount: 4,
            usageMinutes: 95,
            limitMinutes: 120,
            enabled: _groupEnabled,
            onToggle: () => setState(() => _groupEnabled = !_groupEnabled),
          ),

          _SectionHeader('Notificación'),
          const NotificationCard(
            type: NotificationType.social,
            content: 'pedro_gz quiere ser tu amigo',
            timestamp: 'hace 5 min',
            read: false,
          ),
          const SizedBox(height: 6),
          const NotificationCard(
            type: NotificationType.achievement,
            content: '¡Subiste al nivel 8! Sigue así.',
            timestamp: 'hace 2h',
            read: false,
          ),
          const SizedBox(height: 6),
          const NotificationCard(
            type: NotificationType.task,
            content: 'Tienes 3 tareas pendientes para hoy',
            timestamp: 'ayer',
            read: true,
          ),
          const SizedBox(height: 6),
          const NotificationCard(
            type: NotificationType.streak,
            content: '¡Tu racha de 12 días está en riesgo!',
            timestamp: 'hace 1h',
            read: false,
          ),

          _SectionHeader('Temporizador activo'),
          TimerCard(
            modeName: 'Pomodoro',
            progress: 0.65,
            timeDisplay: '15:32',
            isRunning: true,
            onPlayPause: () {},
          ),

          _SectionHeader('Hub de temporizadores'),
          TimerHubCard(
            icon: Icons.timer_rounded,
            title: 'Pomodoro',
            description: '25 min trabajo · 5 min descanso',
            onTap: () {},
          ),
          const SizedBox(height: 8),
          TimerHubCard(
            icon: Icons.fitness_center_rounded,
            title: 'Entrenamiento',
            description: 'Series y descansos personalizados',
            accentColor: Colors.orange,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          TimerHubCard(
            icon: Icons.self_improvement_rounded,
            title: 'Meditación',
            description: 'Sesiones guiadas de relajación',
            accentColor: Colors.purple,
            onTap: () {},
          ),
          const SizedBox(height: 8),
          TimerHubCard(
            icon: Icons.restaurant_rounded,
            title: 'Cocina',
            description: 'Temporizadores para recetas',
            accentColor: Colors.deepOrange,
            onTap: () {},
          ),

          _SectionHeader('Estadísticas de perfil'),
          const ProfileStatsCard(
            name: 'Mario García',
            username: 'mario_garcia',
            level: 8,
            currentXp: 640,
            xpToNextLevel: 1000,
            totalPoints: 12840,
            streak: 12,
            bestStreak: 18,
          ),

          _SectionHeader('Resumen semanal'),
          const WeeklyReactionCard(
            weekLabel: '23 jun – 29 jun',
            points: 1340,
            steps: 58200,
            tasks: 24,
            streak: 6,
            reactions: [
              WeeklyReaction(name: 'Ana López', emoji: '👍'),
              WeeklyReaction(name: 'Lucía Fdez', emoji: '🔥'),
              WeeklyReaction(name: 'Carlos Ruiz', emoji: '❤️'),
            ],
          ),

          _SectionHeader('Pasos de hoy'),
          const StepsCard(
            steps: 7432,
            goal: 10000,
            pointsEarned: 37,
          ),

          _SectionHeader('Resumen semanal de pasos'),
          const StepsSummaryCard(
            weeklySteps: [8210, 11430, 6800, 9340, 7432, 4200, 0],
            goal: 10000,
          ),

          _SectionHeader('Calendario'),
          const CalendarWeekRow(),

          _SectionHeader('Calendario mensual'),
          MonthCalendarCard(
            month: _catalogMonth,
            selectedDay: _catalogSelectedDay,
            today: DateTime(2026, 7, 3),
            taskCountByDay: const {1: 1, 5: 2, 10: 3, 18: 1, 25: 1},
            onPrevMonth: () => setState(
              () => _catalogMonth = DateTime(_catalogMonth.year, _catalogMonth.month - 1),
            ),
            onNextMonth: () => setState(
              () => _catalogMonth = DateTime(_catalogMonth.year, _catalogMonth.month + 1),
            ),
            onDaySelected: (d) => setState(() => _catalogSelectedDay = d),
          ),

          _SectionHeader('Chips de categoría y dificultad'),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final c in TaskCategory.values) TaskCategoryChip(category: c),
              for (final d in TaskDifficulty.values) DifficultyChip(difficulty: d),
            ],
          ),

          _SectionHeader('Filtro desplegable'),
          Row(
            children: [
              Expanded(
                child: DropdownPill<TaskDifficulty>(
                  label: 'Dificultad',
                  selected: _dropdownDifficulty,
                  onChanged: (v) => setState(() => _dropdownDifficulty = v),
                  items: [
                    const DropdownPillItem(value: null, label: 'Todas', icon: Icons.list_rounded),
                    for (final d in TaskDifficulty.values)
                      DropdownPillItem(
                        value: d,
                        label: d.label(context),
                        icon: Icons.circle,
                        color: d.color(Theme.of(context).colorScheme),
                      ),
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: DropdownPill<TaskCategory>(
                  label: 'Categoría',
                  selected: _dropdownCategory,
                  onChanged: (v) => setState(() => _dropdownCategory = v),
                  items: [
                    const DropdownPillItem(value: null, label: 'Todas', icon: Icons.list_rounded),
                    for (final c in TaskCategory.values)
                      DropdownPillItem(value: c, label: c.label(context), icon: c.icon, color: c.color),
                  ],
                ),
              ),
            ],
          ),

          _SectionHeader('Tarjeta de tarea del calendario'),
          CalendarTaskCard(
            title: 'Preparar presentación TFG',
            difficulty: TaskDifficulty.hard,
            category: TaskCategory.estudio,
            durationMinutes: 90,
            completed: _catalogTaskDone,
            onToggle: () => setState(() => _catalogTaskDone = !_catalogTaskDone),
            onEdit: () {},
            onDelete: () {},
          ),

          _SectionHeader('Resumen del día'),
          const DailySummaryCard(
            userName: 'Demo',
            streak: 7,
            stepsToday: 7432,
            stepsGoal: 10000,
            tasksCompleted: 5,
            tasksTotal: 8,
            pointsToday: 237,
            rankingPosition: 4,
          ),

          _SectionHeader('Gráfica de progreso'),
          const ProgressChartCard(
            pointsHistory: AppData.last30DaysPoints,
            stepsHistory: AppData.last30DaysSteps,
            tasksHistory: AppData.last30DaysTasks,
            dayLabels: AppData.last30DaysLabels,
          ),

          _SectionHeader('Progreso de pasos'),
          StepsProgressCard(
            steps: AppData.stepsToday,
            goal: _catalogStepsGoal,
            pointsEarnedToday: AppData.pointsTodayFromSteps,
            onConfigureGoal: () => setState(() => _catalogStepsGoal += 1000),
          ),

          _SectionHeader('Preset de cronómetro'),
          TimerPresetCard(preset: AppData.timerPresets.first, onPlay: () {}),

          _SectionHeader('Tarjeta de recordatorio'),
          ReminderCard(
            title: 'Estirar la espalda',
            dateTime: DateTime.now().add(const Duration(hours: 2)),
            enabled: _catalogReminderEnabled,
            onToggle: (v) => setState(() => _catalogReminderEnabled = v),
            onDelete: () {},
          ),

          _SectionHeader('Tarjeta de restricción tecnológica'),
          TechRestrictionCard(
            restriction: _catalogRestriction,
            onToggle: (v) => setState(() => _catalogRestriction.enabled = v),
            onDelete: () {},
          ),

          _SectionHeader('Sección plegable'),
          DayPilotCollapsibleSection(
            icon: Icons.list_alt_rounded,
            title: 'Detalles',
            children: [
              const SizedBox(height: 8),
              CategoryChipGroup(
                label: 'Categoría',
                selected: _catalogCategory,
                onChanged: (c) => setState(() => _catalogCategory = c),
              ),
              const SizedBox(height: 16),
              DifficultyField(
                label: 'Dificultad',
                value: _catalogDifficulty,
                onChanged: (d) => setState(() => _catalogDifficulty = d),
              ),
            ],
          ),

          _SectionHeader('Switch tile'),
          DayPilotFormSection(
            title: 'Opciones',
            children: [
              DayPilotSwitchTile(
                label: 'Notificaciones',
                subtitle: 'Recibir alertas y recordatorios',
                icon: Icons.notifications_outlined,
                value: _switchVal,
                onChanged: (v) => setState(() => _switchVal = v),
              ),
              DayPilotSwitchTile(
                label: 'Modo oscuro',
                icon: Icons.dark_mode_outlined,
                value: _switchVal2,
                onChanged: (v) => setState(() => _switchVal2 = v),
              ),
            ],
          ),

          _SectionHeader('Select field'),
          DayPilotSelectField<String>(
            label: 'Frecuencia',
            value: _selectVal,
            hint: 'Elige una opción',
            options: const ['Diario', 'Semanal', 'Mensual', 'Días laborables'],
            display: (s) => s,
            prefixIcon: Icons.repeat_rounded,
            onChanged: (v) => setState(() => _selectVal = v),
          ),

          _SectionHeader('Slider'),
          DayPilotFormSection(
            title: 'Límite de tiempo',
            children: [
              DayPilotSliderField(
                label: 'Límite diario',
                value: _sliderVal,
                min: 5,
                max: 120,
                divisions: 23,
                displayValue: (v) => '${v.toInt()} min',
                onChanged: (v) => setState(() => _sliderVal = v),
              ),
            ],
          ),

          _SectionHeader('Stepper'),
          DayPilotFormSection(
            title: 'Duración',
            children: [
              DayPilotStepper(
                label: 'Tiempo de trabajo',
                value: _stepperVal,
                min: 5,
                max: 90,
                step: 5,
                suffix: 'min',
                onChanged: (v) => setState(() => _stepperVal = v),
              ),
            ],
          ),

          _SectionHeader('Selector de fecha'),
          DayPilotDateField(
            label: 'Fecha límite',
            value: _pickedDate,
            onChanged: (d) => setState(() => _pickedDate = d),
          ),

          _SectionHeader('Selector de hora'),
          DayPilotTimeField(
            label: 'Hora de aviso',
            value: _pickedTime,
            onChanged: (t) => setState(() => _pickedTime = t),
          ),

          _SectionHeader('Grupo de radio'),
          DayPilotFormSection(
            title: 'Dificultad',
            children: [
              DayPilotRadioGroup<TaskDifficulty>(
                value: _radioVal,
                options: TaskDifficulty.values,
                display: (p) => switch (p) {
                  TaskDifficulty.easy   => 'Fácil — sin urgencia',
                  TaskDifficulty.medium => 'Media — importante',
                  TaskDifficulty.hard   => 'Difícil — urgente',
                },
                onChanged: (v) => setState(() => _radioVal = v),
              ),
            ],
          ),

          _SectionHeader('Grupo de chips (multi)'),
          DayPilotFormSection(
            title: 'Categorías',
            children: [
              DayPilotChipGroup<String>(
                options: const ['Personal', 'Trabajo', 'Salud', 'Estudio', 'Otro'],
                selected: _chipSelected,
                display: (s) => s,
                onChanged: (v) => setState(() => _chipSelected = v),
              ),
            ],
          ),
          const SizedBox(height: 12),
          _SectionHeader('Grupo de chips (selección única)'),
          DayPilotFormSection(
            title: 'Dificultad',
            children: [
              DayPilotChipGroup<TaskDifficulty>(
                options: TaskDifficulty.values,
                selected: _chipSingle,
                display: (p) => switch (p) {
                  TaskDifficulty.easy   => 'Fácil',
                  TaskDifficulty.medium => 'Media',
                  TaskDifficulty.hard   => 'Difícil',
                },
                singleSelect: true,
                onChanged: (v) => setState(() => _chipSingle = v),
              ),
            ],
          ),

          _SectionHeader('Selector de color'),
          DayPilotFormSection(
            title: 'Color de categoría',
            children: [
              DayPilotColorPicker(
                label: 'Elige un color',
                value: _pickedColor,
                colors: DayPilotColorPicker.defaultColors,
                onChanged: (c) => setState(() => _pickedColor = c),
              ),
            ],
          ),

          _SectionHeader('Sección de formulario'),
          DayPilotFormSection(
            title: 'Configuración del temporizador',
            children: [
              DayPilotStepper(
                label: 'Tiempo de trabajo',
                value: _stepperVal,
                min: 5,
                max: 90,
                step: 5,
                suffix: 'min',
                onChanged: (v) => setState(() => _stepperVal = v),
              ),
              DayPilotSwitchTile(
                label: 'Sonido al terminar',
                icon: Icons.volume_up_outlined,
                value: _switchVal,
                onChanged: (v) => setState(() => _switchVal = v),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

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
