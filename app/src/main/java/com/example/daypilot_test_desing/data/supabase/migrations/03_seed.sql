-- =============================================
-- DAYPILOT — SEED DE DATOS DE PRUEBA
-- Solo para entornos de desarrollo / pruebas.
-- Contraseña de todos los usuarios: password123
-- =============================================


-- =============================================
-- USUARIOS DE PRUEBA (auth.users + auth.identities)
-- =============================================

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


-- =============================================
-- PERFILES (public.users)
-- level se fija a mano porque fn_update_level solo se dispara en UPDATE,
-- no en INSERT (debe coincidir con floor(total_points_historical / 50) + 1)
-- =============================================

INSERT INTO users (id, email, name, username, username_lower, region, level, total_points_historical) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ana.garcia@daypilot.test',      'Ana García',      'anagarcia',       'anagarcia',       'Canarias',   3, 130),
    ('a0000000-0000-0000-0000-000000000002', 'carlos.ruiz@daypilot.test',     'Carlos Ruiz',     'carlosruiz',      'carlosruiz',      'Madrid',     2, 80),
    ('a0000000-0000-0000-0000-000000000003', 'maria.lopez@daypilot.test',     'María López',     'marialopez',      'marialopez',      'Cataluña',   5, 200),
    ('a0000000-0000-0000-0000-000000000004', 'javier.moreno@daypilot.test',   'Javier Moreno',   'javiermoreno',    'javiermoreno',    'Andalucía',  1, 40),
    ('a0000000-0000-0000-0000-000000000005', 'lucia.fernandez@daypilot.test', 'Lucía Fernández', 'luciafernandez',  'luciafernandez',  'Galicia',    7, 310)
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- HISTORIAL DIARIO (genera las rachas automáticamente
-- a través de trg_update_streak — no se toca user_streaks a mano)
-- =============================================

-- Ana: 14 días consecutivos, racha larga y constante
INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_points, steps_points, wellness_points, timer_points, tech_health_points,
       tasks_points + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000001'::uuid AS user_id,
        d::date AS date,
        (4500 + (random() * 3500))::int AS steps,
        6000 AS steps_goal,
        (2 + (random() * 3))::int AS tasks_completed,
        (20 + (random() * 15))::int AS tasks_points,
        (10 + (random() * 10))::int AS steps_points,
        5 AS wellness_points, 5 AS timer_points, 5 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 14, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

-- Carlos: 5 días consecutivos, racha corta y reciente
INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_points, steps_points, wellness_points, timer_points, tech_health_points,
       tasks_points + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000002'::uuid AS user_id,
        d::date AS date,
        (3000 + (random() * 3000))::int AS steps,
        5000 AS steps_goal,
        (1 + (random() * 2))::int AS tasks_completed,
        (10 + (random() * 10))::int AS tasks_points,
        (5 + (random() * 10))::int AS steps_points,
        0 AS wellness_points, 5 AS timer_points, 0 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 5, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

-- María: dos tramos con un hueco en medio (racha rota una vez:
-- longest_streak quedará en 5, current_streak en 4)
INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_points, steps_points, wellness_points, timer_points, tech_health_points,
       tasks_points + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000003'::uuid AS user_id,
        d::date AS date,
        (6000 + (random() * 4000))::int AS steps,
        8000 AS steps_goal,
        (3 + (random() * 3))::int AS tasks_completed,
        (25 + (random() * 15))::int AS tasks_points,
        (15 + (random() * 10))::int AS steps_points,
        5 AS wellness_points, 5 AS timer_points, 5 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 10, CURRENT_DATE - 6, INTERVAL '1 day') AS d
    UNION ALL
    SELECT
        'a0000000-0000-0000-0000-000000000003'::uuid,
        d::date,
        (6000 + (random() * 4000))::int,
        8000,
        (3 + (random() * 3))::int,
        (25 + (random() * 15))::int,
        (15 + (random() * 10))::int,
        5, 5, 5
    FROM generate_series(CURRENT_DATE - 4, CURRENT_DATE - 1, INTERVAL '1 day') AS d
    -- (CURRENT_DATE - 5 queda sin fila a propósito: ahí se rompe la racha)
) sub
ON CONFLICT (user_id, date) DO NOTHING;

-- Javier: solo 3 días, racha corta (perfil de usuario poco constante)
INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_points, steps_points, wellness_points, timer_points, tech_health_points,
       tasks_points + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000004'::uuid AS user_id,
        d::date AS date,
        (1500 + (random() * 2000))::int AS steps,
        4000 AS steps_goal,
        (0 + (random() * 2))::int AS tasks_completed,
        (0 + (random() * 10))::int AS tasks_points,
        (0 + (random() * 5))::int AS steps_points,
        0 AS wellness_points, 0 AS timer_points, 0 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 3, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;

-- Lucía: 20 días consecutivos, la racha más larga del grupo
INSERT INTO user_daily_log (user_id, date, steps, steps_goal, tasks_completed, tasks_points, steps_points, wellness_points, timer_points, tech_health_points, total_points)
SELECT user_id, date, steps, steps_goal, tasks_completed,
       tasks_points, steps_points, wellness_points, timer_points, tech_health_points,
       tasks_points + steps_points + wellness_points + timer_points + tech_health_points
