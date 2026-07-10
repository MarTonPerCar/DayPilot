CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE EXTENSION IF NOT EXISTS pg_cron;

CREATE TABLE users (
    id                       UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email                    TEXT NOT NULL,
    name                     TEXT NOT NULL,
    username                 TEXT NOT NULL UNIQUE,
    username_lower           TEXT NOT NULL UNIQUE,
    photo_url                TEXT,
    region                   TEXT,
    level                    INTEGER NOT NULL DEFAULT 1,
    total_points_historical  INTEGER NOT NULL DEFAULT 0,
    points_to_next_level     INTEGER NOT NULL DEFAULT 20,
    pending_steps_goal       INTEGER,
    pending_steps_goal_date  DATE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username_lower ON users(username_lower);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_streaks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_streak   INTEGER NOT NULL DEFAULT 0,
    longest_streak   INTEGER NOT NULL DEFAULT 0,
    last_active_date DATE,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);

CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               TEXT NOT NULL,
    description         TEXT,
    category            TEXT NOT NULL DEFAULT 'General',
    difficulty          TEXT NOT NULL DEFAULT 'MEDIUM'
                            CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    estimated_minutes   INTEGER NOT NULL DEFAULT 30,
    reminder_enabled    BOOLEAN NOT NULL DEFAULT false,
    is_recurring        BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);

CREATE TABLE task_days (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id      UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date         DATE NOT NULL,

    is_completed BOOLEAN NOT NULL DEFAULT false,
    completed_at TIMESTAMPTZ,
    is_earned    BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(task_id, date)
);

CREATE INDEX idx_task_days_user_date ON task_days(user_id, date);

CREATE TABLE habits_daily (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                     DATE NOT NULL,
    steps                    INTEGER NOT NULL DEFAULT 0,
    steps_goal               INTEGER NOT NULL DEFAULT 2000,
    timer_point_earned       BOOLEAN NOT NULL DEFAULT false,
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, date)
);

CREATE INDEX idx_habits_daily_user_date ON habits_daily(user_id, date);

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

CREATE TABLE tech_health_config (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    app_package         TEXT NOT NULL,
    app_name            TEXT NOT NULL,
    limit_hours         DECIMAL(4,1) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT true,

    pending_active      BOOLEAN,
    pending_limit_hours DECIMAL(4,1),
    is_violated_today   BOOLEAN NOT NULL DEFAULT false,
    pending_delete      BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, app_package)
);

CREATE TABLE tech_health_group_config (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_name          TEXT NOT NULL,
    limit_hours         DECIMAL(4,1) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    pending_active      BOOLEAN,
    pending_limit_hours DECIMAL(4,1),
    is_violated_today   BOOLEAN NOT NULL DEFAULT false,
    pending_delete      BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, group_name)
);

CREATE INDEX idx_tech_health_group_config_user ON tech_health_group_config(user_id);

CREATE TABLE tech_health_group_apps (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id     UUID NOT NULL REFERENCES tech_health_group_config(id) ON DELETE CASCADE,
    app_package  TEXT NOT NULL,
    app_name     TEXT NOT NULL,
    UNIQUE(group_id, app_package)
);

CREATE INDEX idx_tech_health_group_apps_group ON tech_health_group_apps(group_id);

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
    streak_at_day       INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, date)
);

CREATE INDEX idx_user_daily_log_user_date ON user_daily_log(user_id, date DESC);

CREATE TABLE user_weekly_summary (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start            DATE NOT NULL,
    total_steps           INTEGER NOT NULL DEFAULT 0,
    total_tasks_completed INTEGER NOT NULL DEFAULT 0,
    total_points          INTEGER NOT NULL DEFAULT 0,
    best_streak           INTEGER NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id)
);

CREATE INDEX idx_weekly_summary_user ON user_weekly_summary(user_id, week_start DESC);

CREATE TABLE points_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points     INTEGER NOT NULL CHECK (points > 0),
    source     TEXT NOT NULL CHECK (source IN ('STEPS', 'TASKS', 'WELLNESS', 'TIMER', 'TECH_HEALTH')),
    day_key    DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_points_log_user_day ON points_log(user_id, day_key DESC);

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

