# DayPilot — Migraciones Supabase

Estos ficheros SQL definen y alimentan la base de datos de DayPilot.

| Fichero | Contenido |
|---|---|
| `01_schema.sql` | Esquema completo: tablas, triggers, funciones, vistas, RLS. Autocontenido — se ejecuta sobre una base vacía |
| `02_drop_all.sql` | Elimina todo (útil para partir de cero en desarrollo) |
| `03_seed.sql` | Inserta usuarios y datos de prueba representativos |
| `04_show_everything.sql` | Consulta de solo lectura: una fila por usuario con absolutamente todos sus datos (perfil, tareas, hábitos, progreso, puntos, amigos, notificaciones…) anidados como JSON, para inspeccionar el estado real de una cuenta sin lanzar una consulta por tabla |

Para partir de cero: `01_schema.sql`, luego `02_drop_all.sql`/`03_seed.sql` si hace falta.

---

## Tablas

### `users`
Perfil público de cada usuario, vinculado a `auth.users` de Supabase Auth.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | UUID PK | Mismo UUID que `auth.users.id` |
| `email` | TEXT | Correo electrónico |
| `name` | TEXT | Nombre visible |
| `username` | TEXT UNIQUE | Nombre de usuario (sensible a mayúsculas) |
| `username_lower` | TEXT UNIQUE | Versión en minúsculas, para búsquedas case-insensitive |
| `photo_url` | TEXT | URL pública del avatar (bucket `avatars` de Supabase Storage) |
| `region` | TEXT | Región/comunidad autónoma del usuario |
| `level` | INTEGER | Nivel actual, calculado automáticamente por `fn_update_level` |
| `total_points_historical` | INTEGER | Puntos acumulados desde el inicio (nunca decrece) |
| `points_to_next_level` | INTEGER | Puntos históricos totales necesarios para alcanzar `level + 1`; recalculado por `fn_update_level` junto con `level` |
| `pending_steps_goal` | INTEGER | Nueva meta de pasos en cola, aún no aplicada. Mecanismo de sincronización entre dispositivos: al cambiar la meta desde un dispositivo se escribe aquí para que otros dispositivos de la misma cuenta la recojan |
| `pending_steps_goal_date` | DATE | Fecha en la que `pending_steps_goal` pasa a ser efectiva. `fn_apply_pending_steps_goals` limpia ambos campos cada noche una vez pasa esa fecha |
| `created_at` | TIMESTAMPTZ | Fecha de registro |

**Fórmula de nivel:** `N = floor((-1 + sqrt(1 + 4·(2 + pts/5))) / 2)`. El nivel 1 se alcanza con 0 pts, el 2 con 20 pts, el 3 con 50 pts y así sucesivamente (coste por nivel = 20 + 10·(nivel − 1)). `points_to_next_level(N) = 5·N·(N+3)` es la inversa: puntos históricos totales para alcanzar el nivel `N+1`.

`fn_update_level` solo se dispara en `UPDATE OF total_points_historical` — un `INSERT` en `users` (como el del seed) no lo recalcula, hay que fijar `points_to_next_level` a mano si no se quiere el valor por defecto (20, el del nivel 1).

La meta de pasos *activa* de un dispositivo no vive aquí — se hidrata desde la fila más reciente de `habits_daily.steps_goal` la primera vez que arranca la app, y a partir de ahí vive en local (`SharedPreferences`). `pending_steps_goal`/`_date` son solo el buzón de sincronización entre dispositivos.

---

### `user_streaks`
Racha de días consecutivos de actividad de cada usuario. Se actualiza automáticamente vía `fn_update_streak` cada vez que `fn_close_daily_progress` escribe en `user_daily_log`.

| Columna | Descripción |
|---|---|
| `user_id` | FK a `users`, UNIQUE (una fila por usuario) |
| `current_streak` | Días consecutivos activos hasta hoy |
| `longest_streak` | Máximo histórico de días consecutivos |
| `last_active_date` | Último día con actividad registrada |

---

### `tasks`
Tareas creadas por el usuario. Una tarea puede tener varias fechas asignadas (tabla `task_days`) — una tarea recurrente se representa con UNA fila en `tasks` y VARIAS en `task_days`.

