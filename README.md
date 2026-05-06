# DayPilot — Rama de Diseño Android

> Rama de prototipado y validación visual de la interfaz de DayPilot para Android.

Esta rama (`Test-Diseno-Android`) es el espacio de trabajo dedicado al diseño y pruebas de la interfaz de usuario de DayPilot. Aquí se desarrollan, validan y refinan todos los componentes de UI con Jetpack Compose antes de integrarlos en la rama principal del proyecto. No depende de ningún backend ni de datos reales — toda la información es de prueba.

---

## Índice

- [Tecnología](#tecnología)
- [Sistema de diseño](#sistema-de-diseño)
- [Arquitectura de la UI](#arquitectura-de-la-ui)
- [Componentes implementados](#componentes-implementados)
- [Pantallas implementadas](#pantallas-implementadas)
- [Internacionalización](#internacionalización)
- [ComponentCatalog](#componentcatalog)

---

## Tecnología

| Elemento | Detalle |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Gestión de estado | `remember` / `mutableStateOf` (NavGraph) |
| Temas | `StateFlow` reactivo, sin reinicio |
| Min SDK | 26 |
| Target SDK | 35 |

---

## Sistema de diseño

### Temas de color

La app cuenta con **5 temas predefinidos**, cada uno con versión clara y oscura, aplicables en caliente sin reiniciar la aplicación:

| ID | Nombre |
|---|---|
| `sage_green` | Verde Salvia *(default)* |
| `ocean_blue` | Azul Océano |
| `sunset_orange` | Naranja Atardecer |
| `lavender_purple` | Lavanda |
| `rose_gold` | Oro Rosa |

El tema se gestiona mediante un `StateFlow` a nivel de aplicación y se aplica de forma reactiva a través de `DayPilotTheme`, que acepta los parámetros `theme: DayPilotTheme` y `darkMode: Boolean`.

### Tipografía

Escala tipográfica completa definida en Material 3, cubriendo todos los niveles desde `displayLarge` hasta `labelSmall`, con fuentes y pesos personalizados coherentes con la identidad visual de DayPilot.

---

## Arquitectura de la UI

```
app/
├── ui/
│   ├── theme/
│   │   ├── Color.kt          ← paletas de los 5 temas
│   │   ├── Typography.kt     ← escala tipográfica completa
│   │   └── Theme.kt          ← aplicación reactiva del tema
│   ├── model/                ← data classes y enums de UI
│   │   ├── TaskModels.kt
│   │   ├── ProgressModels.kt
│   │   ├── TechHealthModels.kt
│   │   ├── ReminderModels.kt
│   │   ├── ReminderFormData.kt
│   │   ├── RankingModels.kt
│   │   ├── TimerModels.kt
│   │   ├── FriendModels.kt
│   │   ├── NotificationModels.kt
│   │   ├── SearchModels.kt
│   │   ├── ReactionType.kt
│   │   └── UIModels.kt
│   ├── components/
│   │   ├── basic/            ← componentes atómicos reutilizables
│   │   ├── cards/            ← tarjetas de UI
│   │   ├── forms/            ← formularios y componentes de entrada
│   │   └── DayPilotCalendar.kt
│   ├── screens/              ← pantallas completas
│   └── navigation/
│       ├── DayPilotNavGraph.kt
│       └── DayPilotDestinations.kt
├── ComponentCatalog.kt       ← catálogo visual de todos los componentes
└── MainActivity.kt
```

### Gestión de estado

El estado mutable compartido (listas de tareas, recordatorios, restricciones) vive en el `NavGraph` mediante `var x by remember { mutableStateOf(...) }` con actualizaciones basadas en `.copy()`. Las pantallas individuales reciben el estado como parámetros y emiten eventos hacia arriba — patrón preparado para la migración a MVVM + ViewModels en el Incremento 1.

---

## Componentes implementados

### Basic (atómicos)

| Componente | Descripción |
|---|---|
| `DayPilotButtonPrimary/Outlined/Error/Text` | Variantes de botón |
| `DayPilotFAB` / `DayPilotIconButton` | Botón flotante e icono |
| `DayPilotAvatar` | Avatar con iniciales o imagen |
| `DifficultyChip` / `CategoryChip` / `DurationChip` | Chips de tarea |
| `DayPilotTextField` / `DayPilotPasswordField` | Campos de texto |
| `DayPilotDropdownField` | Selector desplegable |
| `DayPilotTopBar` / `DayPilotTopBarWithAction` | Barras superiores |
| `DayPilotBottomBar` | Barra de navegación inferior |
| `DayPilotSectionHeader` | Cabecera de sección con acción opcional |
| `DayPilotDivider` | Separador |
| `DayPilotEmptyState` | Estado vacío con icono y mensaje |
| `DayPilotSwitchRow` | Fila con switch |
| `DayPilotStatItem` / `DayPilotStatsRow` | Estadísticas en línea |
| `DayPilotWeeklyStat` / `DayPilotStatVerticalDivider` | Stats semanales |
| `DayPilotReactionBar` / `DayPilotReactionSummary` / `DayPilotReactionBadgeRow` | Sistema de reacciones |
| `DayPilotFilterSelector` | Selector de filtros horizontal |
| `DayPilotThemeSelector` / `DayPilotDarkModeSelector` / `DayPilotOptionSelector` | Selectores de configuración |
| `AppPickerSheet` / `AppMultiPickerSheet` | Selectores de apps del sistema |
| `DayPilotPhotoPickerDialog` | Diálogo de selección de foto |
| `MilestoneChip` / `StepStatRow` / `StepsComponents` | Componentes de pasos |
| `StatsTopBlock` / `StatsBreakdownRow` / `StatsComponents` | Desglose de estadísticas |
| `SummaryStatCard` / `ChartSummaryItem` / `DailySummaryStat` | Resúmenes de datos |
| `ProfileStatBlock` / `HomeSectionIndicator` | Bloques de perfil y home |
| `TaskDot` | Punto de tarea para calendario |

### Cards

| Componente | Descripción |
|---|---|
| `TaskCard` / `TaskMiniCard` / `TaskDayCard` | Variantes de tarjeta de tarea |
| `CalendarDayCard` | Celda de día en el calendario |
| `DailySummaryCard` | Resumen diario en home |
| `StatsCard` | Desglose de puntos del día |
| `ProfileStatsCard` / `ProfileInfoRow` | Tarjetas de perfil |
| `WeeklyReactionCard` | Resumen semanal con reacciones |
| `FriendCard` | Tarjeta de amigo con resumen desplegable |
| `UserSearchCard` / `FriendRequestCard` | Búsqueda y solicitudes |
| `RankingCard` / `CurrentUserRankingCard` | Posiciones del ranking |
| `PodiumCard` | Podio de los 3 primeros |
| `HabitCard` | Acceso rápido a hábito |
| `StepsCard` | Progreso de pasos con gráfico circular |
| `StepsSummaryCard` | Resumen semanal de pasos |
| `ReminderCard` | Recordatorio con toggle y borrado |
| `AppLimitCard` / `GroupLimitCard` | Restricciones de apps |
| `TimerCard` / `TimerHubCard` | Tarjetas de cronómetro |
| `ProgressChartCard` | Gráfico de progreso con curva Bezier |
| `HomeMenuCard` / `HomeMenuCardInline` | Menú de inicio |
| `NotificationCard` | Notificación con tipo y estado |

### Forms

| Componente | Descripción |
|---|---|
| `TaskFormCard` | Formulario de creación/edición de tarea con secciones colapsables |
| `ReminderFormCard` | Formulario de recordatorio con accesos rápidos y selector de fecha |
| `AppLimitFormCard` | Formulario de restricción de app o grupo |
| `AuthComponents` | Login, registro, toggle de autenticación |
| `DayPilotCalendar` | Calendario mensual con puntos de tarea |

---

## Pantallas implementadas

| Pantalla | Ruta | Descripción |
|---|---|---|
| `AuthScreen` | `auth` | Login y registro con animación de flip |
| `ResetPasswordScreen` | `reset_password` | Recuperación de contraseña |
| `HomeScreen` | `home` | Resumen diario + menú 2×2 |
| `CalendarScreen` | `calendar` | Calendario + tareas del día + filtros |
| `HabitsScreen` | `habits` | Hub de hábitos |
| `StepsScreen` | `steps` | Progreso de pasos + resumen semanal |
| `TimerHubScreen` | `timer_hub` | Selección de tipo de cronómetro |
| `TimerScreen` | `timer/{mode}/{minutes}` | Cronómetro con círculo de progreso |
| `PomodoroScreen` | `pomodoro/{sessions}` | Pomodoro con fases trabajo/descanso |
| `RemindersScreen` | `reminders` | Lista de recordatorios + formulario |
| `TechHealthScreen` | `tech_health` | Restricciones de apps y grupos |
| `ProgressScreen` | `progress` | Gráficos de progreso de 30 días |
| `RivalryScreen` | `rivalry` | Podio + ranking completo de amigos |
| `FriendsScreen` | `friends` | Lista de amigos + solicitudes |
| `SearchFriendsScreen` | `search_friends` | Búsqueda de usuarios |
| `NotificationsScreen` | `notifications` | Historial de notificaciones |
| `ProfileScreen` | `profile` | Perfil completo con stats y resumen |
| `EditProfileScreen` | `edit_profile` | Edición de nombre, username y foto |
| `SettingsScreen` | `settings` | Tema, modo oscuro, idioma, notificaciones |

---

## Internacionalización

Todos los strings de la interfaz están externalizados en `strings.xml` con soporte para **3 idiomas**:

| Carpeta | Idioma |
|---|---|
| `values/` | Español *(default)* |
| `values-en/` | Inglés |
| `values-de/` | Alemán |

El cambio de idioma sigue el sistema del dispositivo Android. Los strings con parámetros usan el formato estándar de Android (`%1$s`, `%1$d`). No hay ningún string hardcodeado en el código fuente de la UI.

---

## ComponentCatalog

El archivo `ComponentCatalog.kt` contiene **8 previews independientes** en Android Studio que muestran todos los componentes organizados visualmente sin necesidad de ejecutar la aplicación:

| Preview | Contenido |
|---|---|
| `CatalogBasicInputs` | Botones, Avatar, Chips, TextFields |
| `CatalogBasicLayout` | TopBars, Headers, Divider, EmptyState, Switch, Stats |
| `CatalogBasicMisc` | Reactions, Selectors, TaskDot, Steps, Profile blocks |
| `CatalogCardsPeople` | Task, Notification, Habit, Friend, User cards |
| `CatalogCardsRankingTimer` | Ranking, Podium, Reminder, Timer cards |
| `CatalogCardsStats` | Stats, Profile, Weekly, Daily, Steps, AppLimit cards |
| `CatalogCardsCalendar` | Progress Chart, Home Menu, Calendar |
| `CatalogForms` | Task Form, Reminder, AppLimit, Auth |
