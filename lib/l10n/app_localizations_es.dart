// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get navInicio => 'Inicio';

  @override
  String get navAmigos => 'Amigos';

  @override
  String get navAvisos => 'Avisos';

  @override
  String get navPerfil => 'Perfil';

  @override
  String get commonCancel => 'Cancelar';

  @override
  String get commonSave => 'Guardar';

  @override
  String get commonSaveChanges => 'Guardar cambios';

  @override
  String get commonCreate => 'Crear';

  @override
  String get commonAdd => 'Añadir';

  @override
  String get commonEdit => 'Editar';

  @override
  String get commonDelete => 'Eliminar';

  @override
  String get commonStart => 'Iniciar';

  @override
  String get commonAccept => 'Aceptar';

  @override
  String get commonYes => 'Sí';

  @override
  String get commonNo => 'No';

  @override
  String get commonToday => 'Hoy';

  @override
  String get commonTomorrow => 'Mañana';

  @override
  String get commonSeeAll => 'Ver todo';

  @override
  String get commonOr => 'o';

  @override
  String get commonPassword => 'Contraseña';

  @override
  String get commonSteps => 'Pasos';

  @override
  String get commonTasks => 'Tareas';

  @override
  String get commonHabits => 'Hábitos';

  @override
  String get commonTimer => 'Cronómetro';

  @override
  String get commonPoints => 'Puntos';

  @override
  String get commonPointsToday => 'Puntos hoy';

  @override
  String get commonRanking => 'Ranking';

  @override
  String get commonTotal => 'Total';

  @override
  String get commonAverage => 'Media';

  @override
  String get commonBest => 'Mejor';

  @override
  String commonPointsSuffix(int points) {
    return '$points pts';
  }

  @override
  String get settingsTitle => 'Configuración';

  @override
  String get settingsColorTheme => 'Tema de color';

  @override
  String get settingsDarkMode => 'Modo oscuro';

  @override
  String get settingsDarkModeSubtitle => 'Activa el tema oscuro de la app';

  @override
  String get settingsNotifications => 'Notificaciones';

  @override
  String get settingsNotificationsSubtitle =>
      'Activar o desactivar notificaciones';

  @override
  String get settingsTaskReminders => 'Recordatorio de tareas';

  @override
  String get settingsTaskRemindersSubtitle =>
      'Aviso diario con tus tareas pendientes';

  @override
  String get settingsStreakAlert => 'Alerta de racha';

  @override
  String get settingsStreakAlertSubtitle =>
      'Aviso a las 22h si no has abierto la app';

  @override
  String get settingsEditProfile => 'Editar perfil';

  @override
  String get settingsEditProfileSubtitle =>
      'Nombre, usuario, región y contraseña';

  @override
  String get settingsLanguage => 'Idioma';

  @override
  String get settingsChooseLanguage => 'Elige un idioma';

  @override
  String get settingsDeveloper => 'Desarrollador';

  @override
  String get settingsComponentCatalog => 'Catálogo de componentes';

  @override
  String get settingsSignOut => 'Cerrar sesión';

  @override
  String get themeSageGreen => 'Verde Salvia';

  @override
  String get themeOcean => 'Océano';

  @override
  String get themeLavender => 'Morado Lavanda';

  @override
  String get themeAmber => 'Ámbar';

  @override
  String get themeAmoled => 'AMOLED';

  @override
  String get difficultyEasy => 'Fácil';

  @override
  String get difficultyMedium => 'Media';

  @override
  String get difficultyHard => 'Difícil';

  @override
  String get categoryTrabajo => 'Trabajo';

  @override
  String get categoryEstudio => 'Estudio';

  @override
  String get categoryDeporte => 'Deporte';

  @override
  String get categorySalud => 'Salud';

  @override
  String get categoryPersonal => 'Personal';

  @override
  String get categoryHogar => 'Hogar';

  @override
  String get categoryOtro => 'Otro';

  @override
  String get calendarTitle => 'Calendario';

  @override
  String get calendarDifficulty => 'Dificultad';

  @override
  String get calendarCategory => 'Categoría';

  @override
  String get calendarAll => 'Todas';

  @override
  String calendarTasksForDay(int day) {
    return 'Tareas del día $day';
  }

  @override
  String get calendarAdd => 'Añadir';

  @override
  String get calendarEmptyDay => 'No hay tareas para este día';

  @override
  String get stepsGoalTitle => 'Configurar meta de pasos';

  @override
  String stepsGoalValue(int goal) {
    return '$goal pasos';
  }

  @override
  String get stepsGoalQuickGoals => 'Metas rápidas';

  @override
  String get timerCustomTitle => 'Cronómetro personalizado';

  @override
  String timerMinutesValue(int minutes) {
    return '$minutes minutos';
  }

  @override
  String get timerPomodoroConfigTitle => 'Configurar Pomodoro';

  @override
  String get timerWork => 'Trabajo';

  @override
  String get timerRest => 'Descanso';

  @override
  String get timerTotal => 'Total';

  @override
  String get timerSessionsCount => 'Número de sesiones';

  @override
  String timerSessionsValue(int n) {
    return '$n sesiones';
  }

  @override
  String get timerStartPomodoro => 'Iniciar Pomodoro';

  @override
  String timerOneSession(int min) {
    return '1 sesión ($min min)';
  }

  @override
  String timerEightSessions(String h) {
    return '8 sesiones (${h}h)';
  }

  @override
  String timerMinValue(int min) {
    return '$min min';
  }

  @override
  String get taskEditTitle => 'Editar tarea';

  @override
  String get taskNewTitle => 'Nueva tarea';

  @override
  String get taskInfoSection => 'Información';

  @override
  String get taskTitleLabel => 'Título';

  @override
  String get taskTitleRequired => 'El título es obligatorio';

  @override
  String get taskDescriptionLabel => 'Descripción (opcional)';

  @override
  String get taskDetailsSection => 'Detalles';

  @override
  String get taskDurationEstimate => 'Duración estimada';

  @override
  String get taskMinSuffix => 'min';

  @override
  String get taskDaysSuffix => 'días';

  @override
  String get taskReminderSection => 'Recordatorio y repetición';

  @override
  String get taskActivateReminder => 'Activar recordatorio';

  @override
  String get taskReminderSubtitle => 'Recibirás una notificación';

  @override
  String get taskRecurring => 'Tarea recurrente';

  @override
  String get taskRecurringSubtitle => 'Se repite cada X días';

  @override
  String get taskRepeatEvery => 'Repetir cada';

  @override
  String get taskCreateButton => 'Crear tarea';

  @override
  String get taskErrorCreate => 'No se pudo crear la tarea.';

  @override
  String get taskErrorUpdate => 'No se pudo actualizar la tarea.';

  @override
  String get taskErrorToggle => 'No se pudo actualizar la tarea.';

  @override
  String get taskErrorDelete => 'No se pudo eliminar la tarea.';

  @override
  String get techRestrictionTypeApp => 'App';

  @override
  String get techRestrictionTypeGroup => 'Grupo';

  @override
  String techRestrictionUsageToday(int used, int limit) {
    return 'Hoy: $used / $limit min';
  }

  @override
  String techRestrictionNotifyEvery(int seconds) {
    return 'Notif: cada ${seconds}s';
  }

  @override
  String get techRestrictionDeletesTomorrow => '🗑️  Se elimina mañana';

  @override
  String get restrictionPickApps => 'Elegir apps';

  @override
  String get restrictionPickApp => 'Elegir app';

  @override
  String get restrictionDone => 'Listo';

  @override
  String get restrictionNewTitle => 'Nueva restricción';

  @override
  String get restrictionApplication => 'Aplicación';

  @override
  String get restrictionGroupName => 'Nombre del grupo';

  @override
  String get restrictionGroupApps => 'Apps del grupo';

  @override
  String restrictionAppsSelected(int n) {
    return '$n apps seleccionadas';
  }

  @override
  String restrictionDailyLimitRange(String min, String max) {
    return 'Límite diario ($min → $max)';
  }

  @override
  String restrictionSelected(String value) {
    return 'Seleccionado: $value';
  }

  @override
  String get reminderFrequencyOnce => 'Una vez';

  @override
  String get reminderFrequencyDaily => 'Diario';

  @override
  String get reminderFrequencyWeekly => 'Semanal';

  @override
  String get reminderNewTitle => 'Nuevo recordatorio';

  @override
  String get reminderNameLabel => 'Nombre del recordatorio';

  @override
  String get reminderQuickAccess => 'Acceso rápido';

  @override
  String get reminderOrPickDateTime => 'o elige fecha y hora';

  @override
  String get reminderNoDateSelected => 'Sin fecha seleccionada';

  @override
  String get reminderFrequency => 'Frecuencia';

  @override
  String get reminderNotifyBefore => 'Aviso previo';

  @override
  String get reminderNotifyBeforeSubtitle => 'Notifica 10 min antes';

  @override
  String get reminderCreateButton => 'Crear recordatorio';

  @override
  String get taskDetailReminderActive => 'Recordatorio activado';

  @override
  String get taskDetailMarkPending => 'Marcar como pendiente';

  @override
  String get taskDetailMarkDone => 'Marcar como completada';

  @override
  String get stepsUnitLower => 'pasos';

  @override
  String stepsGoalCaption(String goal) {
    return 'Meta: $goal pasos';
  }

  @override
  String stepsCompletedPercent(String percent) {
    return '$percent% completado';
  }

  @override
  String stepsPointsEarned(int points) {
    return '+$points pts';
  }

  @override
  String get stepsThisWeek => 'Esta semana';

  @override
  String stepsAveragePerDay(String avg) {
    return 'Media: $avg/día';
  }

  @override
  String stepsWeekTotal(String total) {
    return 'Total: $total pasos';
  }

  @override
  String get dailySummaryTitle => 'Resumen del día';

  @override
  String get profileInfoEmail => 'Email';

  @override
  String get profileInfoSince => 'Desde';

  @override
  String get profileInfoUsername => 'Username';

  @override
  String profileLevelBadge(int level) {
    return 'Nivel $level';
  }

  @override
  String profileLevelProgress(int level) {
    return 'Progreso nivel $level';
  }

  @override
  String profileXpProgress(int current, int toNext) {
    return '$current / $toNext pts';
  }

  @override
  String get profileTotalPoints => 'Puntos totales';

  @override
  String get profileCurrentStreak => 'Racha actual';

  @override
  String get profileBestStreak => 'Mejor racha';

  @override
  String get notifTypeSocial => 'Social';

  @override
  String get notifTypeStreak => 'Racha';

  @override
  String get notifTypeReminder => 'Recordatorios';

  @override
  String get notifTypeAchievement => 'Logros';

  @override
  String get timerPause => 'Pausar';

  @override
  String get stepsConfigureGoal => 'Configurar meta';

  @override
  String get stepsMilestones => 'Hitos';

  @override
  String stepsOfGoal(int goal) {
    return 'de $goal';
  }

  @override
  String get stepsPointsEarnedToday => 'Puntos ganados hoy';

  @override
  String get stepsNextGoal => 'Siguiente meta';

  @override
  String get notificationsMarkAllRead => 'Leer todo';

  @override
  String get friendCardRemoveTooltip => 'Eliminar amigo';

  @override
  String get friendCardNoActivity => 'Sin actividad la semana pasada';

  @override
  String get friendCardDecline => 'Rechazar';

  @override
  String get friendCardPending => 'Pendiente';

  @override
  String dailySummaryGreeting(String name) {
    return 'Hola, $name 👋';
  }

  @override
  String get dailySummarySubtitle => 'Tu resumen de hoy';

  @override
  String dailySummaryStreakDays(int days) {
    return '$days días';
  }

  @override
  String dailySummaryGoalCaption(String goal) {
    return 'meta $goal';
  }

  @override
  String get dailySummaryCompleted => 'completadas';

  @override
  String get dailySummaryPointsEarnedLabel => 'pts ganados';

  @override
  String get dailySummaryAmongFriends => 'entre amigos';

  @override
  String get weeklyReactionFriendsReactions => 'Reacciones de tus amigos';

  @override
  String get techHealthTitle => 'Salud Tecnológica';

  @override
  String get techHealthUnavailableTitle => 'No disponible en este dispositivo';

  @override
  String get techHealthUnavailableBody =>
      'La salud tecnológica necesita permisos de estadísticas de uso y accesibilidad que solo existen en Android. Por ahora esta función no está disponible en tu sistema — llegará cuando tengas la app instalada en el móvil.';

  @override
  String get techHealthPointDialogTitle => 'Punto de salud tecnológica';

  @override
  String get techHealthPointDialogBody =>
      'Activa al menos 3 restricciones y no superes ningún límite durante el día. Si lo consigues, ganarás 10 puntos extra que se sumarán a tus puntos de mañana.';

  @override
  String get techHealthPointBannerLabel =>
      'Punto obtenible — toca para saber más';

  @override
  String get techHealthRestrictionsTitle => 'Restricciones';

  @override
  String techHealthRestrictionsCount(int n) {
    return '$n en total';
  }

  @override
  String get permissionsTitle => 'Configurar permisos';

  @override
  String get permissionsIntro =>
      'Para funcionar correctamente, DayPilot necesita dos permisos. No se preocupe: solo se usan para lo que se explica aquí.';

  @override
  String get permissionsWarning =>
      'Estos son permisos importantes, pero no hay nada de qué preocuparse. La app solo los usa para medir el uso de tus apps y bloquearlas cuando superas el límite.';

  @override
  String get permissionsUsageAccessTitle => 'Acceso a estadísticas de uso';

  @override
  String get permissionsGranted => 'Concedido';

  @override
  String get permissionsUsageAccessBody =>
      'Mide cuántos minutos al día usas cada aplicación. Sin esto no es posible controlar los límites de tiempo.';

  @override
  String get permissionsAccessibilityTitle => 'Servicio de accesibilidad';

  @override
  String get permissionsAccessibilityBody =>
      'Detecta qué app está en primer plano y la cierra si has superado el límite. No lee texto ni interactúa con ninguna app.';

  @override
  String get permissionsPathSettings => 'Ajustes → ';

  @override
  String get permissionsPathAccessibility => 'Accesibilidad → ';

  @override
  String get permissionsPathInstalledServices => 'Servicios instalados → ';

  @override
  String get permissionsOpenAccessibility => 'Abrir accesibilidad';

  @override
  String get loginTagline => 'Vuela hacia tus metas';

  @override
  String get loginSignInTab => 'Iniciar sesión';

  @override
  String get loginSignUpTab => 'Crear cuenta';

  @override
  String get loginForgotPassword => '¿Has olvidado tu contraseña?';

  @override
  String get loginSubmit => 'Entrar';

  @override
  String get loginNameLabel => 'Nombre';

  @override
  String get loginUsernameLabel => 'Nombre de usuario';

  @override
  String get loginTimezoneLabel => 'Región / zona horaria';

  @override
  String get loginRegisterSubmit => 'Registrar';

  @override
  String get authErrorInvalidCredentials => 'Email o contraseña incorrectos.';

  @override
  String get authErrorEmailNotConfirmed =>
      'Confirma tu email antes de iniciar sesión.';

  @override
  String get authErrorAlreadyRegistered =>
      'Ya existe una cuenta con este email.';

  @override
  String get authErrorWeakPassword =>
      'La contraseña debe tener al menos 6 caracteres.';

  @override
  String get authErrorInvalidEmail => 'Introduce un email válido.';

  @override
  String get authErrorUnknown => 'Ha ocurrido un error. Inténtalo de nuevo.';

  @override
  String get forgotPasswordTitle => 'Recuperar contraseña';

  @override
  String get forgotPasswordSentBody =>
      'Revisa tu correo, te hemos enviado un enlace para restablecer tu contraseña.';

  @override
  String get forgotPasswordBody =>
      'Introduce tu email y te enviaremos un enlace para restablecer tu contraseña';

  @override
  String get forgotPasswordSendButton => 'Enviar enlace';

  @override
  String get forgotPasswordBackToLogin => 'Volver al inicio de sesión';

  @override
  String get rivalryTitle => 'Rivalidad';

  @override
  String get rivalryPointsThisMonth => 'Puntos de este mes';

  @override
  String get rivalryFullRanking => 'CLASIFICACIÓN COMPLETA';

  @override
  String get progressTitle => 'Progreso';

  @override
  String get habitsOtherHabits => 'Otros hábitos';

  @override
  String get habitsTimersTitle => 'Cronómetros';

  @override
  String get habitsTimersSubtitle => 'Pomodoro, entrenamiento y más';

  @override
  String get habitsTechHealthSubtitle => 'Límites por app / grupo + avisos';

  @override
  String get remindersTitle => 'Recordatorios';

  @override
  String get habitsRemindersSubtitle => 'Avisos, timers y rutinas';

  @override
  String get friendsRequestsTab => 'Solicitudes';

  @override
  String friendsRequestsTabCount(int n) {
    return 'Solicitudes ($n)';
  }

  @override
  String get friendsNoRequests => 'No tienes solicitudes pendientes';

  @override
  String get remindersEmptyState => 'No tienes recordatorios.\n¡Añade uno!';

  @override
  String homeTasksTodayLabel(int completed, int total) {
    return '$completed/$total tareas hoy';
  }

  @override
  String get searchFriendsTitle => 'Buscar amigos';

  @override
  String get searchFriendsHint => 'Buscar por nombre o email';

  @override
  String timerSessionOf(int current, int total) {
    return 'Sesión $current de $total';
  }

  @override
  String timerPhaseMinutes(int min, String phase) {
    return '$min min $phase';
  }

  @override
  String homeStepsProgressLabel(int percent) {
    return '$percent% pasos';
  }

  @override
  String get homeTimerPending => 'Cronómetro pendiente';
}
