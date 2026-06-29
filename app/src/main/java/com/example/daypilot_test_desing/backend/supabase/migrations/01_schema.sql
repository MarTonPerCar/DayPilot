-- =============================================
-- DAYPILOT — ESQUEMA COMPLETO
-- =============================================


-- =============================================
-- EXTENSIONES
-- =============================================

-- Necesaria para crypt() / gen_salt() al sembrar usuarios de prueba en 03_seed.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Necesaria para programar fn_close_daily_progress() y fn_generate_weekly_summary()
-- Si tu proyecto no la permite por SQL, actívala desde
-- Database → Extensions → pg_cron en el dashboard de Supabase y vuelve a ejecutar
-- solo la sección "TAREAS PROGRAMADAS" más abajo.
CREATE EXTENSION IF NOT EXISTS pg_cron;


-- =============================================
-- USUARIOS
-- =============================================

CREATE TABLE users (
    id                      UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email                   TEXT NOT NULL,
    name                    TEXT NOT NULL,
    username                TEXT NOT NULL UNIQUE,
    username_lower          TEXT NOT NULL UNIQUE,
    photo_url               TEXT,
    region                  TEXT,
    zone_id                 TEXT,
    level                   INTEGER NOT NULL DEFAULT 1,
    total_points_historical INTEGER NOT NULL DEFAULT 0,
    theme_color             TEXT NOT NULL DEFAULT 'sage_green',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username_lower ON users(username_lower);
CREATE INDEX idx_users_email ON users(email);


-- =============================================
-- RACHAS
-- =============================================

CREATE TABLE user_streaks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_streak   INTEGER NOT NULL DEFAULT 0,
    longest_streak   INTEGER NOT NULL DEFAULT 0,
    last_active_date DATE,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);


-- =============================================
-- TAREAS
-- =============================================

CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               TEXT NOT NULL,
    description         TEXT,
    category            TEXT NOT NULL DEFAULT 'General',
    difficulty          TEXT NOT NULL DEFAULT 'MEDIUM'
                            CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    estimated_minutes   INTEGER NOT NULL DEFAULT 30,
    is_completed        BOOLEAN NOT NULL DEFAULT false,
    completed_at        TIMESTAMPTZ,
    reminder_enabled    BOOLEAN NOT NULL DEFAULT false,
    is_recurring        BOOLEAN NOT NULL DEFAULT false,
    recurrence_days     INTEGER,
    recurrence_end_date DATE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_user_completed ON tasks(user_id, is_completed);
CREATE INDEX idx_tasks_completed_at ON tasks(completed_at)
    WHERE is_completed = true;

CREATE TABLE task_days (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date    DATE NOT NULL,
    UNIQUE(task_id, date)
);

CREATE INDEX idx_task_days_user_date ON task_days(user_id, date);


-- =============================================
-- HÁBITOS DIARIOS
-- Fusiona pasos, cronómetro y salud tecnológica
-- =============================================

CREATE TABLE habits_daily (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                     DATE NOT NULL,
    steps                    INTEGER NOT NULL DEFAULT 0,
    steps_goal               INTEGER NOT NULL DEFAULT 2000,
    steps_finalized          BOOLEAN NOT NULL DEFAULT false,
    timer_point_earned       BOOLEAN NOT NULL DEFAULT false,
    tech_health_point_earned BOOLEAN NOT NULL DEFAULT false,
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, date)
);

CREATE INDEX idx_habits_daily_user_date ON habits_daily(user_id, date);


-- =============================================
-- PROGRESO DIARIO
-- Una fila por usuario, se reinicia cada noche
-- mediante fn_close_daily_progress()
-- =============================================