CREATE UNIQUE INDEX reactions_from_user_summary_key ON reactions(from_user_id, weekly_summary_id);
CREATE INDEX idx_reactions_to ON reactions(to_user_id);
CREATE INDEX idx_reactions_summary ON reactions(weekly_summary_id);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       TEXT NOT NULL CHECK (type IN (
                   'FRIEND_REQUEST', 'FRIEND_ACCEPTED', 'REACTION',
                   'LEVEL_UP', 'STREAK_RISK', 'STEPS_GOAL', 'TASK_COMPLETED',
                   'TIMER_DONE', 'TASK_REMINDER', 'DAILY_SUMMARY'
               )),
    title      TEXT NOT NULL,
    body       TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

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
AFTER UPDATE ON task_days
FOR EACH ROW EXECUTE FUNCTION fn_task_completed_to_progress();

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

CREATE OR REPLACE FUNCTION fn_update_level()
RETURNS TRIGGER AS $$
DECLARE
    new_level INTEGER;
BEGIN
    new_level := GREATEST(1,
        FLOOR((-1.0 + SQRT(1.0 + 4.0 * (2.0 + NEW.total_points_historical::FLOAT / 5.0))) / 2.0)::INTEGER
    );
    IF new_level <> OLD.level THEN
        NEW.level := new_level;
    END IF;
    NEW.points_to_next_level := 5 * new_level * (new_level + 3);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_level
BEFORE UPDATE OF total_points_historical ON users
FOR EACH ROW EXECUTE FUNCTION fn_update_level();

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
        new_streak := curr_streak;
    ELSIF last_date = NEW.date - 1 THEN
        new_streak := curr_streak + 1;
        UPDATE user_streaks SET
            current_streak   = new_streak,
            longest_streak   = GREATEST(long_streak, new_streak),
            last_active_date = NEW.date,
            updated_at       = now()
        WHERE user_id = NEW.user_id;
    ELSE
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

-- Creates the public.users profile row the moment an auth.users row is
-- created (signup), instead of relying on the client to do it later —
-- works regardless of email confirmation timing.
CREATE OR REPLACE FUNCTION fn_create_user_profile()
RETURNS TRIGGER
SECURITY DEFINER SET search_path = public
AS $$
DECLARE
    v_username TEXT;
BEGIN
    v_username := COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1));

    BEGIN
        INSERT INTO users (id, email, name, username, username_lower, region)
        VALUES (
            NEW.id,
            NEW.email,
            COALESCE(NEW.raw_user_meta_data->>'name', v_username),
            v_username,
            lower(v_username),
            NEW.raw_user_meta_data->>'region'
        );
    EXCEPTION WHEN unique_violation THEN
        -- Username taken — fall back to a suffixed one rather than failing
        -- the whole signup transaction.
        INSERT INTO users (id, email, name, username, username_lower, region)
        VALUES (
            NEW.id,
            NEW.email,
            COALESCE(NEW.raw_user_meta_data->>'name', v_username),
            v_username || '_' || substr(NEW.id::text, 1, 6),
            lower(v_username || '_' || substr(NEW.id::text, 1, 6)),
            NEW.raw_user_meta_data->>'region'
        );
    END;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_user_profile
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION fn_create_user_profile();

CREATE OR REPLACE FUNCTION fn_seed_daily_progress()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO daily_progress (user_id, date)
    VALUES (NEW.id, CURRENT_DATE)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_seed_daily_progress
AFTER INSERT ON users
FOR EACH ROW EXECUTE FUNCTION fn_seed_daily_progress();

CREATE OR REPLACE FUNCTION fn_cleanup_completed_tasks()
RETURNS void AS $$
BEGIN
    DELETE FROM task_days
    WHERE is_completed = true
      AND completed_at < now() - INTERVAL '20 days';

    DELETE FROM tasks t
    WHERE NOT EXISTS (SELECT 1 FROM task_days td WHERE td.task_id = t.id);
