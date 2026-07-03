// Datos de ejemplo (placeholder) para toda la app.
// Todo lo que se ve en pantalla sale de aquí: cuando haya backend real,
// basta con sustituir el contenido de esta clase por llamadas a la API.

import 'package:flutter/material.dart';
import '../components/basic/task_category.dart';
import '../components/basic/task_dot.dart';
import '../components/cards/notification_card.dart';
import '../components/cards/weekly_reaction_card.dart';

class AppFriend {
  final String name;
  final String email;
  final int points;
  final int streak;
  final int? weeklyPoints;
  final int? weeklyTasks;
  final int? weeklySteps;
  final int? weeklyStreak;
  final String? reactionSelected;

  const AppFriend(
    this.name,
    this.email,
    this.points,
    this.streak, {
    this.weeklyPoints,
    this.weeklyTasks,
    this.weeklySteps,
    this.weeklyStreak,
    this.reactionSelected,
  });
}

class AppFriendRequest {
  final String name;
  final String email;
  const AppFriendRequest(this.name, this.email);
}

class AppSearchResult {
  final String name;
  final String email;
  final bool isFriend;
  final bool isPending;
  const AppSearchResult(this.name, this.email, this.isFriend, this.isPending);
}

class AppNotification {
  final NotificationType type;
  final String content;
  final String time;
  final bool read;
  const AppNotification({
    required this.type,
    required this.content,
    required this.time,
    required this.read,
  });
}

class AppRankingUser {
  final String name;
  final int points;
  final int streak;
  final bool isCurrentUser;
  const AppRankingUser(this.name, this.points, this.streak, this.isCurrentUser);
}

class AppTask {
  final String id;
  final String title;
  final String? description;
  final TaskDifficulty difficulty;
  final TaskCategory category;
  final DateTime date;
  final int durationMinutes;
  final bool reminder;
  final bool recurring;
  final int repeatEveryDays;
  bool done;

  AppTask({
    required this.id,
    required this.title,
    this.description,
    required this.difficulty,
    required this.category,
    required this.date,
    required this.durationMinutes,
    this.reminder = false,
    this.recurring = false,
    this.repeatEveryDays = 1,
    this.done = false,
  });
}

enum TimerMode { pomodoro, fixed, custom }

class TimerPreset {
  final String id;
  final String name;
  final String description;
  final IconData icon;
  final Color color;
  final TimerMode mode;
  final int minutes; // fixed/custom: duración; pomodoro: minutos de trabajo
  final int restMinutes; // solo pomodoro
  final int sessions; // solo pomodoro

  const TimerPreset({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.color,
    required this.mode,
    required this.minutes,
    this.restMinutes = 0,
    this.sessions = 1,
  });
}

class AppReminder {
  final String id;
  final String title;
  final DateTime dateTime;
  final String frequency; // 'Una vez' | 'Diario' | 'Semanal'
  final bool notifyBefore;
  bool enabled;

  AppReminder({
    required this.id,
    required this.title,
    required this.dateTime,
    this.frequency = 'once',
    this.notifyBefore = false,
    this.enabled = true,
  });
}

enum RestrictionType { app, group }

class TechRestriction {
  final String id;
  final String name;
  final String identifier;
  final IconData icon;
  final Color color;
  final RestrictionType type;
  final int usedMinutesToday;
  final int limitMinutes;
  final int notifyIntervalSeconds;
  bool enabled;

  TechRestriction({
    required this.id,
    required this.name,
    required this.identifier,
    required this.icon,
    required this.color,
    this.type = RestrictionType.app,
    required this.usedMinutesToday,
    required this.limitMinutes,
    this.notifyIntervalSeconds = 3600,
    this.enabled = true,
  });
}

class AppData {
  AppData._();