CREATE TABLE daily_progress (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                DATE NOT NULL DEFAULT CURRENT_DATE,
    steps               INTEGER NOT NULL DEFAULT 0,
    tasks_completed     INTEGER NOT NULL DEFAULT 0,
    tasks_points        INTEGER NOT NULL DEFAULT 0,
    steps_points        INTEGER NOT NULL DEFAULT 0,
    wellness_points     INTEGER NOT NULL DEFAULT 0,
    timer_points        INTEGER NOT NULL DEFAULT 0,
    tech_health_points  INTEGER NOT NULL DEFAULT 0,
    total_points        INTEGER NOT NULL DEFAULT 0,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);

-- =============================================
-- Salud Tecnologica
-- =============================================

CREATE TABLE tech_health_config (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    app_package TEXT NOT NULL,
    app_name    TEXT NOT NULL,
    limit_hours DECIMAL(4,1) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, app_package)
);

-- =============================================
-- HISTORIAL DIARIO
-- Hasta 30 filas por usuario, una por día cerrado
-- =============================================

CREATE TABLE user_daily_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                DATE NOT NULL,
    steps               INTEGER NOT NULL DEFAULT 0,
    steps_goal          INTEGER NOT NULL DEFAULT 0,
    tasks_completed     INTEGER NOT NULL DEFAULT 0,
    tasks_points        INTEGER NOT NULL DEFAULT 0,
    steps_points        INTEGER NOT NULL DEFAULT 0,
    wellness_points     INTEGER NOT NULL DEFAULT 0,
    timer_points        INTEGER NOT NULL DEFAULT 0,
    tech_health_points  INTEGER NOT NULL DEFAULT 0,
    total_points        INTEGER NOT NULL DEFAULT 0,
    streak_at_day        INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, date)
);

CREATE INDEX idx_user_daily_log_user_date ON user_daily_log(user_id, date DESC);


-- =============================================
-- RESUMEN SEMANAL
-- Generado cada semana mediante fn_generate_weekly_summary(), inmutable
-- =============================================

CREATE TABLE user_weekly_summary (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start            DATE NOT NULL,
    week_end              DATE NOT NULL,
    total_steps           INTEGER NOT NULL DEFAULT 0,
    total_tasks_completed INTEGER NOT NULL DEFAULT 0,
    total_points          INTEGER NOT NULL DEFAULT 0,
    best_streak           INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, week_start)
);

CREATE INDEX idx_weekly_summary_user ON user_weekly_summary(user_id, week_start DESC);


-- =============================================
-- LOG DE PUNTOS
-- =============================================

CREATE TABLE points_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points     INTEGER NOT NULL,
    source     TEXT NOT NULL CHECK (source IN ('STEPS', 'TASKS', 'WELLNESS', 'TIMER', 'TECH_HEALTH')),
    day_key    DATE NOT NULL,
    zone_id    TEXT,
    metadata   JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_points_log_user_day ON points_log(user_id, day_key DESC);


-- =============================================
-- SOCIAL
-- =============================================

CREATE TABLE friend_requests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    to_user_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(from_user_id, to_user_id),
    CHECK (from_user_id <> to_user_id)
);

CREATE INDEX idx_friend_requests_to ON friend_requests(to_user_id);

CREATE TABLE friends (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    since        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(requester_id, receiver_id),
    CHECK (requester_id <> receiver_id)
);

CREATE INDEX idx_friends_requester ON friends(requester_id);
CREATE INDEX idx_friends_receiver ON friends(receiver_id);

CREATE TABLE reactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    to_user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weekly_summary_id UUID NOT NULL REFERENCES user_weekly_summary(id) ON DELETE CASCADE,
    type              TEXT NOT NULL CHECK (type IN ('fire', 'clap', 'strong', 'star')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(from_user_id, to_user_id, weekly_summary_id),
    CHECK (from_user_id <> to_user_id)
);

CREATE INDEX idx_reactions_to ON reactions(to_user_id);
CREATE INDEX idx_reactions_summary ON reactions(weekly_summary_id);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       TEXT NOT NULL CHECK (type IN (
                   'FRIEND_REQUEST', 'FRIEND_ACCEPTED', 'REACTION',
                   'LEVEL_UP', 'STREAK_RISK', 'STEPS_GOAL',
                   'TIMER_DONE', 'TASK_REMINDER', 'DAILY_SUMMARY'
               )),
    title      TEXT NOT NULL,
    body       TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT false,
    metadata   JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