END;
$$ LANGUAGE plpgsql;

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

CREATE OR REPLACE FUNCTION fn_close_daily_progress()
RETURNS void AS $$
BEGIN

    INSERT INTO points_log (user_id, points, source, day_key)
    SELECT eligible.user_id, 10, 'TECH_HEALTH', CURRENT_DATE - INTERVAL '1 day'
    FROM (
        SELECT
            u.id AS user_id,
            COALESCE(app_counts.n, 0) + COALESCE(group_counts.n, 0)             AS restriction_count,
            COALESCE(app_counts.violated, 0) + COALESCE(group_counts.violated, 0) AS violated_count
        FROM users u
        LEFT JOIN (
            SELECT user_id,
                   COUNT(*)                                   AS n,
                   COUNT(*) FILTER (WHERE is_violated_today)   AS violated
            FROM tech_health_config
            WHERE is_active = true
            GROUP BY user_id
        ) app_counts ON app_counts.user_id = u.id
        LEFT JOIN (
            SELECT g.user_id,
                   SUM(m.n)                                            AS n,
                   SUM(CASE WHEN g.is_violated_today THEN m.n ELSE 0 END) AS violated
            FROM tech_health_group_config g
            JOIN (
                SELECT group_id, COUNT(*) AS n
                FROM tech_health_group_apps
                GROUP BY group_id
            ) m ON m.group_id = g.id
            WHERE g.is_active = true
            GROUP BY g.user_id
        ) group_counts ON group_counts.user_id = u.id
    ) eligible
    WHERE eligible.restriction_count >= 3
      AND eligible.violated_count = 0
      AND NOT EXISTS (
          SELECT 1 FROM points_log pl
          WHERE pl.user_id = eligible.user_id
            AND pl.day_key = CURRENT_DATE - INTERVAL '1 day'
            AND pl.source  = 'TECH_HEALTH'
      );

    DELETE FROM tech_health_config       WHERE pending_delete = true;
    DELETE FROM tech_health_group_config WHERE pending_delete = true;

    UPDATE tech_health_config
    SET is_active = pending_active, pending_active = NULL
    WHERE pending_active IS NOT NULL;

    UPDATE tech_health_group_config
    SET is_active = pending_active, pending_active = NULL
    WHERE pending_active IS NOT NULL;

    UPDATE tech_health_config
    SET limit_hours = pending_limit_hours, pending_limit_hours = NULL
    WHERE pending_limit_hours IS NOT NULL;

    UPDATE tech_health_group_config
    SET limit_hours = pending_limit_hours, pending_limit_hours = NULL
    WHERE pending_limit_hours IS NOT NULL;

    UPDATE tech_health_config       SET is_violated_today = false WHERE is_violated_today = true;
    UPDATE tech_health_group_config SET is_violated_today = false WHERE is_violated_today = true;

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

CREATE OR REPLACE FUNCTION fn_generate_weekly_summary()
RETURNS void AS $$
BEGIN
    DELETE FROM reactions
    WHERE weekly_summary_id IN (SELECT id FROM user_weekly_summary);

    INSERT INTO user_weekly_summary (
        user_id, week_start,
        total_steps, total_tasks_completed, total_points, best_streak
    )
    SELECT
        udl.user_id,
        CURRENT_DATE - INTERVAL '7 days',
        SUM(udl.steps),
        SUM(udl.tasks_completed),
        SUM(udl.total_points),
        MAX(udl.streak_at_day)
    FROM user_daily_log udl
    WHERE udl.date >= CURRENT_DATE - INTERVAL '7 days'
      AND udl.date < CURRENT_DATE
    GROUP BY udl.user_id
    ON CONFLICT (user_id) DO UPDATE SET
        week_start            = EXCLUDED.week_start,
        total_steps           = EXCLUDED.total_steps,
        total_tasks_completed = EXCLUDED.total_tasks_completed,
        total_points          = EXCLUDED.total_points,
        best_streak           = EXCLUDED.best_streak,
        created_at            = now();
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_apply_pending_steps_goals()
RETURNS void AS $$
BEGIN
    UPDATE users
    SET pending_steps_goal      = NULL,
        pending_steps_goal_date = NULL
    WHERE pending_steps_goal IS NOT NULL
      AND pending_steps_goal_date <= CURRENT_DATE;
