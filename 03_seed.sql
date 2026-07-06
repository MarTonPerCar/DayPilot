INSERT INTO auth.users (
    id, instance_id, aud, role, email, encrypted_password,
    email_confirmed_at, raw_app_meta_data, raw_user_meta_data,
    confirmation_token, email_change, email_change_token_new, recovery_token,
    created_at, updated_at
) VALUES
    ('a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'ana.garcia@daypilot.test', crypt('password123', gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', '', '', '', '', now(), now()),
    ('a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'carlos.ruiz@daypilot.test', crypt('password123', gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', '', '', '', '', now(), now()),
    ('a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'maria.lopez@daypilot.test', crypt('password123', gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', '', '', '', '', now(), now()),
    ('a0000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'javier.moreno@daypilot.test', crypt('password123', gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', '', '', '', '', now(), now()),
    ('a0000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'lucia.fernandez@daypilot.test', crypt('password123', gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', '', '', '', '', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO auth.identities (
    id, user_id, provider_id, identity_data, provider,
    last_sign_in_at, created_at, updated_at
)
SELECT
    u.id, u.id, u.id::text,
    format('{"sub": "%s", "email": "%s"}', u.id::text, u.email)::jsonb,
    'email', now(), now(), now()
FROM auth.users u
WHERE u.id IN (
    'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000005'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, name, username, username_lower, region) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ana.garcia@daypilot.test',      'Ana García',      'anagarcia',       'anagarcia',       'Canarias'),
    ('a0000000-0000-0000-0000-000000000002', 'carlos.ruiz@daypilot.test',     'Carlos Ruiz',     'carlosruiz',      'carlosruiz',      'Madrid'),
    ('a0000000-0000-0000-0000-000000000003', 'maria.lopez@daypilot.test',     'María López',     'marialopez',      'marialopez',      'Cataluña'),
    ('a0000000-0000-0000-0000-000000000004', 'javier.moreno@daypilot.test',   'Javier Moreno',   'javiermoreno',    'javiermoreno',    'Andalucía'),
    ('a0000000-0000-0000-0000-000000000005', 'lucia.fernandez@daypilot.test', 'Lucía Fernández', 'luciafernandez',  'luciafernandez',  'Galicia')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_completed * 20 AS tasks_points,
       steps_points, wellness_points, timer_points, tech_health_points,
       tasks_completed * 20 + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000001'::uuid AS user_id,
        d::date AS date,
        (4500 + (random() * 3500))::int AS steps,
        6000 AS steps_goal,
        (2 + floor(random() * 3))::int AS tasks_completed,
        (ARRAY[10, 30, 60])[1 + floor(random() * 3)::int] AS steps_points,
        10 AS wellness_points, 10 AS timer_points, 10 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 14, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_completed * 20 AS tasks_points,
       steps_points, wellness_points, timer_points, tech_health_points,
       tasks_completed * 20 + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000002'::uuid AS user_id,
        d::date AS date,
        (3000 + (random() * 3000))::int AS steps,
        5000 AS steps_goal,
        (1 + floor(random() * 3))::int AS tasks_completed,
        (ARRAY[0, 10, 30])[1 + floor(random() * 3)::int] AS steps_points,
        0 AS wellness_points, 10 AS timer_points, 0 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 5, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_completed * 20 AS tasks_points,
       steps_points, wellness_points, timer_points, tech_health_points,
       tasks_completed * 20 + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000003'::uuid AS user_id,
        d::date AS date,
        (6000 + (random() * 4000))::int AS steps,
        8000 AS steps_goal,
        (3 + floor(random() * 4))::int AS tasks_completed,
        (ARRAY[10, 30, 60])[1 + floor(random() * 3)::int] AS steps_points,
        10 AS wellness_points, 10 AS timer_points, 10 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 10, CURRENT_DATE - 6, INTERVAL '1 day') AS d
    UNION ALL
    SELECT
        'a0000000-0000-0000-0000-000000000003'::uuid,
        d::date,
        (6000 + (random() * 4000))::int,
        8000,
        (3 + floor(random() * 4))::int,
        (ARRAY[10, 30, 60])[1 + floor(random() * 3)::int],
        10, 10, 10
    FROM generate_series(CURRENT_DATE - 4, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_completed * 20 AS tasks_points,
       steps_points, wellness_points, timer_points, tech_health_points,
       tasks_completed * 20 + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000004'::uuid AS user_id,
        d::date AS date,
        (1500 + (random() * 2000))::int AS steps,
        4000 AS steps_goal,
        (0 + floor(random() * 3))::int AS tasks_completed,
        (ARRAY[0, 10])[1 + floor(random() * 2)::int] AS steps_points,
        0 AS wellness_points, 0 AS timer_points, 0 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 3, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_completed * 20 AS tasks_points,
       steps_points, wellness_points, timer_points, tech_health_points,
       tasks_completed * 20 + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000005'::uuid AS user_id,
        d::date AS date,
        (7000 + (random() * 5000))::int AS steps,
        10000 AS steps_goal,
        (3 + floor(random() * 5))::int AS tasks_completed,
        (ARRAY[30, 60])[1 + floor(random() * 2)::int] AS steps_points,
        10 AS wellness_points, 10 AS timer_points, 10 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 20, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

UPDATE users u SET total_points_historical = COALESCE(
    (SELECT SUM(total_points) FROM user_daily_log WHERE user_id = u.id), 0
)
WHERE u.id IN (
    'a0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000005'
);

INSERT INTO friend_requests (from_user_id, to_user_id) VALUES
    ('a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001')
ON CONFLICT (from_user_id, to_user_id) DO NOTHING;

INSERT INTO friends (requester_id, receiver_id) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'),
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004')
ON CONFLICT (requester_id, receiver_id) DO NOTHING;

INSERT INTO habits_daily (user_id, date, steps, steps_goal)
VALUES ('a0000000-0000-0000-0000-000000000001', CURRENT_DATE, 3200, 6000)
ON CONFLICT (user_id, date) DO UPDATE SET
    steps = EXCLUDED.steps, steps_goal = EXCLUDED.steps_goal;

INSERT INTO tasks (user_id, title, description, category, difficulty, estimated_minutes) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Comprar fruta',                  NULL, 'Hogar',    'EASY',   15),
    ('a0000000-0000-0000-0000-000000000001', 'Repasar apuntes de BD',          NULL, 'Estudio',  'HARD',   90),
    ('a0000000-0000-0000-0000-000000000001', 'Llamar al dentista',             NULL, 'Salud',    'MEDIUM', 10),
    ('a0000000-0000-0000-0000-000000000001', 'Entregar práctica de Kotlin',    NULL, 'Estudio',  'HARD',  120),
    ('a0000000-0000-0000-0000-000000000001', 'Pagar el alquiler',              NULL, 'General',  'EASY',   10);

INSERT INTO task_days (task_id, user_id, date)
SELECT t.id, t.user_id, CASE t.title
    WHEN 'Comprar fruta'               THEN CURRENT_DATE
    WHEN 'Repasar apuntes de BD'       THEN CURRENT_DATE + 1
    WHEN 'Llamar al dentista'          THEN CURRENT_DATE + 2
    WHEN 'Entregar práctica de Kotlin' THEN CURRENT_DATE - 2
    WHEN 'Pagar el alquiler'           THEN CURRENT_DATE - 1
END
FROM tasks t
WHERE t.user_id = 'a0000000-0000-0000-0000-000000000001';

UPDATE task_days td SET
    is_completed = true,
    completed_at = CASE t.title
        WHEN 'Entregar práctica de Kotlin' THEN now() - INTERVAL '2 days'
        WHEN 'Pagar el alquiler'           THEN now() - INTERVAL '1 day'
    END,
    is_earned    = true
FROM tasks t
WHERE td.task_id = t.id
  AND t.user_id = 'a0000000-0000-0000-0000-000000000001'
  AND t.title IN ('Entregar práctica de Kotlin', 'Pagar el alquiler');

INSERT INTO tasks (user_id, title, category, difficulty, estimated_minutes)
VALUES ('a0000000-0000-0000-0000-000000000001', 'Estirar 10 minutos', 'Salud', 'EASY', 10);

INSERT INTO task_days (task_id, user_id, date)
SELECT id, user_id, CURRENT_DATE FROM tasks
WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND title = 'Estirar 10 minutos';

UPDATE task_days SET is_completed = true, completed_at = now(), is_earned = true
WHERE task_id = (
    SELECT id FROM tasks
    WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND title = 'Estirar 10 minutos'
);

INSERT INTO tech_health_config (user_id, app_package, app_name, limit_hours, is_active) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'com.instagram.android', 'Instagram', 1.0, true),
    ('a0000000-0000-0000-0000-000000000001', 'com.zhiliaoapp.musically', 'TikTok', 0.5, true)
