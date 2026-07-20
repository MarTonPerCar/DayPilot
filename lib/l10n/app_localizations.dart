import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_de.dart';
import 'app_localizations_en.dart';
import 'app_localizations_es.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('de'),
    Locale('en'),
    Locale('es'),
  ];

  /// No description provided for @navInicio.
  ///
  /// In es, this message translates to:
  /// **'Inicio'**
  String get navInicio;

  /// No description provided for @navAmigos.
  ///
  /// In es, this message translates to:
  /// **'Amigos'**
  String get navAmigos;

  /// No description provided for @navAvisos.
  ///
  /// In es, this message translates to:
  /// **'Avisos'**
  String get navAvisos;

  /// No description provided for @navPerfil.
  ///
  /// In es, this message translates to:
  /// **'Perfil'**
  String get navPerfil;

  /// No description provided for @commonCancel.
  ///
  /// In es, this message translates to:
  /// **'Cancelar'**
  String get commonCancel;

  /// No description provided for @commonSave.
  ///
  /// In es, this message translates to:
  /// **'Guardar'**
  String get commonSave;

  /// No description provided for @commonSaveChanges.
  ///
  /// In es, this message translates to:
  /// **'Guardar cambios'**
  String get commonSaveChanges;

  /// No description provided for @commonCreate.
  ///
  /// In es, this message translates to:
  /// **'Crear'**
  String get commonCreate;

  /// No description provided for @commonAdd.
  ///
  /// In es, this message translates to:
  /// **'Añadir'**
  String get commonAdd;

  /// No description provided for @commonEdit.
  ///
  /// In es, this message translates to:
  /// **'Editar'**
  String get commonEdit;

  /// No description provided for @commonDelete.
  ///
  /// In es, this message translates to:
  /// **'Eliminar'**
  String get commonDelete;

  /// No description provided for @commonStart.
  ///
  /// In es, this message translates to:
  /// **'Iniciar'**
  String get commonStart;

  /// No description provided for @commonAccept.
  ///
  /// In es, this message translates to:
  /// **'Aceptar'**
  String get commonAccept;

  /// No description provided for @commonYes.
  ///
  /// In es, this message translates to:
  /// **'Sí'**
  String get commonYes;

  /// No description provided for @commonNo.
  ///
  /// In es, this message translates to:
  /// **'No'**
  String get commonNo;

  /// No description provided for @commonToday.
  ///
  /// In es, this message translates to:
  /// **'Hoy'**
  String get commonToday;

  /// No description provided for @commonTomorrow.
  ///
  /// In es, this message translates to:
  /// **'Mañana'**
  String get commonTomorrow;

  /// No description provided for @commonSeeAll.
  ///
  /// In es, this message translates to:
  /// **'Ver todo'**
  String get commonSeeAll;

  /// No description provided for @commonOr.
  ///
  /// In es, this message translates to:
  /// **'o'**
  String get commonOr;

  /// No description provided for @commonPassword.
  ///
  /// In es, this message translates to:
  /// **'Contraseña'**
  String get commonPassword;

  /// No description provided for @commonSteps.
  ///
  /// In es, this message translates to:
  /// **'Pasos'**
  String get commonSteps;

  /// No description provided for @commonTasks.
  ///
  /// In es, this message translates to:
  /// **'Tareas'**
  String get commonTasks;

  /// No description provided for @commonHabits.
  ///
  /// In es, this message translates to:
  /// **'Hábitos'**
  String get commonHabits;

  /// No description provided for @commonTimer.
  ///
  /// In es, this message translates to:
  /// **'Cronómetro'**
  String get commonTimer;

  /// No description provided for @commonPoints.
  ///
  /// In es, this message translates to:
  /// **'Puntos'**
  String get commonPoints;

  /// No description provided for @commonPointsToday.
  ///
  /// In es, this message translates to:
  /// **'Puntos hoy'**
  String get commonPointsToday;

  /// No description provided for @commonRanking.
  ///
  /// In es, this message translates to:
  /// **'Ranking'**
  String get commonRanking;

  /// No description provided for @commonTotal.
  ///
  /// In es, this message translates to:
  /// **'Total'**
  String get commonTotal;

  /// No description provided for @commonAverage.
  ///
  /// In es, this message translates to:
  /// **'Media'**
  String get commonAverage;

  /// No description provided for @commonBest.
  ///
  /// In es, this message translates to:
  /// **'Mejor'**
  String get commonBest;

  /// No description provided for @commonPointsSuffix.
  ///
  /// In es, this message translates to:
  /// **'{points} pts'**
  String commonPointsSuffix(int points);

  /// No description provided for @settingsTitle.
  ///
  /// In es, this message translates to:
  /// **'Configuración'**
  String get settingsTitle;

  /// No description provided for @settingsColorTheme.
  ///
  /// In es, this message translates to:
  /// **'Tema de color'**
  String get settingsColorTheme;

  /// No description provided for @settingsDarkMode.
  ///
  /// In es, this message translates to:
  /// **'Modo oscuro'**
  String get settingsDarkMode;

  /// No description provided for @settingsDarkModeSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Activa el tema oscuro de la app'**
  String get settingsDarkModeSubtitle;

  /// No description provided for @settingsNotifications.
  ///
  /// In es, this message translates to:
  /// **'Notificaciones'**
  String get settingsNotifications;

  /// No description provided for @settingsNotificationsSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Activar o desactivar notificaciones'**
  String get settingsNotificationsSubtitle;

  /// No description provided for @settingsTaskReminders.
  ///
  /// In es, this message translates to:
  /// **'Recordatorio de tareas'**
  String get settingsTaskReminders;

  /// No description provided for @settingsTaskRemindersSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Aviso diario con tus tareas pendientes'**
  String get settingsTaskRemindersSubtitle;

  /// No description provided for @settingsStreakAlert.
  ///
  /// In es, this message translates to:
  /// **'Alerta de racha'**
  String get settingsStreakAlert;

  /// No description provided for @settingsStreakAlertSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Aviso a las 22h si no has abierto la app'**
  String get settingsStreakAlertSubtitle;

  /// No description provided for @settingsLaunchAtStartup.
  ///
  /// In es, this message translates to:
  /// **'Iniciar con el sistema'**
  String get settingsLaunchAtStartup;

  /// No description provided for @settingsLaunchAtStartupSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Abre DayPilot automáticamente al iniciar sesión en tu PC'**
  String get settingsLaunchAtStartupSubtitle;

  /// No description provided for @settingsEditProfile.
  ///
  /// In es, this message translates to:
  /// **'Editar perfil'**
  String get settingsEditProfile;

  /// No description provided for @settingsEditProfileSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Nombre, usuario, región y contraseña'**
  String get settingsEditProfileSubtitle;

  /// No description provided for @settingsChangePhoto.
  ///
  /// In es, this message translates to:
  /// **'Cambiar foto'**
  String get settingsChangePhoto;

  /// No description provided for @settingsChangePhotoPrompt.
  ///
  /// In es, this message translates to:
  /// **'¿Desde dónde quieres elegir la foto?'**
  String get settingsChangePhotoPrompt;

  /// No description provided for @settingsGallery.
  ///
  /// In es, this message translates to:
  /// **'Galería'**
  String get settingsGallery;

  /// No description provided for @settingsCamera.
  ///
  /// In es, this message translates to:
  /// **'Cámara'**
  String get settingsCamera;

  /// No description provided for @settingsAdjustPhoto.
  ///
  /// In es, this message translates to:
  /// **'Ajustar foto'**
  String get settingsAdjustPhoto;

  /// No description provided for @settingsPersonalInfo.
  ///
  /// In es, this message translates to:
  /// **'Información personal'**
  String get settingsPersonalInfo;

  /// No description provided for @settingsSecurity.
  ///
  /// In es, this message translates to:
  /// **'Seguridad'**
  String get settingsSecurity;

  /// No description provided for @settingsChangePassword.
  ///
  /// In es, this message translates to:
  /// **'Cambiar contraseña'**
  String get settingsChangePassword;

  /// No description provided for @settingsNewPassword.
  ///
  /// In es, this message translates to:
  /// **'Nueva contraseña'**
  String get settingsNewPassword;

  /// No description provided for @settingsConfirmPassword.
  ///
  /// In es, this message translates to:
  /// **'Confirmar contraseña'**
  String get settingsConfirmPassword;

  /// No description provided for @settingsPasswordMismatch.
  ///
  /// In es, this message translates to:
  /// **'Las contraseñas no coinciden'**
  String get settingsPasswordMismatch;

  /// No description provided for @settingsPasswordChanged.
  ///
  /// In es, this message translates to:
  /// **'Contraseña actualizada'**
  String get settingsPasswordChanged;

  /// No description provided for @settingsAvatarUploadError.
  ///
  /// In es, this message translates to:
  /// **'No se pudo subir la foto'**
  String get settingsAvatarUploadError;

  /// No description provided for @settingsLanguage.
  ///
  /// In es, this message translates to:
  /// **'Idioma'**
  String get settingsLanguage;

  /// No description provided for @settingsChooseLanguage.
  ///
  /// In es, this message translates to:
  /// **'Elige un idioma'**
  String get settingsChooseLanguage;

  /// No description provided for @settingsDeveloper.
  ///
  /// In es, this message translates to:
  /// **'Desarrollador'**
  String get settingsDeveloper;

  /// No description provided for @settingsComponentCatalog.
  ///
  /// In es, this message translates to:
  /// **'Catálogo de componentes'**
  String get settingsComponentCatalog;

  /// No description provided for @settingsSignOut.
  ///
  /// In es, this message translates to:
  /// **'Cerrar sesión'**
  String get settingsSignOut;

  /// No description provided for @themeSageGreen.
  ///
  /// In es, this message translates to:
  /// **'Verde Salvia'**
  String get themeSageGreen;

  /// No description provided for @themeOcean.
  ///
  /// In es, this message translates to:
  /// **'Océano'**
  String get themeOcean;

  /// No description provided for @themeLavender.
  ///
  /// In es, this message translates to:
  /// **'Morado Lavanda'**
  String get themeLavender;

  /// No description provided for @themeAmber.
  ///
  /// In es, this message translates to:
  /// **'Ámbar'**
  String get themeAmber;

  /// No description provided for @themeAmoled.
  ///
  /// In es, this message translates to:
  /// **'AMOLED'**
  String get themeAmoled;

  /// No description provided for @difficultyEasy.
  ///
  /// In es, this message translates to:
  /// **'Fácil'**
  String get difficultyEasy;

  /// No description provided for @difficultyMedium.
  ///
  /// In es, this message translates to:
  /// **'Media'**
  String get difficultyMedium;

  /// No description provided for @difficultyHard.
  ///
  /// In es, this message translates to:
  /// **'Difícil'**
  String get difficultyHard;

  /// No description provided for @categoryTrabajo.
  ///
  /// In es, this message translates to:
  /// **'Trabajo'**
  String get categoryTrabajo;

  /// No description provided for @categoryEstudio.
  ///
  /// In es, this message translates to:
  /// **'Estudio'**
  String get categoryEstudio;

  /// No description provided for @categoryDeporte.
  ///
  /// In es, this message translates to:
  /// **'Deporte'**
  String get categoryDeporte;

  /// No description provided for @categorySalud.
  ///
  /// In es, this message translates to:
  /// **'Salud'**
  String get categorySalud;

  /// No description provided for @categoryPersonal.
  ///
  /// In es, this message translates to:
  /// **'Personal'**
  String get categoryPersonal;

  /// No description provided for @categoryHogar.
  ///
  /// In es, this message translates to:
  /// **'Hogar'**
  String get categoryHogar;

  /// No description provided for @categoryOtro.
  ///
  /// In es, this message translates to:
  /// **'Otro'**
  String get categoryOtro;

  /// No description provided for @calendarTitle.
  ///
  /// In es, this message translates to:
  /// **'Calendario'**
  String get calendarTitle;

  /// No description provided for @calendarDifficulty.
  ///
  /// In es, this message translates to:
  /// **'Dificultad'**
  String get calendarDifficulty;

  /// No description provided for @calendarCategory.
  ///
  /// In es, this message translates to:
  /// **'Categoría'**
  String get calendarCategory;

  /// No description provided for @calendarAll.
  ///
  /// In es, this message translates to:
  /// **'Todas'**
  String get calendarAll;

  /// No description provided for @calendarTasksForDay.
  ///
  /// In es, this message translates to:
  /// **'Tareas del día {day}'**
  String calendarTasksForDay(int day);

  /// No description provided for @calendarAdd.
  ///
  /// In es, this message translates to:
  /// **'Añadir'**
  String get calendarAdd;

  /// No description provided for @calendarEmptyDay.
  ///
  /// In es, this message translates to:
  /// **'No hay tareas para este día'**
  String get calendarEmptyDay;

  /// No description provided for @stepsGoalTitle.
  ///
  /// In es, this message translates to:
  /// **'Configurar meta de pasos'**
  String get stepsGoalTitle;

  /// No description provided for @stepsGoalValue.
  ///
  /// In es, this message translates to:
  /// **'{goal} pasos'**
  String stepsGoalValue(int goal);

  /// No description provided for @stepsGoalQuickGoals.
  ///
  /// In es, this message translates to:
  /// **'Metas rápidas'**
  String get stepsGoalQuickGoals;

  /// No description provided for @timerCustomTitle.
  ///
  /// In es, this message translates to:
  /// **'Cronómetro personalizado'**
  String get timerCustomTitle;

  /// No description provided for @timerMinutesValue.
  ///
  /// In es, this message translates to:
  /// **'{minutes} minutos'**
  String timerMinutesValue(int minutes);

  /// No description provided for @timerPomodoroConfigTitle.
  ///
  /// In es, this message translates to:
  /// **'Configurar Pomodoro'**
  String get timerPomodoroConfigTitle;

  /// No description provided for @timerWork.
  ///
  /// In es, this message translates to:
  /// **'Trabajo'**
  String get timerWork;

  /// No description provided for @timerRest.
  ///
  /// In es, this message translates to:
  /// **'Descanso'**
  String get timerRest;

  /// No description provided for @timerTotal.
  ///
  /// In es, this message translates to:
  /// **'Total'**
  String get timerTotal;

  /// No description provided for @timerSessionsCount.
  ///
  /// In es, this message translates to:
  /// **'Número de sesiones'**
  String get timerSessionsCount;

  /// No description provided for @timerSessionsValue.
  ///
  /// In es, this message translates to:
  /// **'{n} sesiones'**
  String timerSessionsValue(int n);

  /// No description provided for @timerStartPomodoro.
  ///
  /// In es, this message translates to:
  /// **'Iniciar Pomodoro'**
  String get timerStartPomodoro;

  /// No description provided for @timerOneSession.
  ///
  /// In es, this message translates to:
  /// **'1 sesión ({min} min)'**
  String timerOneSession(int min);

  /// No description provided for @timerEightSessions.
  ///
  /// In es, this message translates to:
  /// **'8 sesiones ({h}h)'**
  String timerEightSessions(String h);

  /// No description provided for @timerMinValue.
  ///
  /// In es, this message translates to:
  /// **'{min} min'**
  String timerMinValue(int min);

  /// No description provided for @taskEditTitle.
  ///
  /// In es, this message translates to:
  /// **'Editar tarea'**
  String get taskEditTitle;

  /// No description provided for @taskNewTitle.
  ///
  /// In es, this message translates to:
  /// **'Nueva tarea'**
  String get taskNewTitle;

  /// No description provided for @taskInfoSection.
  ///
  /// In es, this message translates to:
  /// **'Información'**
  String get taskInfoSection;

  /// No description provided for @taskTitleLabel.
  ///
  /// In es, this message translates to:
  /// **'Título'**
  String get taskTitleLabel;

  /// No description provided for @taskTitleRequired.
  ///
  /// In es, this message translates to:
  /// **'El título es obligatorio'**
  String get taskTitleRequired;

  /// No description provided for @taskDescriptionLabel.
  ///
  /// In es, this message translates to:
  /// **'Descripción (opcional)'**
  String get taskDescriptionLabel;

  /// No description provided for @taskDetailsSection.
  ///
  /// In es, this message translates to:
  /// **'Detalles'**
  String get taskDetailsSection;

  /// No description provided for @taskDurationEstimate.
  ///
  /// In es, this message translates to:
  /// **'Duración estimada'**
  String get taskDurationEstimate;

  /// No description provided for @taskMinSuffix.
  ///
  /// In es, this message translates to:
  /// **'min'**
  String get taskMinSuffix;

  /// No description provided for @taskDaysSuffix.
  ///
  /// In es, this message translates to:
  /// **'días'**
  String get taskDaysSuffix;

  /// No description provided for @taskReminderSection.
  ///
  /// In es, this message translates to:
  /// **'Recordatorio y repetición'**
  String get taskReminderSection;

  /// No description provided for @taskActivateReminder.
  ///
  /// In es, this message translates to:
  /// **'Activar recordatorio'**
  String get taskActivateReminder;

  /// No description provided for @taskReminderSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Recibirás una notificación'**
  String get taskReminderSubtitle;

  /// No description provided for @taskRecurring.
  ///
  /// In es, this message translates to:
  /// **'Tarea recurrente'**
  String get taskRecurring;

  /// No description provided for @taskRecurringSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Se repite cada X días'**
  String get taskRecurringSubtitle;

  /// No description provided for @taskRepeatEvery.
  ///
  /// In es, this message translates to:
  /// **'Repetir cada'**
  String get taskRepeatEvery;

  /// No description provided for @taskCreateButton.
  ///
  /// In es, this message translates to:
  /// **'Crear tarea'**
  String get taskCreateButton;

  /// No description provided for @taskErrorCreate.
  ///
  /// In es, this message translates to:
  /// **'No se pudo crear la tarea.'**
  String get taskErrorCreate;

  /// No description provided for @taskErrorUpdate.
  ///
  /// In es, this message translates to:
  /// **'No se pudo actualizar la tarea.'**
  String get taskErrorUpdate;

  /// No description provided for @taskErrorToggle.
  ///
  /// In es, this message translates to:
  /// **'No se pudo actualizar la tarea.'**
  String get taskErrorToggle;

  /// No description provided for @taskErrorDelete.
  ///
  /// In es, this message translates to:
  /// **'No se pudo eliminar la tarea.'**
  String get taskErrorDelete;

  /// No description provided for @techRestrictionTypeApp.
  ///
  /// In es, this message translates to:
  /// **'App'**
  String get techRestrictionTypeApp;

  /// No description provided for @techRestrictionTypeGroup.
  ///
  /// In es, this message translates to:
  /// **'Grupo'**
  String get techRestrictionTypeGroup;

  /// No description provided for @techRestrictionUsageToday.
  ///
  /// In es, this message translates to:
  /// **'Hoy: {used} / {limit} min'**
  String techRestrictionUsageToday(int used, int limit);

  /// No description provided for @techRestrictionNotifyEvery.
  ///
  /// In es, this message translates to:
  /// **'Notif: cada {seconds}s'**
  String techRestrictionNotifyEvery(int seconds);

  /// No description provided for @techRestrictionDeletesTomorrow.
  ///
  /// In es, this message translates to:
  /// **'🗑️  Se elimina mañana'**
  String get techRestrictionDeletesTomorrow;

  /// No description provided for @restrictionPickApps.
  ///
  /// In es, this message translates to:
  /// **'Elegir apps'**
  String get restrictionPickApps;

  /// No description provided for @restrictionPickApp.
  ///
  /// In es, this message translates to:
  /// **'Elegir app'**
  String get restrictionPickApp;

  /// No description provided for @restrictionDone.
  ///
  /// In es, this message translates to:
  /// **'Listo'**
  String get restrictionDone;

  /// No description provided for @restrictionNewTitle.
  ///
  /// In es, this message translates to:
  /// **'Nueva restricción'**
  String get restrictionNewTitle;

  /// No description provided for @restrictionApplication.
  ///
  /// In es, this message translates to:
  /// **'Aplicación'**
  String get restrictionApplication;

  /// No description provided for @restrictionGroupName.
  ///
  /// In es, this message translates to:
  /// **'Nombre del grupo'**
  String get restrictionGroupName;

  /// No description provided for @restrictionGroupApps.
  ///
  /// In es, this message translates to:
  /// **'Apps del grupo'**
  String get restrictionGroupApps;

  /// No description provided for @restrictionAppsSelected.
  ///
  /// In es, this message translates to:
  /// **'{n} apps seleccionadas'**
  String restrictionAppsSelected(int n);

  /// No description provided for @restrictionDailyLimitRange.
  ///
  /// In es, this message translates to:
  /// **'Límite diario ({min} → {max})'**
  String restrictionDailyLimitRange(String min, String max);

  /// No description provided for @restrictionSelected.
  ///
  /// In es, this message translates to:
  /// **'Seleccionado: {value}'**
  String restrictionSelected(String value);

  /// No description provided for @reminderFrequencyOnce.
  ///
  /// In es, this message translates to:
  /// **'Una vez'**
  String get reminderFrequencyOnce;

  /// No description provided for @reminderFrequencyDaily.
  ///
  /// In es, this message translates to:
  /// **'Diario'**
  String get reminderFrequencyDaily;

  /// No description provided for @reminderFrequencyWeekly.
  ///
  /// In es, this message translates to:
  /// **'Semanal'**
  String get reminderFrequencyWeekly;

  /// No description provided for @reminderNewTitle.
  ///
  /// In es, this message translates to:
  /// **'Nuevo recordatorio'**
  String get reminderNewTitle;

  /// No description provided for @reminderNameLabel.
  ///
  /// In es, this message translates to:
  /// **'Nombre del recordatorio'**
  String get reminderNameLabel;

  /// No description provided for @reminderQuickAccess.
  ///
  /// In es, this message translates to:
  /// **'Acceso rápido'**
  String get reminderQuickAccess;

  /// No description provided for @reminderOrPickDateTime.
  ///
  /// In es, this message translates to:
  /// **'o elige fecha y hora'**
  String get reminderOrPickDateTime;

  /// No description provided for @reminderNoDateSelected.
  ///
  /// In es, this message translates to:
  /// **'Sin fecha seleccionada'**
  String get reminderNoDateSelected;

  /// No description provided for @reminderFrequency.
  ///
  /// In es, this message translates to:
  /// **'Frecuencia'**
  String get reminderFrequency;

  /// No description provided for @reminderNotifyBefore.
  ///
  /// In es, this message translates to:
  /// **'Aviso previo'**
  String get reminderNotifyBefore;

  /// No description provided for @reminderNotifyBeforeSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Notifica 10 min antes'**
  String get reminderNotifyBeforeSubtitle;

  /// No description provided for @reminderCreateButton.
  ///
  /// In es, this message translates to:
  /// **'Crear recordatorio'**
  String get reminderCreateButton;

  /// No description provided for @taskDetailReminderActive.
  ///
  /// In es, this message translates to:
  /// **'Recordatorio activado'**
  String get taskDetailReminderActive;

  /// No description provided for @taskDetailMarkPending.
  ///
  /// In es, this message translates to:
  /// **'Marcar como pendiente'**
  String get taskDetailMarkPending;

  /// No description provided for @taskDetailMarkDone.
  ///
  /// In es, this message translates to:
  /// **'Marcar como completada'**
  String get taskDetailMarkDone;

  /// No description provided for @stepsUnitLower.
  ///
  /// In es, this message translates to:
  /// **'pasos'**
  String get stepsUnitLower;

  /// No description provided for @stepsGoalCaption.
  ///
  /// In es, this message translates to:
  /// **'Meta: {goal} pasos'**
  String stepsGoalCaption(String goal);

  /// No description provided for @stepsCompletedPercent.
  ///
  /// In es, this message translates to:
  /// **'{percent}% completado'**
  String stepsCompletedPercent(String percent);

  /// No description provided for @stepsPointsEarned.
  ///
  /// In es, this message translates to:
  /// **'+{points} pts'**
  String stepsPointsEarned(int points);

  /// No description provided for @stepsThisWeek.
  ///
  /// In es, this message translates to:
  /// **'Esta semana'**
  String get stepsThisWeek;

  /// No description provided for @stepsAveragePerDay.
  ///
  /// In es, this message translates to:
  /// **'Media: {avg}/día'**
  String stepsAveragePerDay(String avg);

  /// No description provided for @stepsWeekTotal.
  ///
  /// In es, this message translates to:
  /// **'Total: {total} pasos'**
  String stepsWeekTotal(String total);

  /// No description provided for @dailySummaryTitle.
  ///
  /// In es, this message translates to:
  /// **'Resumen del día'**
  String get dailySummaryTitle;

  /// No description provided for @profileInfoEmail.
  ///
  /// In es, this message translates to:
  /// **'Email'**
  String get profileInfoEmail;

  /// No description provided for @profileInfoSince.
  ///
  /// In es, this message translates to:
  /// **'Desde'**
  String get profileInfoSince;

  /// No description provided for @profileInfoUsername.
  ///
  /// In es, this message translates to:
  /// **'Username'**
  String get profileInfoUsername;

  /// No description provided for @profileLevelBadge.
  ///
  /// In es, this message translates to:
  /// **'Nivel {level}'**
  String profileLevelBadge(int level);

  /// No description provided for @profileLevelProgress.
  ///
  /// In es, this message translates to:
  /// **'Progreso nivel {level}'**
  String profileLevelProgress(int level);

  /// No description provided for @profileXpProgress.
  ///
  /// In es, this message translates to:
  /// **'{current} / {toNext} pts'**
  String profileXpProgress(int current, int toNext);

  /// No description provided for @profileTotalPoints.
  ///
  /// In es, this message translates to:
  /// **'Puntos totales'**
  String get profileTotalPoints;

  /// No description provided for @profileCurrentStreak.
  ///
  /// In es, this message translates to:
  /// **'Racha actual'**
  String get profileCurrentStreak;

  /// No description provided for @profileBestStreak.
  ///
  /// In es, this message translates to:
  /// **'Mejor racha'**
  String get profileBestStreak;

  /// No description provided for @notifTypeSocial.
  ///
  /// In es, this message translates to:
  /// **'Social'**
  String get notifTypeSocial;

  /// No description provided for @notifTypeStreak.
  ///
  /// In es, this message translates to:
  /// **'Racha'**
  String get notifTypeStreak;

  /// No description provided for @notifTypeReminder.
  ///
  /// In es, this message translates to:
  /// **'Recordatorios'**
  String get notifTypeReminder;

  /// No description provided for @notifTypeAchievement.
  ///
  /// In es, this message translates to:
  /// **'Logros'**
  String get notifTypeAchievement;

  /// No description provided for @notifTimeJustNow.
  ///
  /// In es, this message translates to:
  /// **'ahora mismo'**
  String get notifTimeJustNow;

  /// No description provided for @notifTimeMinutesAgo.
  ///
  /// In es, this message translates to:
  /// **'hace {n} min'**
  String notifTimeMinutesAgo(int n);

  /// No description provided for @notifTimeHoursAgo.
  ///
  /// In es, this message translates to:
  /// **'hace {n} h'**
  String notifTimeHoursAgo(int n);

  /// No description provided for @notifTimeYesterday.
  ///
  /// In es, this message translates to:
  /// **'ayer'**
  String get notifTimeYesterday;

  /// No description provided for @notifTimeDaysAgo.
  ///
  /// In es, this message translates to:
  /// **'hace {n} días'**
  String notifTimeDaysAgo(int n);

  /// No description provided for @notifFriendRequestTitle.
  ///
  /// In es, this message translates to:
  /// **'Nueva solicitud de amistad'**
  String get notifFriendRequestTitle;

  /// No description provided for @notifFriendRequestBody.
  ///
  /// In es, this message translates to:
  /// **'{username} quiere ser tu amigo.'**
  String notifFriendRequestBody(String username);

  /// No description provided for @notifFriendAcceptedTitle.
  ///
  /// In es, this message translates to:
  /// **'Solicitud aceptada'**
  String get notifFriendAcceptedTitle;

  /// No description provided for @notifFriendAcceptedBody.
  ///
  /// In es, this message translates to:
  /// **'{username} aceptó tu solicitud de amistad.'**
  String notifFriendAcceptedBody(String username);

  /// No description provided for @notifReactionTitle.
  ///
  /// In es, this message translates to:
  /// **'Nueva reacción'**
  String get notifReactionTitle;

  /// No description provided for @notifReactionBody.
  ///
  /// In es, this message translates to:
  /// **'{username} reaccionó a tu resumen semanal con {emoji}.'**
  String notifReactionBody(String username, String emoji);

  /// No description provided for @notifTimerDoneTitle.
  ///
  /// In es, this message translates to:
  /// **'¡Temporizador completado! ⏱'**
  String get notifTimerDoneTitle;

  /// No description provided for @notifTimerDoneBody.
  ///
  /// In es, this message translates to:
  /// **'Has completado una sesión de concentración y ganado 10 pts.'**
  String get notifTimerDoneBody;

  /// No description provided for @notifLevelUpTitle.
  ///
  /// In es, this message translates to:
  /// **'¡Subiste de nivel! 🏆'**
  String get notifLevelUpTitle;

  /// No description provided for @notifLevelUpBody.
  ///
  /// In es, this message translates to:
  /// **'Ahora eres nivel {level}. ¡Sigue así!'**
  String notifLevelUpBody(int level);

  /// No description provided for @notifStepsGoalTitle.
  ///
  /// In es, this message translates to:
  /// **'¡Objetivo completado! 🎉'**
  String get notifStepsGoalTitle;

  /// No description provided for @notifStepsGoalBody.
  ///
  /// In es, this message translates to:
  /// **'Has alcanzado tu objetivo de pasos (+30 pts).'**
  String get notifStepsGoalBody;

  /// No description provided for @notifTaskCompletedTitle.
  ///
  /// In es, this message translates to:
  /// **'¡Tarea completada! ✅'**
  String get notifTaskCompletedTitle;

  /// No description provided for @notifTaskCompletedBody.
  ///
  /// In es, this message translates to:
  /// **'Completaste \"{title}\" y ganaste 20 pts.'**
  String notifTaskCompletedBody(String title);

  /// No description provided for @notifTaskReminderTitle.
  ///
  /// In es, this message translates to:
  /// **'Tareas de hoy'**
  String get notifTaskReminderTitle;

  /// No description provided for @notifTaskReminderCount.
  ///
  /// In es, this message translates to:
  /// **'Tienes {count} tareas pendientes hoy'**
  String notifTaskReminderCount(int count);

  /// No description provided for @notifTaskReminderNone.
  ///
  /// In es, this message translates to:
  /// **'No tienes tareas para hoy ✓'**
  String get notifTaskReminderNone;

  /// No description provided for @notifTaskReminderGeneric.
  ///
  /// In es, this message translates to:
  /// **'Revisa tus tareas para hoy'**
  String get notifTaskReminderGeneric;

  /// No description provided for @notifStreakDangerTitle.
  ///
  /// In es, this message translates to:
  /// **'¡Racha en peligro! 🔥'**
  String get notifStreakDangerTitle;

  /// No description provided for @notifStreakDangerBody.
  ///
  /// In es, this message translates to:
  /// **'Abre la app antes de medianoche para mantener tu racha'**
  String get notifStreakDangerBody;

  /// No description provided for @timerPause.
  ///
  /// In es, this message translates to:
  /// **'Pausar'**
  String get timerPause;

  /// No description provided for @stepsConfigureGoal.
  ///
  /// In es, this message translates to:
  /// **'Configurar meta'**
  String get stepsConfigureGoal;

  /// No description provided for @stepsMilestones.
  ///
  /// In es, this message translates to:
  /// **'Hitos'**
  String get stepsMilestones;

  /// No description provided for @stepsOfGoal.
  ///
  /// In es, this message translates to:
  /// **'de {goal}'**
  String stepsOfGoal(int goal);

  /// No description provided for @stepsPointsEarnedToday.
  ///
  /// In es, this message translates to:
  /// **'Puntos ganados hoy'**
  String get stepsPointsEarnedToday;

  /// No description provided for @stepsNextGoal.
  ///
  /// In es, this message translates to:
  /// **'Siguiente meta'**
  String get stepsNextGoal;

  /// No description provided for @stepsPendingGoal.
  ///
  /// In es, this message translates to:
  /// **'Meta pendiente: {goal} pasos (activa mañana)'**
  String stepsPendingGoal(int goal);

  /// No description provided for @notificationsMarkAllRead.
  ///
  /// In es, this message translates to:
  /// **'Leer todo'**
  String get notificationsMarkAllRead;

  /// No description provided for @notificationsEmpty.
  ///
  /// In es, this message translates to:
  /// **'No tienes avisos'**
  String get notificationsEmpty;

  /// No description provided for @friendCardRemoveTooltip.
  ///
  /// In es, this message translates to:
  /// **'Eliminar amigo'**
  String get friendCardRemoveTooltip;

  /// No description provided for @friendCardNoActivity.
  ///
  /// In es, this message translates to:
  /// **'Sin actividad la semana pasada'**
  String get friendCardNoActivity;

  /// No description provided for @friendCardDecline.
  ///
  /// In es, this message translates to:
  /// **'Rechazar'**
  String get friendCardDecline;

  /// No description provided for @friendCardPending.
  ///
  /// In es, this message translates to:
  /// **'Pendiente'**
  String get friendCardPending;

  /// No description provided for @dailySummaryGreeting.
  ///
  /// In es, this message translates to:
  /// **'Hola, {name} 👋'**
  String dailySummaryGreeting(String name);

  /// No description provided for @dailySummarySubtitle.
  ///
  /// In es, this message translates to:
  /// **'Tu resumen de hoy'**
  String get dailySummarySubtitle;

  /// No description provided for @dailySummaryStreakDays.
  ///
  /// In es, this message translates to:
  /// **'{days} días'**
  String dailySummaryStreakDays(int days);

  /// No description provided for @dailySummaryGoalCaption.
  ///
  /// In es, this message translates to:
  /// **'meta {goal}'**
  String dailySummaryGoalCaption(String goal);

  /// No description provided for @dailySummaryCompleted.
  ///
  /// In es, this message translates to:
  /// **'completadas'**
  String get dailySummaryCompleted;

  /// No description provided for @dailySummaryPointsEarnedLabel.
  ///
  /// In es, this message translates to:
  /// **'pts ganados'**
  String get dailySummaryPointsEarnedLabel;

  /// No description provided for @dailySummaryAmongFriends.
  ///
  /// In es, this message translates to:
  /// **'entre amigos'**
  String get dailySummaryAmongFriends;

  /// No description provided for @weeklyReactionFriendsReactions.
  ///
  /// In es, this message translates to:
  /// **'Reacciones de tus amigos'**
  String get weeklyReactionFriendsReactions;

  /// No description provided for @weeklySummaryLastWeek.
  ///
  /// In es, this message translates to:
  /// **'Semana pasada'**
  String get weeklySummaryLastWeek;

  /// No description provided for @techHealthTitle.
  ///
  /// In es, this message translates to:
  /// **'Salud Tecnológica'**
  String get techHealthTitle;

  /// No description provided for @techHealthUnavailableTitle.
  ///
  /// In es, this message translates to:
  /// **'Aún no añadido en esta plataforma'**
  String get techHealthUnavailableTitle;

  /// No description provided for @techHealthUnavailableBody.
  ///
  /// In es, this message translates to:
  /// **'La salud tecnológica necesita vigilar el uso de apps y bloquearlas en tiempo real, algo que por ahora solo está implementado en Android. Añadirlo en otras plataformas es técnicamente posible, pero todavía no se ha hecho — por ahora esta función solo funciona en Android.'**
  String get techHealthUnavailableBody;

  /// No description provided for @techHealthPointDialogTitle.
  ///
  /// In es, this message translates to:
  /// **'Punto de salud tecnológica'**
  String get techHealthPointDialogTitle;

  /// No description provided for @techHealthPointDialogBody.
  ///
  /// In es, this message translates to:
  /// **'Activa al menos 3 restricciones y no superes ningún límite durante el día. Si lo consigues, ganarás 10 puntos extra que se sumarán a tus puntos de mañana.'**
  String get techHealthPointDialogBody;

  /// No description provided for @techHealthPointBannerLabel.
  ///
  /// In es, this message translates to:
  /// **'Punto obtenible — toca para saber más'**
  String get techHealthPointBannerLabel;

  /// No description provided for @techHealthPointLostLabel.
  ///
  /// In es, this message translates to:
  /// **'Punto perdido hoy — toca para saber más'**
  String get techHealthPointLostLabel;

  /// No description provided for @techHealthPointLostBody.
  ///
  /// In es, this message translates to:
  /// **'Has superado el límite de alguna restricción hoy, así que no ganarás el punto extra. Vuelve a intentarlo mañana.'**
  String get techHealthPointLostBody;

  /// No description provided for @techHealthRestrictionsTitle.
  ///
  /// In es, this message translates to:
  /// **'Restricciones'**
  String get techHealthRestrictionsTitle;

  /// No description provided for @techHealthRestrictionsCount.
  ///
  /// In es, this message translates to:
  /// **'{n} en total'**
  String techHealthRestrictionsCount(int n);

  /// No description provided for @permissionsTitle.
  ///
  /// In es, this message translates to:
  /// **'Configurar permisos'**
  String get permissionsTitle;

  /// No description provided for @permissionsIntro.
  ///
  /// In es, this message translates to:
  /// **'Para funcionar correctamente, DayPilot necesita dos permisos. No se preocupe: solo se usan para lo que se explica aquí.'**
  String get permissionsIntro;

  /// No description provided for @permissionsWarning.
  ///
  /// In es, this message translates to:
  /// **'Estos son permisos importantes, pero no hay nada de qué preocuparse. La app solo los usa para medir el uso de tus apps y bloquearlas cuando superas el límite.'**
  String get permissionsWarning;

  /// No description provided for @permissionsUsageAccessTitle.
  ///
  /// In es, this message translates to:
  /// **'Acceso a estadísticas de uso'**
  String get permissionsUsageAccessTitle;

  /// No description provided for @permissionsGranted.
  ///
  /// In es, this message translates to:
  /// **'Concedido'**
  String get permissionsGranted;

  /// No description provided for @permissionsUsageAccessBody.
  ///
  /// In es, this message translates to:
  /// **'Mide cuántos minutos al día usas cada aplicación. Sin esto no es posible controlar los límites de tiempo.'**
  String get permissionsUsageAccessBody;

  /// No description provided for @permissionsAccessibilityTitle.
  ///
  /// In es, this message translates to:
  /// **'Servicio de accesibilidad'**
  String get permissionsAccessibilityTitle;

  /// No description provided for @permissionsAccessibilityBody.
  ///
  /// In es, this message translates to:
  /// **'Detecta qué app está en primer plano y la cierra si has superado el límite. No lee texto ni interactúa con ninguna app.'**
  String get permissionsAccessibilityBody;

  /// No description provided for @permissionsPathSettings.
  ///
  /// In es, this message translates to:
  /// **'Ajustes → '**
  String get permissionsPathSettings;

  /// No description provided for @permissionsPathAccessibility.
  ///
  /// In es, this message translates to:
  /// **'Accesibilidad → '**
  String get permissionsPathAccessibility;

  /// No description provided for @permissionsPathInstalledServices.
  ///
  /// In es, this message translates to:
  /// **'Servicios instalados → '**
  String get permissionsPathInstalledServices;

  /// No description provided for @permissionsOpenAccessibility.
  ///
  /// In es, this message translates to:
  /// **'Abrir accesibilidad'**
  String get permissionsOpenAccessibility;

  /// No description provided for @loginTagline.
  ///
  /// In es, this message translates to:
  /// **'Vuela hacia tus metas'**
  String get loginTagline;

  /// No description provided for @loginSignInTab.
  ///
  /// In es, this message translates to:
  /// **'Iniciar sesión'**
  String get loginSignInTab;

  /// No description provided for @loginSignUpTab.
  ///
  /// In es, this message translates to:
  /// **'Crear cuenta'**
  String get loginSignUpTab;

  /// No description provided for @loginForgotPassword.
  ///
  /// In es, this message translates to:
  /// **'¿Has olvidado tu contraseña?'**
  String get loginForgotPassword;

  /// No description provided for @loginSubmit.
  ///
  /// In es, this message translates to:
  /// **'Entrar'**
  String get loginSubmit;

  /// No description provided for @loginNameLabel.
  ///
  /// In es, this message translates to:
  /// **'Nombre'**
  String get loginNameLabel;

  /// No description provided for @loginUsernameLabel.
  ///
  /// In es, this message translates to:
  /// **'Nombre de usuario'**
  String get loginUsernameLabel;

  /// No description provided for @loginTimezoneLabel.
  ///
  /// In es, this message translates to:
  /// **'Región / zona horaria'**
  String get loginTimezoneLabel;

  /// No description provided for @loginRegisterSubmit.
  ///
  /// In es, this message translates to:
  /// **'Registrar'**
  String get loginRegisterSubmit;

  /// No description provided for @authErrorInvalidCredentials.
  ///
  /// In es, this message translates to:
  /// **'Email o contraseña incorrectos.'**
  String get authErrorInvalidCredentials;

  /// No description provided for @authErrorEmailNotConfirmed.
  ///
  /// In es, this message translates to:
  /// **'Confirma tu email antes de iniciar sesión.'**
  String get authErrorEmailNotConfirmed;

  /// No description provided for @authErrorAlreadyRegistered.
  ///
  /// In es, this message translates to:
  /// **'Ya existe una cuenta con este email.'**
  String get authErrorAlreadyRegistered;

  /// No description provided for @authErrorWeakPassword.
  ///
  /// In es, this message translates to:
  /// **'La contraseña debe tener al menos 6 caracteres.'**
  String get authErrorWeakPassword;

  /// No description provided for @authErrorInvalidEmail.
  ///
  /// In es, this message translates to:
  /// **'Introduce un email válido.'**
  String get authErrorInvalidEmail;

  /// No description provided for @authErrorUnknown.
  ///
  /// In es, this message translates to:
  /// **'Ha ocurrido un error. Inténtalo de nuevo.'**
  String get authErrorUnknown;

  /// No description provided for @authErrorFillAllFields.
  ///
  /// In es, this message translates to:
  /// **'Rellena todos los campos.'**
  String get authErrorFillAllFields;

  /// No description provided for @authRegisterCheckEmail.
  ///
  /// In es, this message translates to:
  /// **'Cuenta creada — revisa tu email para confirmarla antes de iniciar sesión.'**
  String get authRegisterCheckEmail;

  /// No description provided for @authErrorRateLimited.
  ///
  /// In es, this message translates to:
  /// **'Demasiados intentos — espera unos minutos antes de volver a intentarlo.'**
  String get authErrorRateLimited;

  /// No description provided for @forgotPasswordTitle.
  ///
  /// In es, this message translates to:
  /// **'Recuperar contraseña'**
  String get forgotPasswordTitle;

  /// No description provided for @forgotPasswordSentBody.
  ///
  /// In es, this message translates to:
  /// **'Revisa tu correo, te hemos enviado un enlace para restablecer tu contraseña.'**
  String get forgotPasswordSentBody;

  /// No description provided for @forgotPasswordBody.
  ///
  /// In es, this message translates to:
  /// **'Introduce tu email y te enviaremos un enlace para restablecer tu contraseña'**
  String get forgotPasswordBody;

  /// No description provided for @forgotPasswordSendButton.
  ///
  /// In es, this message translates to:
  /// **'Enviar enlace'**
  String get forgotPasswordSendButton;

  /// No description provided for @forgotPasswordBackToLogin.
  ///
  /// In es, this message translates to:
  /// **'Volver al inicio de sesión'**
  String get forgotPasswordBackToLogin;

  /// No description provided for @rivalryTitle.
  ///
  /// In es, this message translates to:
  /// **'Rivalidad'**
  String get rivalryTitle;

  /// No description provided for @rivalryPointsThisMonth.
  ///
  /// In es, this message translates to:
  /// **'Puntos de este mes'**
  String get rivalryPointsThisMonth;

  /// No description provided for @rivalryFullRanking.
  ///
  /// In es, this message translates to:
  /// **'CLASIFICACIÓN COMPLETA'**
  String get rivalryFullRanking;

  /// No description provided for @rivalryEmpty.
  ///
  /// In es, this message translates to:
  /// **'Añade amigos para ver la clasificación'**
  String get rivalryEmpty;

  /// No description provided for @progressTitle.
  ///
  /// In es, this message translates to:
  /// **'Progreso'**
  String get progressTitle;

  /// No description provided for @habitsOtherHabits.
  ///
  /// In es, this message translates to:
  /// **'Otros hábitos'**
  String get habitsOtherHabits;

  /// No description provided for @habitsTimersTitle.
  ///
  /// In es, this message translates to:
  /// **'Cronómetros'**
  String get habitsTimersTitle;

  /// No description provided for @habitsTimersSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Pomodoro, entrenamiento y más'**
  String get habitsTimersSubtitle;

  /// No description provided for @habitsTechHealthSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Límites por app / grupo + avisos'**
  String get habitsTechHealthSubtitle;

  /// No description provided for @remindersTitle.
  ///
  /// In es, this message translates to:
  /// **'Recordatorios'**
  String get remindersTitle;

  /// No description provided for @habitsRemindersSubtitle.
  ///
  /// In es, this message translates to:
  /// **'Avisos, timers y rutinas'**
  String get habitsRemindersSubtitle;

  /// No description provided for @friendsRequestsTab.
  ///
  /// In es, this message translates to:
  /// **'Solicitudes'**
  String get friendsRequestsTab;

  /// No description provided for @friendsRequestsTabCount.
  ///
  /// In es, this message translates to:
  /// **'Solicitudes ({n})'**
  String friendsRequestsTabCount(int n);

  /// No description provided for @friendsNoRequests.
  ///
  /// In es, this message translates to:
  /// **'No tienes solicitudes pendientes'**
  String get friendsNoRequests;

  /// No description provided for @friendsNoFriends.
  ///
  /// In es, this message translates to:
  /// **'Aún no tienes amigos'**
  String get friendsNoFriends;

  /// No description provided for @friendsRemoveConfirmTitle.
  ///
  /// In es, this message translates to:
  /// **'¿Eliminar amigo?'**
  String get friendsRemoveConfirmTitle;

  /// No description provided for @friendsRemoveConfirmMessage.
  ///
  /// In es, this message translates to:
  /// **'¿Seguro que quieres eliminar a {name} de tus amigos?'**
  String friendsRemoveConfirmMessage(String name);

  /// No description provided for @remindersEmptyState.
  ///
  /// In es, this message translates to:
  /// **'No tienes recordatorios.\n¡Añade uno!'**
  String get remindersEmptyState;

  /// No description provided for @homeTasksTodayLabel.
  ///
  /// In es, this message translates to:
  /// **'{completed}/{total} tareas hoy'**
  String homeTasksTodayLabel(int completed, int total);

  /// No description provided for @homeTasksLabel.
  ///
  /// In es, this message translates to:
  /// **'{completed}/{total} tareas'**
  String homeTasksLabel(int completed, int total);

  /// No description provided for @searchFriendsTitle.
  ///
  /// In es, this message translates to:
  /// **'Buscar amigos'**
  String get searchFriendsTitle;

  /// No description provided for @searchFriendsHint.
  ///
  /// In es, this message translates to:
  /// **'Buscar por nombre o email'**
  String get searchFriendsHint;

  /// No description provided for @timerSessionOf.
  ///
  /// In es, this message translates to:
  /// **'Sesión {current} de {total}'**
  String timerSessionOf(int current, int total);

  /// No description provided for @timerPhaseMinutes.
  ///
  /// In es, this message translates to:
  /// **'{min} min {phase}'**
  String timerPhaseMinutes(int min, String phase);

  /// No description provided for @timerPointEarned.
  ///
  /// In es, this message translates to:
  /// **'Punto del día conseguido'**
  String get timerPointEarned;

  /// No description provided for @timerClosedAppWarning.
  ///
  /// In es, this message translates to:
  /// **'El temporizador se pausa si cierras o minimizas la app'**
  String get timerClosedAppWarning;

  /// No description provided for @homeStepsProgressLabel.
  ///
  /// In es, this message translates to:
  /// **'{percent}% pasos'**
  String homeStepsProgressLabel(int percent);

  /// No description provided for @homeTimerPending.
  ///
  /// In es, this message translates to:
  /// **'Cronómetro pendiente'**
  String get homeTimerPending;

  /// No description provided for @trayOpen.
  ///
  /// In es, this message translates to:
  /// **'Abrir'**
  String get trayOpen;

  /// No description provided for @trayExit.
  ///
  /// In es, this message translates to:
  /// **'Salir'**
  String get trayExit;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['de', 'en', 'es'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'de':
      return AppLocalizationsDe();
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