-- Users see/update/delete their own rows; any authenticated user can INSERT (needed for cross-user events)
CREATE POLICY "notifications_own"         ON notifications FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "notifications_insert_auth" ON notifications FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "notifications_update_own"  ON notifications FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "notifications_delete_own"  ON notifications FOR DELETE USING (auth.uid() = user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);


-- =============================================
-- TRIGGERS
-- =============================================

-- Actualiza daily_progress cuando cambian los hábitos diarios
CREATE OR REPLACE FUNCTION fn_sync_habits_to_progress()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE daily_progress SET
        steps      = NEW.steps,
        updated_at = now()
    WHERE user_id = NEW.user_id;

    IF NOT FOUND THEN
        INSERT INTO daily_progress (user_id, date, steps)
        VALUES (NEW.user_id, NEW.date, NEW.steps);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_habits_to_progress
AFTER INSERT OR UPDATE ON habits_daily
FOR EACH ROW EXECUTE FUNCTION fn_sync_habits_to_progress();

-- Actualiza tasks_completed en daily_progress al completar una tarea
CREATE OR REPLACE FUNCTION fn_task_completed_to_progress()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_completed = true AND OLD.is_completed = false THEN
        UPDATE daily_progress SET
            tasks_completed = tasks_completed + 1,
            updated_at      = now()
        WHERE user_id = NEW.user_id;

        IF NOT FOUND THEN
            INSERT INTO daily_progress (user_id, date, tasks_completed)
            VALUES (NEW.user_id, CURRENT_DATE, 1);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_task_completed_to_progress
AFTER UPDATE ON tasks
FOR EACH ROW EXECUTE FUNCTION fn_task_completed_to_progress();

-- NUEVO — Propaga cada evento de points_log a daily_progress (por categoría
-- y total) y al histórico del usuario. Sin esto points_log se quedaba como
-- un simple log sin ningún efecto sobre el resto del sistema.
CREATE OR REPLACE FUNCTION fn_sync_points_to_progress()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE daily_progress SET
        tasks_points       = tasks_points       + CASE WHEN NEW.source = 'TASKS'       THEN NEW.points ELSE 0 END,
        steps_points       = steps_points       + CASE WHEN NEW.source = 'STEPS'       THEN NEW.points ELSE 0 END,
        wellness_points    = wellness_points    + CASE WHEN NEW.source = 'WELLNESS'    THEN NEW.points ELSE 0 END,
        timer_points       = timer_points       + CASE WHEN NEW.source = 'TIMER'       THEN NEW.points ELSE 0 END,
        tech_health_points = tech_health_points + CASE WHEN NEW.source = 'TECH_HEALTH' THEN NEW.points ELSE 0 END,
        total_points       = total_points + NEW.points,
        updated_at         = now()
    WHERE user_id = NEW.user_id;

    IF NOT FOUND THEN
        INSERT INTO daily_progress (
            user_id, date, tasks_points, steps_points, wellness_points,
            timer_points, tech_health_points, total_points
        ) VALUES (
            NEW.user_id, NEW.day_key,
            CASE WHEN NEW.source = 'TASKS'       THEN NEW.points ELSE 0 END,
            CASE WHEN NEW.source = 'STEPS'       THEN NEW.points ELSE 0 END,
            CASE WHEN NEW.source = 'WELLNESS'    THEN NEW.points ELSE 0 END,
            CASE WHEN NEW.source = 'TIMER'       THEN NEW.points ELSE 0 END,
            CASE WHEN NEW.source = 'TECH_HEALTH' THEN NEW.points ELSE 0 END,
            NEW.points
        );
    END IF;

    UPDATE users SET total_points_historical = total_points_historical + NEW.points
    WHERE id = NEW.user_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_points_to_progress
