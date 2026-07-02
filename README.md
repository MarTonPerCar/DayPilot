# DayPilot — Rama `Incremento-Android-TestFinal`

> **Estado:** Incremento completado  
> **Base:** `Incremento-Android` (arquitectura MVVM + Supabase integrado)  
> **Propósito:** Testing intensivo del primer incremento funcional completo

---

## ¿Qué es esta rama?

Esta es la rama de trabajo del primer incremento funcional de DayPilot. Parte de la arquitectura MVVM con repositorios conectados a Supabase y añade las funcionalidades que faltaban para que la aplicación sea completa: sistema de puntos real, salud tecnológica con bloqueo de apps, social con reacciones, notificaciones centralizadas y cronómetros. Es también donde se realizó el testing exhaustivo, la revisión de código y la corrección de errores antes de consolidar en `Incremento-Android`.

---

## Punto de partida

La rama base entregó:

- Arquitectura MVVM completa con ViewModels y StateFlow
- Repositorios Supabase para tareas, usuarios, progreso y ranking
- 19 pantallas funcionales con navegación real (no maqueta)
- Sistema de pasos con hitos y sincronización con Supabase
- Sistema de recordatorios con AlarmManager
- Autenticación con Supabase Auth
- CI/CD con GitHub Actions

---

## Funcionalidades implementadas en este incremento

### Salud tecnológica (TechHealth)

Sistema de restricciones de uso de apps basado en dos permisos del sistema: estadísticas de uso y accesibilidad. El usuario puede crear restricciones individuales o por grupo, cada una con un límite diario en minutos. Cuando el usuario supera el límite, `DayPilotAccessibilityService` detecta la app en primer plano y lanza `TechHealthBlockActivity`, que ocupa toda la pantalla y obliga al usuario a volver al inicio. Las restricciones eliminadas o desactivadas se aplican de forma diferida al día siguiente para no interrumpir el uso en curso.

La pantalla de configuración muestra una pasarela de permisos a pantalla completa si alguno no está concedido, con instrucciones paso a paso para cada permiso, indicadores de estado y botones directos a los ajustes del sistema.

Los datos de uso se actualizan mediante un `PeriodicWorkRequest` de WorkManager cada 15 minutos y también en tiempo real cada 60 segundos mientras la app está en primer plano. Si el usuario completa el día sin violar ningún límite (con al menos 3 restricciones activas), gana un punto de bonificación diario.

### Notificaciones

Sistema centralizado de notificaciones almacenadas en Supabase. Cada evento relevante (solicitud de amistad, reacción recibida, alerta de nivel, etc.) genera una fila en la tabla `notifications`. La pantalla de notificaciones las carga, agrupa por tipo y permite marcarlas como leídas. Se utiliza un `Hub` local para evitar duplicados y reducir llamadas a la red en cada apertura.

### Social — Amigos y reacciones

El usuario puede buscar otros usuarios por nombre de usuario, enviar solicitudes de amistad, aceptarlas o rechazarlas y ver el perfil individual de cada amigo. Las reacciones permiten valorar el resumen semanal de un amigo con un emoji; cada usuario solo puede reaccionar una vez por resumen y la reacción llega al destinatario como notificación.

### Rivalidad y ranking

Tabla de clasificación de los últimos 30 días entre el usuario y sus amigos, con pódium para los tres primeros puestos. El ranking se calcula agregando los puntos del log de puntos y se refresca con caché para evitar llamadas repetidas a la red.

### Perfil

Pantalla de perfil con desglose de puntos del día actual por fuente (tareas, pasos, hábitos, cronómetros), posición en el ranking y resumen semanal generado automáticamente por Supabase cada lunes. Incluye subida de foto de perfil al bucket de Supabase Storage con vista previa inmediata.

### Cronómetros y temporizadores

Módulo de temporizadores con tres modalidades: Pomodoro (25 min trabajo / 5 min descanso, número de sesiones configurable), modos predefinidos (entrenamiento, meditación, cocina) y modo libre con duración entre 5 y 180 minutos. La finalización de una sesión otorga puntos según la duración.

---

## Retos técnicos y decisiones relevantes

### AccessibilityService y bloqueo de apps