END;
$$ LANGUAGE plpgsql;

SELECT cron.schedule(
    'close-daily-progress',
    '0 0 * * *',
    $$SELECT fn_close_daily_progress();$$
);

SELECT cron.schedule(
    'generate-weekly-summary',
    '5 0 * * 1',
    $$SELECT fn_generate_weekly_summary();$$
);

SELECT cron.schedule(
    'cleanup-completed-tasks',
    '15 0 * * *',
    $$SELECT fn_cleanup_completed_tasks();$$
);

SELECT cron.schedule(
    'apply-pending-steps-goals',
    '2 0 * * *',
    $$SELECT fn_apply_pending_steps_goals();$$
);

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

CREATE VIEW calendar_tasks WITH (security_invoker = true) AS
SELECT
    td.id AS occurrence_id,
    t.id  AS task_id,
    t.user_id,
    t.title,
    t.description,
    t.category,
    t.difficulty,
    td.is_completed,
    td.is_earned,
    t.estimated_minutes,
    t.reminder_enabled,
    t.is_recurring,
    td.date
FROM tasks t
JOIN task_days td ON td.task_id = t.id
ORDER BY td.date ASC;

ALTER TABLE users                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_streaks              ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_days                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE habits_daily              ENABLE ROW LEVEL SECURITY;
ALTER TABLE daily_progress            ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_daily_log            ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_weekly_summary       ENABLE ROW LEVEL SECURITY;
ALTER TABLE points_log                ENABLE ROW LEVEL SECURITY;
ALTER TABLE friend_requests           ENABLE ROW LEVEL SECURITY;
ALTER TABLE friends                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE reactions                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications             ENABLE ROW LEVEL SECURITY;
ALTER TABLE tech_health_config        ENABLE ROW LEVEL SECURITY;
ALTER TABLE tech_health_group_config  ENABLE ROW LEVEL SECURITY;
ALTER TABLE tech_health_group_apps    ENABLE ROW LEVEL SECURITY;

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

CREATE POLICY "tasks_own"
ON tasks FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "task_days_own"
ON task_days FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "habits_daily_own"
ON habits_daily FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "tech_health_config_own"
ON tech_health_config FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "tech_health_group_config_own"
ON tech_health_group_config FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "tech_health_group_apps_own"
ON tech_health_group_apps FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM tech_health_group_config g
        WHERE g.id = tech_health_group_apps.group_id
          AND g.user_id = auth.uid()
    )
);

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

CREATE POLICY "points_log_own"
ON points_log FOR ALL
USING (auth.uid() = user_id);

CREATE POLICY "friend_requests_own"
ON friend_requests FOR ALL
USING (
    auth.uid() = from_user_id
    OR auth.uid() = to_user_id
);

CREATE POLICY "friends_own"
ON friends FOR ALL
USING (
    auth.uid() = requester_id
    OR auth.uid() = receiver_id
);

CREATE POLICY "reactions_write"
ON reactions FOR INSERT
WITH CHECK (auth.uid() = from_user_id);

CREATE POLICY "reactions_read"
ON reactions FOR SELECT
USING (
    auth.uid() = from_user_id
    OR auth.uid() = to_user_id
);

CREATE POLICY "notifications_own"         ON notifications FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "notifications_insert_auth" ON notifications FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "notifications_update_own"  ON notifications FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "notifications_delete_own"  ON notifications FOR DELETE USING (auth.uid() = user_id);

-- Realtime (Database → Replication): the client subscribes to live row
-- changes on these tables via Supabase Realtime's postgres_changes, each
-- filtered to auth.uid() so a user only ever receives their own rows.
ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
ALTER PUBLICATION supabase_realtime ADD TABLE public.daily_progress;