AFTER INSERT ON points_log
FOR EACH ROW EXECUTE FUNCTION fn_sync_points_to_progress();

-- Actualiza el nivel al acumular puntos históricos
CREATE OR REPLACE FUNCTION fn_update_level()
RETURNS TRIGGER AS $$
DECLARE
    new_level INTEGER;
BEGIN
    new_level := GREATEST(1, FLOOR(NEW.total_points_historical / 50) + 1);
    IF new_level <> OLD.level THEN
        NEW.level := new_level;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_level
BEFORE UPDATE OF total_points_historical ON users
FOR EACH ROW EXECUTE FUNCTION fn_update_level();

-- NUEVO — Actualiza user_streaks cada vez que se cierra un día en
-- user_daily_log. Antes no existía ningún trigger que tocara user_streaks.
CREATE OR REPLACE FUNCTION fn_update_streak()
RETURNS TRIGGER AS $$
DECLARE
    last_date   DATE;
    curr_streak INTEGER;
    long_streak INTEGER;
    new_streak  INTEGER;
BEGIN
    SELECT last_active_date, current_streak, longest_streak
    INTO last_date, curr_streak, long_streak
    FROM user_streaks
    WHERE user_id = NEW.user_id;

    IF NOT FOUND THEN
        INSERT INTO user_streaks (user_id, current_streak, longest_streak, last_active_date)
        VALUES (NEW.user_id, 1, 1, NEW.date);
        new_streak := 1;
    ELSIF last_date = NEW.date THEN
        -- ya había un cierre para ese día, no duplicar
        new_streak := curr_streak;
    ELSIF last_date = NEW.date - 1 THEN
        -- día consecutivo
        new_streak := curr_streak + 1;
        UPDATE user_streaks SET
            current_streak   = new_streak,
            longest_streak   = GREATEST(long_streak, new_streak),
            last_active_date = NEW.date,
            updated_at       = now()
        WHERE user_id = NEW.user_id;
    ELSE
        -- racha rota
        new_streak := 1;
        UPDATE user_streaks SET
            current_streak   = 1,
            last_active_date = NEW.date,
            updated_at       = now()
        WHERE user_id = NEW.user_id;
    END IF;

    UPDATE user_daily_log SET streak_at_day = new_streak WHERE id = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_streak
AFTER INSERT ON user_daily_log
FOR EACH ROW EXECUTE FUNCTION fn_update_streak();