  // ── Usuario actual (perfil) ──────────────────────────────────────
  static const currentUserName = 'Mario García';
  static const currentUserUsername = 'mario_garcia';
  static const currentUserEmail = 'unNobleXD@hotmail.com';
  static const currentUserMemberSince = 'jun 2024';
  static const currentUserLevel = 8;
  static const currentUserXp = 640;
  static const currentUserXpToNextLevel = 1000;
  static const currentUserTotalPoints = 12840;
  static const currentUserStreak = 12;
  static const currentUserBestStreak = 18;

  // ── Resumen de hoy ────────────────────────────────────────────────
  static const stepsToday = 7432;
  static const stepsGoal = 10000;
  static const tasksCompletedToday = 5;
  static const tasksTotalToday = 8;
  static const pointsToday = 237;
  static const rankingPositionToday = 4;
  static const pointsTodayFromTasks = 120;
  static const pointsTodayFromSteps = 62;
  static const pointsTodayFromHabits = 35;
  static const pointsTodayFromTimer = 20;

  // ── Progreso (últimos 30 días, cronológico: día 1 → hoy) ─────────
  static const List<double> last30DaysPoints = [
    64, 58, 70, 66, 60, 55, 62, 59, 53, 60, 57, 54, 58, 62, 65, 60, 57, 50, 33, 20, 8, 0, 45, 132,
  ];
  static const List<double> last30DaysSteps = [
    6200, 7400, 8100, 5600, 9200, 7800, 6900, 8400, 7100, 6600, 9800, 8700,
    7300, 6100, 8900, 7600, 5400, 4300, 3100, 1200, 0, 2100, 6700, 10200,
  ];
  static const List<double> last30DaysTasks = [
    4, 5, 6, 3, 5, 4, 6, 5, 3, 4, 5, 6, 4, 5, 6, 5, 4, 3, 2, 1, 0, 1, 4, 7,
  ];

  // ── Resumen de la semana pasada ─────────────────────────────────
  static const lastWeekLabel = 'Semana pasada';
  static const lastWeekPoints = 1640;
  static const lastWeekSteps = 58200;
  static const lastWeekTasks = 34;
  static const lastWeekStreak = 6;
  static const lastWeekReactions = [
    WeeklyReaction(name: 'Ana López', emoji: '🔥'),
    WeeklyReaction(name: 'Carlos Ruiz', emoji: '👍'),
    WeeklyReaction(name: 'Lucía Fdez', emoji: '⭐'),
  ];

  // ── Menú principal (home) ────────────────────────────────────────
  static const List<double> homeWeeklyPointsTrend = [120, 180, 95, 210, 160, 240, 237];
  static int get homeStepsProgressPercent =>
      stepsGoal > 0 ? ((stepsToday / stepsGoal) * 100).round() : 0;
  static const homeRivalryPosition = 2;
  static const homeRivalryTotal = 4;

  // ── Amigos ────────────────────────────────────────────────────────
  static const friends = [
    AppFriend('Carlos Ruiz', 'carlos.ruiz@daypilot.test', 80, 1,
        weeklyPoints: 145,
        weeklyTasks: 11,
        weeklySteps: 22878,
        weeklyStreak: 5,
        reactionSelected: '👏'),
    AppFriend('María López', 'maria.lopez@daypilot.test', 200, 4,
        weeklyPoints: 262, weeklyTasks: 17, weeklySteps: 30569, weeklyStreak: 4),
    AppFriend('Lucía Fernández', 'lucia.fernandez@daypilot.test', 430, 1,
        weeklyPoints: 401,
        weeklyTasks: 30,
        weeklySteps: 52111,
        weeklyStreak: 20,
        reactionSelected: '🔥'),
  ];

  static const friendRequests = [
    AppFriendRequest('Sofía Martín', 'sofia.martin@daypilot.test'),
  ];

  static const friendSearchResults = [
    AppSearchResult('Elena Vega', 'elena.vega@daypilot.test', false, false),
    AppSearchResult('Pedro Gómez', 'pedro.gomez@daypilot.test', false, false),
    AppSearchResult('Javier Prieto', 'javier.prieto@daypilot.test', false, true),
  ];

