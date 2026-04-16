# DayPilot — Endpoints & Base de datos

Esta rama contiene el desarrollo del backend de DayPilot: la API intermedia, los endpoints funcionales y toda la infraestructura de base de datos que sostiene la aplicación.

---

## API intermedia (Python + Flask)

Se desarrolla una capa de API entre la app móvil y Supabase siguiendo el patrón estándar cliente-servidor:
Las claves de Supabase nunca se exponen en el cliente móvil. Toda la lógica de negocio reside en el backend, y la misma API sirve tanto para Android como para iOS sin duplicar código.

La app utiliza una `BASE_URL` configurable:
- **Desarrollo:** servidor local
- **Producción:** Railway o Render

---

## Endpoints funcionales

### Autenticación
- `POST /auth/register` — Registro de usuario
- `POST /auth/login` — Inicio de sesión
- `POST /auth/logout` — Cierre de sesión
- `POST /auth/reset-password` — Recuperación de contraseña

### Usuario
- `GET /user/:id` — Obtener perfil
- `PUT /user/:id` — Actualizar perfil (nombre, username, foto, región, tema)

### Tareas
- `GET /tasks/:user_id` — Listar tareas del usuario
- `POST /tasks` — Crear tarea (con soporte de recurrencia)
- `PUT /tasks/:id` — Editar tarea
- `DELETE /tasks/:id` — Eliminar tarea
- `POST /tasks/:id/complete` — Marcar tarea como completada

### Hábitos
- `GET /habits/daily/:user_id` — Obtener registro diario
- `PUT /habits/steps/:user_id` — Actualizar pasos del día
- `POST /habits/timer/:user_id` — Registrar cronómetro completado
- `POST /habits/tech-health/:user_id` — Registrar cumplimiento de salud tecnológica

### Progreso y puntos
- `GET /progress/daily/:user_id` — Resumen del día en curso
- `GET /progress/history/:user_id` — Historial de los últimos 30 días
- `GET /progress/weekly/:user_id` — Resumen semanal

### Amigos y social
- `GET /friends/:user_id` — Lista de amigos con racha y resumen
- `POST /friends/request` — Enviar solicitud de amistad
- `POST /friends/accept` — Aceptar solicitud
- `DELETE /friends/:id` — Eliminar amistad
- `GET /friends/ranking/:user_id` — Ranking de amigos (últimos 30 días)
- `POST /reactions` — Añadir reacción al resumen semanal de un amigo

### Notificaciones
- `GET /notifications/:user_id` — Historial de notificaciones (máx. 30)

---

## Base de datos (Supabase / PostgreSQL)

### Tablas principales

| Tabla | Descripción |
|---|---|
| `users` | Perfil del usuario |
| `user_streaks` | Racha diaria separada del perfil |
| `tasks` | Tareas con soporte de recurrencia |
| `task_days` | Fechas asignadas a cada tarea |
| `habits_daily` | Registro diario de pasos, cronómetro y salud tecnológica |
| `daily_progress` | Progreso del día en curso (se reinicia cada noche) |
| `user_daily_log` | Historial permanente de hasta 30 días |
| `user_weekly_summary` | Resumen semanal inmutable generado cada domingo |
| `points_log` | Log de cada evento de puntos |
| `friend_requests` | Solicitudes de amistad pendientes |
| `friends` | Relaciones de amistad aceptadas |
| `reactions` | Reacciones a resúmenes semanales |

### Triggers automáticos
- Actualización de `habits_daily` → sincroniza pasos en `daily_progress`
- Tarea completada → incrementa `tasks_completed` en `daily_progress`
- Actualización de `total_points_historical` → recalcula nivel (`FLOOR(puntos / 50) + 1`)
- Insert en `user_daily_log` → elimina la fila más antigua si hay más de 30
- Insert en `points_log` → elimina entradas con más de 30 días del mismo usuario
- Tareas con `completed_at` > 20 días → eliminación automática

### Cron jobs (medianoche via Supabase Edge Functions)
- Vuelca `daily_progress` en `user_daily_log`
- Reinicia `daily_progress`
- Actualiza `user_streaks`
- Elimina tareas completadas con más de 20 días
- Los domingos genera `user_weekly_summary`

### Vistas
- `friends_ranking` — Ranking de amigos con puntos 30 días, nivel y racha
- `daily_summary` — Resumen del día para la card de inicio
- `calendar_tasks` — Tareas con fechas para el calendario

---

## Migraciones

Las migraciones se gestionan como archivos SQL numerados y versionados:
migrations/
└── 001_initial_schema.sql   ← esquema completo inicial
Cada migración es incremental, reversible donde sea posible, y se aplica en orden estricto sobre la instancia de Supabase.

---

## Estado

- [ ] Esquema inicial verificado en Supabase
- [ ] API Flask — estructura base
- [ ] Endpoints de autenticación
- [ ] Endpoints de tareas
- [ ] Endpoints de hábitos y progreso
- [ ] Endpoints sociales
- [ ] Despliegue en Railway/Render