El bloqueo de apps en Android moderno requiere un `AccessibilityService` que escuche `TYPE_WINDOW_STATE_CHANGED`. La actividad de bloqueo (`TechHealthBlockActivity`) necesita los flags `FLAG_SHOW_WHEN_LOCKED` y `FLAG_TURN_SCREEN_ON` para aparecer encima del lockscreen en algunos fabricantes. El botón de atrás está completamente bloqueado (`onBackPressed` vacío); la única salida es el botón de "Ir al inicio". Para evitar relanzar la pantalla en bucle al pulsar Home o Recientes, se usa `onUserLeaveHint` con un flag `intentionalExit`.

El debounce en el servicio (2.500 ms) evita que la pantalla de bloqueo se remuestre mil veces para la misma app durante el tiempo que tarda en procesarse el intent.

### Permisos con pasarela a pantalla completa

El patrón `if (!permiso) { MostrarGate(); return }` al inicio del composable funciona correctamente en Compose y evita mostrar contenido de la pantalla principal antes de que los permisos estén concedidos. El `return` en un composable finaliza la composición de la función sin afectar al árbol de composición superior.

### Caché de sesión y consistencia

`SessionCache` usa `MutableStateFlow<T?>` donde `null` significa "no cargado" y cualquier valor no nulo es la versión cacheada. El patrón estándar es: si hay valor cacheado, devolverlo; si no, cargar desde Supabase y guardar. Tras cualquier escritura (añadir, editar, borrar, togglear tarea) se invalida el caché poniendo el valor a `null`, garantizando que la siguiente lectura vaya a la base de datos. Este patrón se aplica de forma consistente en `SupabaseTaskRepository`.

### WorkManager y límites del sistema

WorkManager impone un intervalo mínimo de 15 minutos para trabajo periódico en Android. Para el polling más frecuente (actualización de uso de apps mientras la app está en primer plano) se usa un bucle de corrutinas en el ViewModel con `delay(60_000L)`, que el sistema puede cancelar libremente cuando la app pasa a segundo plano.

### Resumen semanal

La tabla `user_weekly_summary` se puebla automáticamente mediante un job de `pg_cron` en Supabase que se ejecuta cada lunes a las 00:05 UTC, justo después de que el job diario cierre el día anterior. El lado Android solo lee de esta tabla; nunca escribe en ella.

### Tema y modo oscuro en actividades independientes

`TechHealthBlockActivity` es una `Activity` independiente (fuera del NavGraph), por lo que no hereda el tema del árbol de Compose principal. Hay que leer `AppPreferences` manualmente, resolver el `DayPilotTheme` enum por nombre y pasar tanto el tema como el `darkMode` al `setContent`. Sin esto, la pantalla de bloqueo siempre renderizaría el tema por defecto (SAGE_GREEN) independientemente de lo que el usuario haya configurado.

---

## Errores encontrados y corregidos

Durante la revisión de código se identificaron y corrigieron los siguientes problemas:

| Severidad | Archivo | Descripción |
|---|---|---|
| Alta | `SupabaseTaskRepository` | `updateTask`, `toggleTask` y `deleteTask` no invalidaban `SessionCache.tasks` tras escribir en BD, devolviendo datos obsoletos en la siguiente lectura |
| Alta | `ProfileViewModel` | `isUploadingAvatar` quedaba `true` permanentemente si `load()` fallaba silenciosamente tras una subida exitosa de foto |
| Media | `AppLimitFormCard` | `name.first()` lanzaba `NoSuchElementException` con nombres de app vacíos |
| Media | `DayPilotAccessibilityService` | `SharedPrefsTechHealthRepository` se instanciaba en cada evento de accesibilidad (hilo principal), potencial ANR con cambio de apps rápido |
| Media | `TechHealthBlockActivity` | La pantalla de bloqueo siempre renderizaba el tema SAGE_GREEN ignorando la preferencia del usuario |

---

## Estructura de ramas relacionadas

| Rama | Propósito |
|---|---|
| `Incremento-Android` | Versión de producción del incremento, base para el siguiente |
| `Incremento-Android-TestFinal` | Esta rama — trabajo y testing |
| `Intensive-Android-Testing` | Misma app con timings acelerados para pruebas exhaustivas |

---

## Convenciones de esta rama

- Commits por bloque funcional, no por archivo
- Las pantallas no conocen los repositorios — solo hablan con el ViewModel
- `SessionCache` es la única fuente de verdad en memoria; la BD es la fuente de verdad persistente