  // ── Avisos ────────────────────────────────────────────────────────
  static const notifications = [
    AppNotification(
      type: NotificationType.achievement,
      content: 'Nuevo logro desbloqueado: 7 días seguidos de actividad.',
      time: 'hace 5 min',
      read: false,
    ),
    AppNotification(
      type: NotificationType.social,
      content: 'sofia_mn quiere ser tu amiga.',
      time: 'hace 1 h',
      read: false,
    ),
    AppNotification(
      type: NotificationType.task,
      content: 'Revisar documentación de Flutter venció ayer.',
      time: 'ayer',
      read: true,
    ),
    AppNotification(
      type: NotificationType.streak,
      content: 'Tu racha de 12 días está en riesgo, completa una tarea hoy.',
      time: 'ayer',
      read: true,
    ),
    AppNotification(
      type: NotificationType.steps,
      content: 'Completaste tus 10 000 pasos diarios.',
      time: 'hace 2 días',
      read: true,
    ),
    AppNotification(
      type: NotificationType.reminder,
      content: 'Tu sesión de Pomodoro empieza en 10 minutos.',
      time: 'hace 3 días',
      read: true,
    ),
  ];

  // ── Rivalidad (ranking solo entre amigos) ────────────────────────
  static const rankingUsers = [
    AppRankingUser('Lucía Fernández', 430, 1, false),
    AppRankingUser('Mario García', 320, 12, true),
    AppRankingUser('María López', 200, 4, false),
    AppRankingUser('Carlos Ruiz', 80, 1, false),
  ];

  // ── Tareas del calendario (julio 2026) ───────────────────────────
  static final today = DateTime(2026, 7, 3);

  static List<AppTask> newTaskList() {
    DateTime d(int day) => DateTime(2026, 7, day);
    return [
      AppTask(
        id: '1',
        title: 'Prueba',
        difficulty: TaskDifficulty.hard,
        category: TaskCategory.otro,
        date: d(1),
        durationMinutes: 35,
        done: true,
      ),
      AppTask(
        id: '2',
        title: 'Reunión de equipo',
        difficulty: TaskDifficulty.medium,
        category: TaskCategory.trabajo,
        date: d(5),
        durationMinutes: 45,
      ),
      AppTask(
        id: '3',
        title: 'Revisar documentación de Flutter',
        difficulty: TaskDifficulty.easy,
        category: TaskCategory.estudio,
        date: d(5),
        durationMinutes: 60,
      ),
      AppTask(
        id: '4',
        title: 'Entrenamiento de piernas',
        difficulty: TaskDifficulty.hard,
        category: TaskCategory.deporte,
        date: d(8),
        durationMinutes: 50,
      ),
      AppTask(
        id: '5',
        title: 'Llamar al médico',
        difficulty: TaskDifficulty.easy,
        category: TaskCategory.salud,
        date: d(10),
        durationMinutes: 15,
        done: true,
      ),
      AppTask(
        id: '6',
        title: 'Comprar comida',
        difficulty: TaskDifficulty.easy,
        category: TaskCategory.hogar,
        date: d(10),
        durationMinutes: 30,
      ),
      AppTask(
        id: '7',
        title: 'Preparar presentación TFG',
        description: 'Slides y guion de la defensa',
        difficulty: TaskDifficulty.hard,
        category: TaskCategory.estudio,
        date: d(14),
        durationMinutes: 90,
        reminder: true,
      ),
      AppTask(
        id: '8',
        title: 'Yoga matutino',
        difficulty: TaskDifficulty.medium,
        category: TaskCategory.deporte,
        date: d(18),
        durationMinutes: 25,
        recurring: true,
      ),
      AppTask(
        id: '9',
        title: 'Organizar armario',
        difficulty: TaskDifficulty.easy,
        category: TaskCategory.hogar,
        date: d(22),
        durationMinutes: 40,
      ),
      AppTask(
        id: '10',
        title: 'Cita con el dentista',
        difficulty: TaskDifficulty.medium,
        category: TaskCategory.salud,
        date: d(25),
        durationMinutes: 30,
        reminder: true,
      ),
      AppTask(
        id: '11',
        title: 'Cerrar sprint',
        difficulty: TaskDifficulty.hard,
        category: TaskCategory.trabajo,
        date: d(30),
        durationMinutes: 60,
      ),
    ];
  }