ON CONFLICT (user_id, app_package) DO NOTHING;

INSERT INTO tech_health_group_config (user_id, group_name, limit_hours, is_active) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Redes sociales', 2.0, true)
ON CONFLICT (user_id, group_name) DO NOTHING;

INSERT INTO tech_health_group_apps (group_id, app_package, app_name)
SELECT id, 'com.whatsapp', 'WhatsApp' FROM tech_health_group_config
WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND group_name = 'Redes sociales'
UNION ALL
SELECT id, 'com.twitter.android', 'X' FROM tech_health_group_config
WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND group_name = 'Redes sociales'
ON CONFLICT (group_id, app_package) DO NOTHING;

INSERT INTO points_log (user_id, points, source, day_key) VALUES
    ('a0000000-0000-0000-0000-000000000001', 10, 'STEPS',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001', 20, 'TASKS',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001', 10, 'TIMER',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001', 10, 'WELLNESS',    CURRENT_DATE);

SELECT fn_generate_weekly_summary();

INSERT INTO reactions (from_user_id, to_user_id, weekly_summary_id, type)
SELECT 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', id, 'fire'
FROM user_weekly_summary
WHERE user_id = 'a0000000-0000-0000-0000-000000000003'
ORDER BY week_start DESC LIMIT 1
ON CONFLICT (from_user_id, to_user_id, weekly_summary_id) DO NOTHING;

INSERT INTO reactions (from_user_id, to_user_id, weekly_summary_id, type)
SELECT 'a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', id, 'clap'
FROM user_weekly_summary
WHERE user_id = 'a0000000-0000-0000-0000-000000000001'
ORDER BY week_start DESC LIMIT 1
ON CONFLICT (from_user_id, to_user_id, weekly_summary_id) DO NOTHING;

INSERT INTO reactions (from_user_id, to_user_id, weekly_summary_id, type)
SELECT 'a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001', id, 'star'
FROM user_weekly_summary
WHERE user_id = 'a0000000-0000-0000-0000-000000000001'
ORDER BY week_start DESC LIMIT 1
ON CONFLICT (from_user_id, to_user_id, weekly_summary_id) DO NOTHING;