FROM (
    SELECT
        'a0000000-0000-0000-0000-000000000005'::uuid AS user_id,
        d::date AS date,
        (7000 + (random() * 5000))::int AS steps,
        10000 AS steps_goal,
        (3 + (random() * 4))::int AS tasks_completed,
        (30 + (random() * 20))::int AS tasks_points,
        (20 + (random() * 15))::int AS steps_points,
        5 AS wellness_points, 5 AS timer_points, 5 AS tech_health_points
    FROM generate_series(CURRENT_DATE - 20, CURRENT_DATE - 1, INTERVAL '1 day') AS d
) sub
ON CONFLICT (user_id, date) DO NOTHING;


-- =============================================
-- SOCIAL
-- =============================================

-- Lucía le ha mandado una solicitud de amistad a Ana, aún pendiente
INSERT INTO friend_requests (from_user_id, to_user_id) VALUES
    ('a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001')
ON CONFLICT (from_user_id, to_user_id) DO NOTHING;

-- Ana ya es amiga de Carlos, María y Javier
INSERT INTO friends (requester_id, receiver_id) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'),
    ('a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004')
ON CONFLICT (requester_id, receiver_id) DO NOTHING;


-- =============================================
-- ACTIVIDAD DE HOY (en vivo, para probar el pipeline
-- completo de triggers sobre el usuario de prueba principal)
-- =============================================

-- Hábitos de hoy de Ana (dispara trg_sync_habits_to_progress)
INSERT INTO habits_daily (user_id, date, steps, steps_goal, steps_finalized, timer_point_earned, tech_health_point_earned)
VALUES ('a0000000-0000-0000-0000-000000000001', CURRENT_DATE, 3200, 6000, false, true, false)
ON CONFLICT (user_id, date) DO UPDATE SET
    steps = EXCLUDED.steps, steps_goal = EXCLUDED.steps_goal,
    timer_point_earned = EXCLUDED.timer_point_earned;

-- Tareas de Ana: pendientes en el calendario + completadas en el pasado
INSERT INTO tasks (user_id, title, description, category, difficulty, estimated_minutes, is_completed, completed_at) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Comprar fruta',                  NULL, 'Compra',   'EASY',   15, false, NULL),
    ('a0000000-0000-0000-0000-000000000001', 'Repasar apuntes de BD',          NULL, 'Estudio',  'HARD',   90, false, NULL),
    ('a0000000-0000-0000-0000-000000000001', 'Llamar al dentista',             NULL, 'Salud',    'MEDIUM', 10, false, NULL),
    ('a0000000-0000-0000-0000-000000000001', 'Entregar práctica de Kotlin',    NULL, 'Estudio',  'HARD',  120, true,  now() - INTERVAL '2 days'),
    ('a0000000-0000-0000-0000-000000000001', 'Pagar el alquiler',              NULL, 'Finanzas', 'EASY',   10, true,  now() - INTERVAL '1 day');

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

-- Tarea creada y completada en el momento, para probar
-- trg_task_completed_to_progress de extremo a extremo
INSERT INTO tasks (user_id, title, category, difficulty, estimated_minutes, is_completed)
VALUES ('a0000000-0000-0000-0000-000000000001', 'Estirar 10 minutos', 'Bienestar', 'EASY', 10, false);

INSERT INTO task_days (task_id, user_id, date)
SELECT id, user_id, CURRENT_DATE FROM tasks
WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND title = 'Estirar 10 minutos';

UPDATE tasks SET is_completed = true, completed_at = now()
WHERE user_id = 'a0000000-0000-0000-0000-000000000001' AND title = 'Estirar 10 minutos';

-- Eventos de puntos de hoy (dispara trg_sync_points_to_progress,
-- que a su vez actualiza daily_progress y total_points_historical)
INSERT INTO points_log (user_id, points, source, day_key) VALUES
    ('a0000000-0000-0000-0000-000000000001', 15, 'STEPS',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001', 20, 'TASKS',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001', 10, 'TIMER',       CURRENT_DATE),
    ('a0000000-0000-0000-0000-000000000001',  5, 'WELLNESS',    CURRENT_DATE);


-- =============================================
-- RESUMEN SEMANAL Y REACCIONES
-- =============================================

-- Genera el resumen de los últimos 7 días cerrados para cada usuario
-- a partir del historial insertado arriba
SELECT fn_generate_weekly_summary();

-- Ana reacciona al resumen semanal de María
INSERT INTO reactions (from_user_id, to_user_id, weekly_summary_id, type)
SELECT 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', id, 'fire'
FROM user_weekly_summary
WHERE user_id = 'a0000000-0000-0000-0000-000000000003'
ORDER BY week_start DESC LIMIT 1
ON CONFLICT (from_user_id, to_user_id, weekly_summary_id) DO NOTHING;

-- Carlos y Lucía reaccionan al resumen semanal de Ana
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