| Columna | Descripción |
|---|---|
| `title` | Título de la tarea |
| `description` | Descripción opcional |
| `category` | Una de: `Trabajo`, `Estudio`, `Deporte`, `Salud`, `General`, `Hogar` (así es exactamente como la app serializa cada `TaskCategory`; `PERSONAL` y `OTHER` comparten `General`) |
| `difficulty` | `EASY` / `MEDIUM` / `HARD` |
| `estimated_minutes` | Duración estimada |
| `reminder_enabled` | Si tiene recordatorio activado |
| `is_recurring` | Si la tarea se repite. La expansión de fechas ocurre una vez, en el cliente, al crear la tarea (genera hasta 90 días de filas en `task_days` de golpe) — no hay ningún mecanismo que continúe generando fechas después de esa ventana |

El completado vive en `task_days`, no aquí — `tasks` no tiene ninguna columna de estado.

---

### `task_days`
Asocia una tarea con una fecha concreta del calendario. Aquí vive el estado real de completado por ocurrencia.

| Columna | Descripción |
|---|---|
| `date` | Fecha de esta ocurrencia |
| `is_completed` | Si esta ocurrencia concreta está marcada como hecha |
| `completed_at` | Cuándo se marcó |
| `is_earned` | Sticky: solo pasa de `false` a `true`, nunca al revés. Evita que desmarcar y volver a marcar la misma ocurrencia pague los puntos dos veces |

Las ocurrencias completadas con más de 20 días se eliminan por `fn_cleanup_completed_tasks` (job nocturno, no trigger), que además borra cualquier fila de `tasks` que se quede sin ninguna ocurrencia.

---

### `habits_daily`
Estado de los hábitos de un usuario para un día concreto. Una fila por (usuario, fecha).

| Columna | Descripción |
|---|---|
| `steps` | Pasos registrados ese día |
| `steps_goal` | Meta de pasos de ese día |
| `timer_point_earned` | Si ya se otorgó el punto de temporizador ese día. `completeTimerSession()` lo consulta antes de conceder puntos (gate server-side, para que no se pueda ganar dos veces desde dos dispositivos el mismo día) y lo marca `true` justo después de otorgarlos |

Cada INSERT/UPDATE en esta tabla dispara `fn_sync_habits_to_progress`, que actualiza los pasos en `daily_progress`.

---

### `daily_progress`
Progreso en vivo del día actual. Una fila por usuario (sin fecha en el índice: se recrea cada día). Se resetea a medianoche UTC por `fn_close_daily_progress`.

| Columnas de puntos | Fuente |
|---|---|
| `tasks_points` | Tareas completadas (20 pts/tarea) |
| `steps_points` | Hitos de pasos (10/20/30 pts al 50%/75%/100% de la meta) |
| `timer_points` | Sesiones de temporizador completadas (10 pts, una vez al día, ver `habits_daily.timer_point_earned`) |
| `tech_health_points` | Bonus diario por cumplir límites de apps |
| `wellness_points` | Categoría general de bienestar |
| `total_points` | Suma de todas las categorías anteriores |

---

### `tech_health_config`
Límite de uso diario configurado por el usuario para cada app del dispositivo. Una fila por (usuario, paquete Android).

| Columna | Descripción |
|---|---|
| `app_package` | Package name de Android (ej. `com.instagram.android`) |
| `app_name` | Nombre legible de la app |
| `limit_hours` | Límite en horas (decimal, ej. `1.5` = 90 min) |
| `is_active` | Si el límite está activo actualmente |
| `pending_active` | Anti-trampa: activar/desactivar el límite se difiere al día siguiente en vez de aplicarse al momento (si no, se podría desactivar justo antes de una violación para esquivar el bloqueo). `NULL` = sin cambio pendiente; si no, el valor que tomará `is_active` esa noche |
| `pending_limit_hours` | Mismo anti-trampa pero para subir el límite (bajarlo se aplica al momento) |
| `is_violated_today` | Si esta restricción concreta ya se ha superado hoy — determina si sigue siendo elegible para el bonus nocturno de +10. Se resetea cada noche |
| `pending_delete` | Soft delete: el usuario ha borrado la restricción desde la app, pero sigue contando como activa hasta que `fn_close_daily_progress` la elimina de verdad esa noche |

### `tech_health_group_config`
Igual que `tech_health_config` pero para un grupo con nombre que agrupa varias apps bajo un único límite combinado (ver `tech_health_group_apps` para la membresía). Mismas columnas y misma semántica anti-trampa que `tech_health_config`, sustituyendo `app_package`/`app_name` por `group_name`.