-- Elimina la fila más antigua de user_daily_log si hay más de 30
CREATE OR REPLACE FUNCTION fn_limit_daily_log()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM user_daily_log
    WHERE id IN (
        SELECT id FROM user_daily_log
        WHERE user_id = NEW.user_id
        ORDER BY date ASC
        OFFSET 30
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_limit_daily_log
AFTER INSERT ON user_daily_log
FOR EACH ROW EXECUTE FUNCTION fn_limit_daily_log();

-- Elimina tareas completadas con más de 20 días
CREATE OR REPLACE FUNCTION fn_cleanup_completed_tasks()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM tasks
    WHERE is_completed = true
      AND completed_at < now() - INTERVAL '20 days';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cleanup_completed_tasks
AFTER INSERT ON tasks
FOR EACH ROW EXECUTE FUNCTION fn_cleanup_completed_tasks();

-- Elimina points_log con más de 30 días
CREATE OR REPLACE FUNCTION fn_cleanup_points_log()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM points_log
    WHERE user_id = NEW.user_id
      AND day_key < CURRENT_DATE - INTERVAL '30 days';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cleanup_points_log
AFTER INSERT ON points_log
FOR EACH ROW EXECUTE FUNCTION fn_cleanup_points_log();


-- =============================================
-- TAREAS PROGRAMADAS (no son triggers de fila,
-- se ejecutan por tiempo vía pg_cron)
-- =============================================

-- NUEVO — Cierra el día: archiva daily_progress de cada usuario activo
-- (con algo de actividad real) en user_daily_log, y resetea daily_progress
-- para el nuevo día. El INSERT en user_daily_log es lo que dispara
-- fn_update_streak y fn_limit_daily_log. Sin esta función, daily_progress
-- nunca se vaciaba, user_daily_log nunca recibía filas y, por tanto,
-- las rachas y la gráfica de 30 días nunca se habrían generado.
CREATE OR REPLACE FUNCTION fn_close_daily_progress()
RETURNS void AS $$
BEGIN
    INSERT INTO user_daily_log (
        user_id, date, steps, steps_goal, tasks_completed,
        tasks_points, steps_points, wellness_points,
        timer_points, tech_health_points, total_points
    )
    SELECT
        dp.user_id,
        dp.date,
        dp.steps,
        COALESCE(hd.steps_goal, 2000),
        dp.tasks_completed,
        dp.tasks_points,
        dp.steps_points,
        dp.wellness_points,
        dp.timer_points,
        dp.tech_health_points,
        dp.total_points
    FROM daily_progress dp
    LEFT JOIN habits_daily hd
        ON hd.user_id = dp.user_id AND hd.date = dp.date
    WHERE dp.total_points > 0 OR dp.steps > 0 OR dp.tasks_completed > 0
    ON CONFLICT (user_id, date) DO NOTHING;

    UPDATE daily_progress SET
        date               = CURRENT_DATE,
        steps              = 0,
        tasks_completed    = 0,
        tasks_points       = 0,
        steps_points       = 0,
        wellness_points    = 0,
        timer_points       = 0,
        tech_health_points = 0,
        total_points       = 0,
        updated_at         = now();
END;
$$ LANGUAGE plpgsql;

-- NUEVO — Agrega los últimos 7 días cerrados de user_daily_log en una fila
-- de user_weekly_summary por usuario. Sin esto, user_weekly_summary (y por
-- tanto las reacciones de amigos, que apuntan a un weekly_summary_id) nunca
-- se habría poblado.
CREATE OR REPLACE FUNCTION fn_generate_weekly_summary()
RETURNS void AS $$
BEGIN
    INSERT INTO user_weekly_summary (
        user_id, week_start, week_end,
        total_steps, total_tasks_completed, total_points, best_streak
    )
    SELECT
        udl.user_id,
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE - INTERVAL '1 day',
        SUM(udl.steps),
        SUM(udl.tasks_completed),
        SUM(udl.total_points),
        MAX(udl.streak_at_day)
    FROM user_daily_log udl
    WHERE udl.date >= CURRENT_DATE - INTERVAL '7 days'
      AND udl.date < CURRENT_DATE
    GROUP BY udl.user_id
    ON CONFLICT (user_id, week_start) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- Cada noche a las 00:00 UTC, cierra el día de todos los usuarios
SELECT cron.schedule(
    'close-daily-progress',
    '0 0 * * *',
    $$SELECT fn_close_daily_progress();$$
);

-- Cada lunes a las 00:05 UTC (justo después de cerrar el domingo),
-- genera el resumen de la semana que acaba de terminar
SELECT cron.schedule(
    'generate-weekly-summary',
    '5 0 * * 1',
    $$SELECT fn_generate_weekly_summary();$$
);


-- =============================================
-- VISTAS
-- =============================================
-- security_invoker = true: la vista respeta las políticas RLS del usuario
-- que consulta, en vez de ejecutarse con los permisos de quien la creó
-- (comportamiento por defecto de Postgres, inseguro junto con RLS).

-- Ranking de amigos. Suma los puntos de los últimos 30 días ya cerrados
-- (user_daily_log) más los puntos de hoy aún sin cerrar (daily_progress),
-- para que el ranking responda en vivo a los puntos ganados en la sesión.
CREATE VIEW friends_ranking WITH (security_invoker = true) AS
SELECT
    u.id,
    u.name,
    u.username,
    u.photo_url,
    u.level,
    COALESCE(us.current_streak, 0) AS current_streak,
    COALESCE(SUM(udl.total_points), 0) + COALESCE(MAX(dp.total_points), 0) AS points_last_30_days
FROM users u
LEFT JOIN user_streaks us
    ON us.user_id = u.id
LEFT JOIN user_daily_log udl
    ON udl.user_id = u.id
    AND udl.date >= CURRENT_DATE - INTERVAL '30 days'
LEFT JOIN daily_progress dp
    ON dp.user_id = u.id
GROUP BY u.id, u.name, u.username, u.photo_url, u.level, COALESCE(us.current_streak, 0);

-- Resumen del día actual para la card del inicio
CREATE VIEW daily_summary WITH (security_invoker = true) AS
SELECT
    u.id AS user_id,
    COALESCE(dp.steps, 0)           AS steps_today,
    COALESCE(hd.steps_goal, 2000)   AS steps_goal,
    COALESCE(dp.tasks_completed, 0) AS tasks_completed,
    COALESCE(dp.total_points, 0)    AS points_today,
    COALESCE(us.current_streak, 0)  AS current_streak,
    COUNT(t.id) FILTER (
        WHERE t.difficulty = 'EASY'
        AND t.is_completed = false
    ) AS pending_easy,
    COUNT(t.id) FILTER (
        WHERE t.difficulty = 'MEDIUM'
        AND t.is_completed = false
    ) AS pending_medium,
    COUNT(t.id) FILTER (
        WHERE t.difficulty = 'HARD'
        AND t.is_completed = false
    ) AS pending_hard
FROM users u
LEFT JOIN daily_progress dp
    ON dp.user_id = u.id
LEFT JOIN habits_daily hd
    ON hd.user_id = u.id AND hd.date = CURRENT_DATE
LEFT JOIN user_streaks us
    ON us.user_id = u.id
LEFT JOIN task_days td
    ON td.user_id = u.id AND td.date = CURRENT_DATE
LEFT JOIN tasks t
    ON t.id = td.task_id
GROUP BY u.id, dp.steps, hd.steps_goal, dp.tasks_completed,
         dp.total_points, us.current_streak;

-- Tareas con fechas para el calendario
CREATE VIEW calendar_tasks WITH (security_invoker = true) AS
SELECT
    t.id,
    t.user_id,
    t.title,
    t.description,
    t.category,
    t.difficulty,
    t.is_completed,
    t.estimated_minutes,
    t.reminder_enabled,
    t.is_recurring,
    td.date
FROM tasks t
JOIN task_days td ON td.task_id = t.id
ORDER BY td.date ASC;


-- =============================================
-- ROW LEVEL SECURITY
-- =============================================

ALTER TABLE users                ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_streaks         ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks                ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_days            ENABLE ROW LEVEL SECURITY;
ALTER TABLE habits_daily         ENABLE ROW LEVEL SECURITY;
ALTER TABLE daily_progress       ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_daily_log       ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_weekly_summary  ENABLE ROW LEVEL SECURITY;
ALTER TABLE points_log           ENABLE ROW LEVEL SECURITY;
ALTER TABLE friend_requests      ENABLE ROW LEVEL SECURITY;
ALTER TABLE friends              ENABLE ROW LEVEL SECURITY;
ALTER TABLE reactions            ENABLE ROW LEVEL SECURITY;
ALTER TABLE tech_health_config   ENABLE ROW LEVEL SECURITY;

-- CORREGIDO — Antes solo se podía leer el propio perfil, lo que bloqueaba
-- por completo la búsqueda de amigos y el ranking (friends_ranking necesita
-- leer los users de los amigos, no solo el propio). Cualquier usuario
-- autenticado puede leer perfiles; solo el propio usuario escribe.
CREATE POLICY "users_read_authenticated"
ON users FOR SELECT
USING (auth.role() = 'authenticated');

CREATE POLICY "users_insert_own"
ON users FOR INSERT
WITH CHECK (auth.uid() = id);

CREATE POLICY "users_update_own"
ON users FOR UPDATE
USING (auth.uid() = id);

CREATE POLICY "users_delete_own"
ON users FOR DELETE
USING (auth.uid() = id);

-- Rachas: lectura propia + amigos (friends_ranking VIEW necesita leer el streak
-- de los amigos con security_invoker=true); escritura solo propia.
CREATE POLICY "streaks_select"
ON user_streaks FOR SELECT
USING (
    auth.uid() = user_id
    OR EXISTS (
        SELECT 1 FROM friends
        WHERE (requester_id = auth.uid() AND receiver_id = user_id)
        OR    (receiver_id = auth.uid() AND requester_id = user_id)
    )
);

CREATE POLICY "streaks_insert_own"
ON user_streaks FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "streaks_update_own"
ON user_streaks FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "streaks_delete_own"
ON user_streaks FOR DELETE
USING (auth.uid() = user_id);

-- Tareas
CREATE POLICY "tasks_own"
ON tasks FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "task_days_own"
ON task_days FOR ALL
USING (auth.uid() = user_id);

-- Hábitos
CREATE POLICY "habits_daily_own"
ON habits_daily FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "tech_health_config_own"
ON tech_health_config FOR ALL
USING (auth.uid() = user_id);

-- CORREGIDO — El comentario original decía "los amigos pueden leer" pero la
-- política solo permitía auth.uid() = user_id para todo, sin excepción de
-- lectura. Se separa en escritura (propio usuario) y lectura (propio + amigos),
-- igual que ya se hacía en user_daily_log y user_weekly_summary.
CREATE POLICY "daily_progress_insert_own"
ON daily_progress FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "daily_progress_update_own"
ON daily_progress FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "daily_progress_delete_own"
ON daily_progress FOR DELETE
USING (auth.uid() = user_id);

CREATE POLICY "daily_progress_read"
ON daily_progress FOR SELECT
USING (
    auth.uid() = user_id
    OR EXISTS (
        SELECT 1 FROM friends
        WHERE (requester_id = auth.uid() AND receiver_id = user_id)
        OR    (receiver_id = auth.uid() AND requester_id = user_id)
    )
);

-- Historial diario: el propio usuario escribe, los amigos pueden leer
CREATE POLICY "daily_log_own_write"
ON user_daily_log FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "daily_log_read"
ON user_daily_log FOR SELECT
USING (
    auth.uid() = user_id
    OR EXISTS (
        SELECT 1 FROM friends
        WHERE (requester_id = auth.uid() AND receiver_id = user_id)
        OR    (receiver_id = auth.uid() AND requester_id = user_id)
    )
);

-- Resumen semanal: el propio usuario escribe, los amigos pueden leer
CREATE POLICY "weekly_summary_own_write"
ON user_weekly_summary FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "weekly_summary_read"
ON user_weekly_summary FOR SELECT
USING (
    auth.uid() = user_id
    OR EXISTS (
        SELECT 1 FROM friends
        WHERE (requester_id = auth.uid() AND receiver_id = user_id)
        OR    (receiver_id = auth.uid() AND requester_id = user_id)
    )
);

-- Puntos
CREATE POLICY "points_log_own"
ON points_log FOR ALL
USING (auth.uid() = user_id);

-- Solicitudes de amistad
CREATE POLICY "friend_requests_own"
ON friend_requests FOR ALL
USING (
    auth.uid() = from_user_id
    OR auth.uid() = to_user_id
);

-- Amigos
CREATE POLICY "friends_own"
ON friends FOR ALL
USING (
    auth.uid() = requester_id
    OR auth.uid() = receiver_id
);

-- Reacciones
CREATE POLICY "reactions_write"
ON reactions FOR INSERT
WITH CHECK (auth.uid() = from_user_id);

CREATE POLICY "reactions_read"
ON reactions FOR SELECT
USING (
    auth.uid() = from_user_id
    OR auth.uid() = to_user_id
);


-- =============================================
-- MIGRATION PATCH (apply to existing databases)
-- Run this section in Supabase SQL Editor if the
-- schema was deployed before this fix was added.
-- =============================================

-- 1. Recreate friends_ranking VIEW with COALESCE so current_streak is never
--    NULL (was causing FriendsRankingDto deserialization to fail silently).
CREATE OR REPLACE VIEW friends_ranking WITH (security_invoker = true) AS
SELECT
    u.id,
    u.name,
    u.username,
    u.photo_url,
    u.level,
    COALESCE(us.current_streak, 0) AS current_streak,
    COALESCE(SUM(udl.total_points), 0) + COALESCE(MAX(dp.total_points), 0) AS points_last_30_days
FROM users u
LEFT JOIN user_streaks us
    ON us.user_id = u.id
LEFT JOIN user_daily_log udl
    ON udl.user_id = u.id
    AND udl.date >= CURRENT_DATE - INTERVAL '30 days'
LEFT JOIN daily_progress dp
    ON dp.user_id = u.id
GROUP BY u.id, u.name, u.username, u.photo_url, u.level, COALESCE(us.current_streak, 0);

-- 2. Split streaks_own FOR ALL into a friends-readable SELECT + own-only writes,
--    so the VIEW can read friends' actual streak values via security_invoker=true.
DROP POLICY IF EXISTS "streaks_own"        ON user_streaks;
DROP POLICY IF EXISTS "streaks_select"     ON user_streaks;
DROP POLICY IF EXISTS "streaks_insert_own" ON user_streaks;
DROP POLICY IF EXISTS "streaks_update_own" ON user_streaks;
DROP POLICY IF EXISTS "streaks_delete_own" ON user_streaks;

CREATE POLICY "streaks_select"
ON user_streaks FOR SELECT
USING (
    auth.uid() = user_id
    OR EXISTS (
        SELECT 1 FROM friends
        WHERE (requester_id = auth.uid() AND receiver_id = user_id)
        OR    (receiver_id = auth.uid() AND requester_id = user_id)
    )
);

CREATE POLICY "streaks_insert_own"
ON user_streaks FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "streaks_update_own"
ON user_streaks FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "streaks_delete_own"
ON user_streaks FOR DELETE
USING (auth.uid() = user_id);

-- 3. Notifications table (new in this patch)
CREATE TABLE IF NOT EXISTS notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       TEXT NOT NULL CHECK (type IN (
                   'FRIEND_REQUEST', 'FRIEND_ACCEPTED', 'REACTION',
                   'LEVEL_UP', 'STREAK_RISK', 'STEPS_GOAL',
                   'TIMER_DONE', 'TASK_REMINDER', 'DAILY_SUMMARY'
               )),
    title      TEXT NOT NULL,
    body       TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT false,
    metadata   JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "notifications_own"         ON notifications;
DROP POLICY IF EXISTS "notifications_insert_auth"  ON notifications;
DROP POLICY IF EXISTS "notifications_update_own"   ON notifications;
DROP POLICY IF EXISTS "notifications_delete_own"   ON notifications;
DROP POLICY IF EXISTS "notif_select_own"           ON notifications;
DROP POLICY IF EXISTS "notif_insert_auth"          ON notifications;
DROP POLICY IF EXISTS "notif_update_own"           ON notifications;
DROP POLICY IF EXISTS "notif_delete_own"           ON notifications;

CREATE POLICY "notifications_own"         ON notifications FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "notifications_insert_auth" ON notifications FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "notifications_update_own"  ON notifications FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "notifications_delete_own"  ON notifications FOR DELETE USING (auth.uid() = user_id);

DROP INDEX IF EXISTS idx_notifications_user;
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);
