# DayPilot — Test Supabase

Esta rama contiene el proyecto de prueba que valida la conexión entre Android y Supabase antes de integrarla en la app principal.

---

## Por qué existe esta rama

El objetivo del Incremento 2 de DayPilot es reemplazar los `FakeRepository` por implementaciones reales conectadas a Supabase. Antes de tocar la app principal, se creó este proyecto Android mínimo e independiente para:

- Confirmar que el cliente `supabase-kt` se conecta correctamente al proyecto de Supabase.
- Validar que el login, las consultas SELECT y los INSERT funcionan con el esquema real.
- Identificar y resolver los problemas de compatibilidad de versiones antes de que afecten al proyecto principal.
- Establecer exactamente qué versiones de Kotlin, supabase-kt y Ktor funcionan juntas.

> **Conclusión:** La conexión funciona. Los resultados de esta rama son la base directa del Incremento 2 en la app principal.

---

## Arquitectura adoptada

Se decidió **no construir una API intermedia** (como Flask o Express). Supabase ya expone una API REST auto-generada a partir del esquema SQL via PostgREST, y gestiona la autenticación con Supabase Auth. El cliente Android usa el SDK oficial `supabase-kt` para conectarse directamente:

```
App Android (supabase-kt)
        ↓  HTTPS
   Supabase Cloud
  ┌──────────────┐
  │  PostgREST   │  ← SELECT, INSERT, UPDATE, DELETE sobre las tablas
  │  Auth        │  ← Login, sesión, JWT
  │  PostgreSQL  │  ← Base de datos con triggers, vistas y RLS
  └──────────────┘
```

La seguridad de los datos no recae en una capa de API propia sino en las **políticas RLS** (Row Level Security) de PostgreSQL, que ya están definidas en el esquema y se aplican automáticamente para cada usuario autenticado.

---

## Versiones validadas

Esta combinación fue la única que compiló correctamente. Cambiar cualquiera de estas versiones puede romper el proyecto.

| Componente | Versión | Motivo |
|---|---|---|
| Kotlin | **2.2.21** | supabase-kt 3.3.0 usa `kotlinx-serialization 1.9.0` compilado con Kotlin 2.2. Las versiones 2.0.x y 2.1.x no pueden leer esos binarios y el compilador K2 falla con un crash interno. |
| supabase-kt | **3.3.0** | Versión estable con Auth y PostgREST. |
| ktor-client-android | **3.3.0** | Debe coincidir con la versión de supabase-kt para evitar incompatibilidades binarias. |
| compileSdk / targetSdk | **36** | supabase-kt arrastra dependencias de AndroidX que lo requieren. |
| Java target | **17** | Necesario con Kotlin 2.2.x y AndroidX moderno. |
| minSdk | **26** | Requisito mínimo de supabase-kt. |

---

## Qué se probó y funcionó

Con los datos de la seed aplicada al proyecto de Supabase:

- ✅ **Login** con email/contraseña de un usuario de prueba (`ana.garcia@daypilot.test`).
- ✅ **SELECT** de tareas del usuario autenticado (respeta RLS, solo devuelve las propias).
- ✅ **INSERT** de una tarea nueva asociada al `user_id` del usuario logueado.

---

## Base de datos

### Esquema

El esquema SQL completo está en la carpeta `migrations/` de la rama principal (`Incremento-Android`). Se compone de tres archivos aplicados en orden en el SQL Editor de Supabase:

| Archivo | Propósito |
|---|---|
| `01_schema.sql` | Crea todas las tablas, índices, triggers, vistas y políticas RLS. |
| `02_drop_all.sql` | Elimina todo de forma segura (útil para resetear el entorno de desarrollo). |
| `03_seed.sql` | Inserta 5 usuarios de prueba con historial, amistades y actividad del día. |

### Tablas principales

| Tabla | Descripción |
|---|---|
| `users` | Perfil del usuario (nivel, puntos históricos, tema). |
| `user_streaks` | Racha diaria, calculada automáticamente por trigger. |
| `tasks` | Tareas con soporte de recurrencia y dificultad. |
| `task_days` | Fechas asignadas a cada tarea (permite recurrencia). |
| `habits_daily` | Registro diario de pasos, cronómetro y salud tecnológica. |
| `daily_progress` | Progreso del día en curso — **una fila por usuario**, se reinicia cada noche. |
| `user_daily_log` | Historial permanente de hasta 30 días cerrados. |
| `user_weekly_summary` | Resumen semanal inmutable, generado cada lunes. |
| `points_log` | Log de cada evento de puntos (STEPS, TASKS, WELLNESS, TIMER, TECH_HEALTH). |
| `friend_requests` | Solicitudes de amistad pendientes. |
| `friends` | Relaciones de amistad aceptadas. |
| `reactions` | Reacciones (fire, clap, strong, star) al resumen semanal de un amigo. |

### Automatismos clave

**Triggers (se ejecutan en cada operación):**
- `habits_daily` → sincroniza pasos en `daily_progress`.
- Tarea completada → incrementa `tasks_completed` en `daily_progress`.
- `points_log` INSERT → propaga puntos por categoría a `daily_progress` y suma a `users.total_points_historical`.
- `users.total_points_historical` UPDATE → recalcula nivel: `FLOOR(puntos / 50) + 1`.
- `user_daily_log` INSERT → actualiza `user_streaks` (días consecutivos).
- `user_daily_log` INSERT → borra la fila más antigua si hay más de 30.

**Cron jobs via `pg_cron` (se ejecutan por tiempo):**
- Cada noche a las 00:00 UTC: `fn_close_daily_progress()` — vuelca `daily_progress` en `user_daily_log` y lo reinicia a cero.
- Cada lunes a las 00:05 UTC: `fn_generate_weekly_summary()` — agrega los últimos 7 días cerrados en `user_weekly_summary`.

**Vistas (`security_invoker = true`, respetan RLS):**
- `friends_ranking` — puntos de los últimos 30 días (cerrados + hoy en vivo).
- `daily_summary` — resumen del día para la card de inicio.
- `calendar_tasks` — tareas con sus fechas para el calendario.

### Usuarios de prueba (seed)

| Email | Contraseña | Notas |
|---|---|---|
| ana.garcia@daypilot.test | password123 | Usuario principal. Racha: 14 días. Amiga de Carlos, María y Javier. |
| carlos.ruiz@daypilot.test | password123 | Racha: 5 días. |
| maria.lopez@daypilot.test | password123 | Racha: 4 días (racha rota una vez, longest: 5). |
| javier.moreno@daypilot.test | password123 | Racha: 3 días. |
| lucia.fernandez@daypilot.test | password123 | Racha: 20 días. Ha enviado solicitud de amistad a Ana (pendiente). |

---

## Siguiente paso

Los resultados de esta rama alimentan directamente el **Incremento 2** de la app principal (`Incremento-Android`), que consistirá en:

1. Añadir el cliente Supabase y las dependencias correctas a la app principal.
2. Implementar `SupabaseXRepository` para cada repositorio existente (uno por uno, empezando por `TaskRepository`).
3. Sustituir en `DayPilotNavGraph.kt` la instanciación de `FakeXRepository` por `SupabaseXRepository` — los ViewModels y las Screens no requieren ningún cambio gracias al patrón Repository.