### `tech_health_group_apps`
Qué apps pertenecen a cada grupo. Sin `user_id` propio — la propiedad se comprueba vía el grupo padre en RLS. El servicio de accesibilidad suma el uso de todas las apps del grupo contra el límite del grupo; a efectos del bonus de "≥3 restricciones", un grupo cuenta como su número de apps miembro, no como 1.

---

### `user_daily_log`
Historial de días cerrados. Cuando `fn_close_daily_progress` se ejecuta, archiva aquí el `daily_progress` del día que termina. Máximo 30 filas por usuario (la más antigua se elimina automáticamente). Sobre esta tabla se calcula la gráfica de progreso de 30 días y las rachas.

---

### `user_weekly_summary`
Resumen semanal generado automáticamente cada lunes a las 00:05 UTC por `fn_generate_weekly_summary`. Agrega los 7 días cerrados de `user_daily_log`. Los amigos pueden ver el resumen de los demás y reaccionar con emojis.

**Una fila por usuario** (`UNIQUE(user_id)`), no una por semana: cada lunes se sobreescribe la fila existente (`ON CONFLICT (user_id) DO UPDATE`) en vez de insertar una nueva, así que solo existe el resumen de la última semana cerrada. Como las reacciones son sobre `weekly_summary_id`, sobreescribir la fila dejaría reacciones de la semana pasada colgando de los números de la semana nueva — `fn_generate_weekly_summary` las borra primero.

| Columna | Descripción |
|---|---|
| `week_start` | Lunes de la semana resumida |
| `total_steps` | Pasos totales de la semana |
| `total_tasks_completed` | Tareas completadas en la semana |
| `total_points` | Puntos ganados en la semana |
| `best_streak` | Racha máxima alcanzada durante la semana |

---

### `points_log`
Registro inmutable de cada evento que genera puntos. Cada INSERT dispara `fn_sync_points_to_progress`, que actualiza `daily_progress` y `users.total_points_historical`. Los registros con más de 30 días se eliminan automáticamente.

| Columna | Descripción |
|---|---|
| `source` | `STEPS` / `TASKS` / `WELLNESS` / `TIMER` / `TECH_HEALTH` |
| `points` | Puntos del evento. `CHECK (points > 0)` — este registro es solo de eventos que suman, nunca de descuentos |
| `day_key` | Fecha a la que se atribuyen los puntos |

---

### `friend_requests`
Solicitudes de amistad pendientes (de → a). Una vez aceptada, se elimina esta fila y se crea una en `friends`.

### `friends`
Amistades aceptadas. Relación bidireccional almacenada como una sola fila (requester → receiver). Las políticas RLS comprueban ambas columnas para leer los datos de un amigo.

### `reactions`
Reacciones emoji de un usuario al resumen semanal de un amigo. Una reacción por (remitente, destinatario, resumen). Tipos posibles: `fire`, `clap`, `strong`, `star`.

### `notifications`
Centro de notificaciones in-app. Cualquier usuario autenticado puede insertar (necesario para eventos cruzados como solicitudes de amistad); solo el destinatario puede leer, marcar como leída o eliminar sus propias notificaciones. Todo evento de la app pasa por esta tabla — no hay notificaciones que vivan solo en el dispositivo.

Tipos soportados: `FRIEND_REQUEST`, `FRIEND_ACCEPTED`, `REACTION`, `LEVEL_UP`, `STREAK_RISK`, `STEPS_GOAL`, `TASK_COMPLETED`, `TIMER_DONE`, `TASK_REMINDER`, `DAILY_SUMMARY`.

---

## Vistas

### `friends_ranking`
Ranking de puntos de los últimos 30 días para el usuario autenticado y sus amigos. Combina `user_daily_log` (días cerrados) con `daily_progress` (puntos de hoy aún sin cerrar), para que el ranking se actualice en tiempo real. Usa `security_invoker = true` para respetar las políticas RLS del usuario que consulta.

### `calendar_tasks`
Tareas con sus fechas de calendario (JOIN de `tasks` + `task_days`). Expone `occurrence_id` (`task_days.id`, identifica una fecha concreta — usar para completar/descompletar) y `task_id` (`tasks.id`, identifica la serie entera — usar para editar/borrar toda la tarea). `is_completed` e `is_earned` vienen de `task_days`, por ocurrencia.

---

## Realtime

Cuatro tablas están añadidas a la publicación `supabase_realtime` (Database → Replication), para que el cliente reciba cambios en vivo por WebSocket en vez de tener que hacer polling:

| Tabla | Quién la escucha | Para qué |
|---|---|---|
| `notifications` | `NotificationsNotifier` (Flutter) | Nuevas notificaciones (solicitud de amistad, amistad aceptada, subida de nivel...) aparecen al instante en la campana, sin refrescar |
| `daily_progress` | `ProgressNotifier` (Flutter) | Los puntos/progreso de hoy se actualizan al instante en cualquier dispositivo donde la sesión esté abierta, en vez de esperar al polling de refresco (que sigue existiendo como red de seguridad) |
| `tasks` | `TasksNotifier` (Flutter) | Cambios en la tarea en sí (título, categoría, dificultad...) hechos desde otro dispositivo se reflejan al instante |
| `task_days` | `TasksNotifier` (Flutter) | Completar/descompletar una ocurrencia, o crear/borrar fechas, se refleja al instante — `calendar_tasks` es una vista sobre `tasks`+`task_days`, así que ambas tablas base se escuchan por separado |

Ambas están filtradas por `user_id = auth.uid()` en el cliente, así que cada usuario solo recibe eventos de sus propias filas.

---

## Funciones y triggers

| Trigger | Sobre | Cuándo | Efecto |
|---|---|---|---|
| `trg_sync_habits_to_progress` | `habits_daily` | INSERT/UPDATE | Actualiza `steps` en `daily_progress` |
| `trg_task_completed_to_progress` | `task_days` | UPDATE | Incrementa `tasks_completed` en `daily_progress` al marcar completada una ocurrencia |
| `trg_sync_points_to_progress` | `points_log` | INSERT | Suma puntos por categoría en `daily_progress`; incrementa `total_points_historical` en `users` |
| `trg_update_level` | `users` | BEFORE UPDATE de `total_points_historical` | Recalcula y actualiza `level`/`points_to_next_level` |
| `trg_update_streak` | `user_daily_log` | INSERT | Incrementa o resetea la racha en `user_streaks` |
| `trg_limit_daily_log` | `user_daily_log` | INSERT | Elimina la fila más antigua si hay más de 30 por usuario |
| `trg_create_user_profile` | `auth.users` | INSERT | Crea la fila de `users` a partir de `raw_user_meta_data` (name/username/region) — funciona sin depender de que el cliente vuelva a iniciar sesión tras confirmar el email |
| `trg_seed_daily_progress` | `users` | INSERT | Crea la fila de `daily_progress` del nuevo usuario |
| `trg_cleanup_points_log` | `points_log` | INSERT | Elimina puntos con más de 30 días |

### Tareas programadas (pg_cron)

| Job | Schedule | Función |
|---|---|---|
| `close-daily-progress` | `0 0 * * *` (cada noche a medianoche UTC) | `fn_close_daily_progress()`: otorga el bonus de +10 TECH_HEALTH del día que cierra (apps y grupos combinados, ≥3 restricciones y ninguna violada), purga `tech_health_config`/`tech_health_group_config` marcados `pending_delete`, aplica cambios diferidos, resetea `is_violated_today`, archiva `daily_progress` en `user_daily_log` y resetea el progreso del día |
| `generate-weekly-summary` | `5 0 * * 1` (cada lunes a las 00:05 UTC) | `fn_generate_weekly_summary()`: agrega los últimos 7 días en `user_weekly_summary` (una fila por usuario, sobreescrita) |
| `cleanup-completed-tasks` | `15 0 * * *` (cada noche a las 00:15 UTC) | `fn_cleanup_completed_tasks()`: elimina ocurrencias completadas con más de 20 días y tareas sin ninguna ocurrencia |
| `apply-pending-steps-goals` | `2 0 * * *` (cada noche a las 00:02 UTC) | `fn_apply_pending_steps_goals()`: limpia `pending_steps_goal`/`pending_steps_goal_date` una vez pasa la fecha efectiva |

---

## Reseteo del esquema (`02_drop_all.sql`)

Deshace por completo lo creado por `01_schema.sql`, pensado para poder partir de cero rápidamente durante el desarrollo. El orden de operaciones es:

1. **Desprogramar los jobs de `pg_cron`** — envuelto en bloques `DO $$ ... EXCEPTION WHEN OTHERS THEN NULL; END $$` para que no falle si algún job todavía no existe (por ejemplo, la primera vez que se ejecuta sobre una base vacía).
2. **Desactivar triggers y FKs temporalmente** con `SET session_replication_role = replica`, lo que permite eliminar las tablas sin preocuparse por el orden de las claves foráneas.
3. **Eliminar las 16 tablas** con `DROP TABLE IF EXISTS ... CASCADE`.
4. **Eliminar las vistas** (`friends_ranking`, `calendar_tasks`) y las 11 funciones listadas explícitamente, con `CASCADE` para arrastrar también los triggers que dependen de cada una.
5. **Restaurar** `session_replication_role = DEFAULT` al final.

---

Todos los usuarios tienen contraseña `password123`.

| Usuario | Email | Región | Días de historial sembrados | Relaciones |
|---|---|---|---|---|
| **Ana García** (`anagarcia`) | ana.garcia@daypilot.test | Canarias | 14 | Amiga de Carlos, María y Javier. Pendiente de aceptar solicitud de Lucía |
| **Carlos Ruiz** (`carlosruiz`) | carlos.ruiz@daypilot.test | Madrid | 5 | Amigo de Ana |
| **María López** (`marialopez`) | maria.lopez@daypilot.test | Cataluña | 9 (dos tandas: 5 + 4) | Amiga de Ana |
| **Javier Moreno** (`javiermoreno`) | javier.moreno@daypilot.test | Andalucía | 3 | Amigo de Ana |
| **Lucía Fernández** (`luciafernandez`) | lucia.fernandez@daypilot.test | Galicia | 20 | Solicitud pendiente a Ana |

`total_points_historical` se calcula al final del seed como la suma de todo lo que se acaba de generar en `user_daily_log` para ese usuario (más lo que añaden los eventos de "hoy" vía `points_log`, en el caso de Ana) — no un número elegido a mano. `level`/`points_to_next_level` salen solos de ahí, porque esa `UPDATE` dispara `trg_update_level` igual que lo haría un evento real de la app.

Solo Ana tiene datos "ricos" — es la cuenta pensada para probar tareas, límites de Salud Tecnológica y notificaciones manualmente:
- 3 tareas pendientes + 2 completadas (con fechas backdatadas) + 1 completada hoy — todas con categorías del conjunto real que usa la app (`Hogar`, `Estudio`, `Salud`, `General`), no valores inventados
- 2 límites individuales (Instagram, TikTok) + 1 grupo "Redes sociales" (WhatsApp + X), 4 restricciones en total — suficiente para el bonus nocturno de Salud Tecnológica (mínimo 3)
- Puntos de hoy de las 4 fuentes (`STEPS`, `TASKS`, `TIMER`, `WELLNESS`)

El resto de usuarios solo tienen `user_daily_log` histórico y relaciones sociales, para poblar el ranking y las reacciones alrededor de Ana. Reacciones cruzadas: Ana → María (`fire`), Carlos → Ana (`clap`), Lucía → Ana (`star`).

Los `user_daily_log` generados con `random()` usan solo valores que la app realmente otorga: `tasks_points` es siempre `tasks_completed * 20`, `steps_points` sale de subconjuntos de `{0, 10, 30, 60}` (los hitos de pasos), y `wellness_points`/`timer_points`/`tech_health_points` son `0` o `10`.

---

## Inspección completa (`04_show_everything.sql`)

Consulta de solo lectura pensada para depurar sin tener que revisar tabla por tabla. Devuelve **una fila por usuario**, ordenada por `username`, con:

- Los datos de `users`, `user_streaks`, `daily_progress` (progreso de hoy) y `user_weekly_summary` como columnas normales, vía `LEFT JOIN` directo.
- Doce relaciones uno-a-muchos agregadas mediante `LEFT JOIN LATERAL` + `json_agg(jsonb_build_object(...))`, cada una devuelta como un array JSON en su propia columna: tareas (con sus ocurrencias anidadas un nivel más adentro), hábitos diarios, histórico de 30 días, límites de salud tecnológica individuales y por grupo, log de puntos, solicitudes de amistad enviadas y recibidas, amistades, reacciones enviadas y recibidas, y notificaciones.

El resultado es, por cada usuario, un único documento con absolutamente todo su estado — el equivalente a una consulta a las 16 tablas del esquema, pero en una sola llamada. Útil sobre todo para verificar manualmente que el seed se ha aplicado bien o que una sesión de pruebas ha dejado los datos en el estado esperado, sin tener que abrir el editor de tablas de Supabase tabla por tabla.