  // ── Cronómetros ────────────────────────────────────────────────
  static const timerPresets = [
    TimerPreset(
      id: 'pomodoro',
      name: 'Pomodoro',
      description: '25 min trabajo + 5 min descanso por sesión',
      icon: Icons.timer_rounded,
      color: Color(0xFFE53935),
      mode: TimerMode.pomodoro,
      minutes: 25,
      restMinutes: 5,
      sessions: 4,
    ),
    TimerPreset(
      id: 'training',
      name: 'Training',
      description: '90 minutos de ejercicio',
      icon: Icons.fitness_center_rounded,
      color: Color(0xFF43A047),
      mode: TimerMode.fixed,
      minutes: 90,
    ),
    TimerPreset(
      id: 'meditation',
      name: 'Meditation',
      description: '60 minutos de concentración',
      icon: Icons.self_improvement_rounded,
      color: Color(0xFF7E57C2),
      mode: TimerMode.fixed,
      minutes: 60,
    ),
    TimerPreset(
      id: 'cooking',
      name: 'Cooking',
      description: '120 minutos en la cocina',
      icon: Icons.restaurant_rounded,
      color: Color(0xFFFB8C00),
      mode: TimerMode.fixed,
      minutes: 120,
    ),
    TimerPreset(
      id: 'custom',
      name: 'Custom',
      description: 'Elige tu propio tiempo',
      icon: Icons.tune_rounded,
      color: Color(0xFF00ACC1),
      mode: TimerMode.custom,
      minutes: 30,
    ),
  ];

  // ── Recordatorios ─────────────────────────────────────────────
  static List<AppReminder> newReminderList() => [];

  // ── Salud tecnológica ────────────────────────────────────────────
  static List<TechRestriction> newRestrictionList() => [
        TechRestriction(
          id: '1',
          name: 'AccuWeather',
          identifier: 'com.accuweather.android',
          icon: Icons.wb_sunny_rounded,
          color: const Color(0xFFFF7043),
          usedMinutesToday: 0,
          limitMinutes: 60,
          enabled: false,
        ),
        TechRestriction(
          id: '2',
          name: 'ALLORO ULPGC',
          identifier: 'com.ric.alloro',
          icon: Icons.public_rounded,
          color: const Color(0xFF42A5F5),
          usedMinutesToday: 0,
          limitMinutes: 60,
          enabled: false,
        ),
        TechRestriction(
          id: '3',
          name: 'YouTube',
          identifier: 'app.revanced.android.youtube',
          icon: Icons.play_circle_fill_rounded,
          color: const Color(0xFFE53935),
          usedMinutesToday: 36,
          limitMinutes: 30,
          enabled: false,
        ),
      ];

  static const mockInstallableApps = [
    ('AccuWeather', Icons.wb_sunny_rounded, Color(0xFFFF7043)),
    ('YouTube', Icons.play_circle_fill_rounded, Color(0xFFE53935)),
    ('Instagram', Icons.camera_alt_rounded, Color(0xFFD81B60)),
    ('TikTok', Icons.music_note_rounded, Color(0xFF212121)),
    ('Chrome', Icons.public_rounded, Color(0xFF42A5F5)),
    ('WhatsApp', Icons.chat_rounded, Color(0xFF43A047)),
  ];

  // ── Registro ──────────────────────────────────────────────────────
  static const timezoneOptions = [
    'Europe/Madrid',
    'Europe/London',
    'America/Mexico_City',
    'America/Bogota',
    'America/Argentina/Buenos_Aires',
  ];
}
