-- =============================================
-- DAYPILOT — BORRADO COMPLETO
-- =============================================

SET session_replication_role = replica;

-- Tareas programadas (pg_cron) — se desprograman antes de borrar las
-- funciones que invocan, así una re-ejecución no deja jobs duplicados
DO $$
BEGIN
    PERFORM cron.unschedule('close-daily-progress');
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

DO $$
BEGIN
    PERFORM cron.unschedule('generate-weekly-summary');
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

-- Notificaciones
DROP TABLE IF EXISTS notifications CASCADE;

-- Reacciones
DROP TABLE IF EXISTS reactions CASCADE;

-- Solicitudes y amigos
DROP TABLE IF EXISTS friend_requests CASCADE;
DROP TABLE IF EXISTS friends CASCADE;

-- Resúmenes y progreso
DROP TABLE IF EXISTS user_weekly_summary CASCADE;
DROP TABLE IF EXISTS user_daily_log CASCADE;
DROP TABLE IF EXISTS daily_progress CASCADE;
DROP TABLE IF EXISTS points_log CASCADE;

-- Hábitos
DROP TABLE IF EXISTS habits_daily CASCADE;

-- Tareas
DROP TABLE IF EXISTS task_days CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;

-- Rachas
DROP TABLE IF EXISTS user_streaks CASCADE;

-- Usuarios
DROP TABLE IF EXISTS users CASCADE;

-- Vistas
DROP VIEW IF EXISTS friends_ranking CASCADE;
DROP VIEW IF EXISTS daily_summary CASCADE;
DROP VIEW IF EXISTS calendar_tasks CASCADE;

-- Funciones y triggers (los triggers ya se eliminan en cascada al borrar
-- las tablas; esto limpia los objetos de función en sí)
DROP FUNCTION IF EXISTS fn_sync_habits_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_task_completed_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_sync_points_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_update_level CASCADE;
DROP FUNCTION IF EXISTS fn_update_streak CASCADE;
DROP FUNCTION IF EXISTS fn_limit_daily_log CASCADE;
DROP FUNCTION IF EXISTS fn_cleanup_completed_tasks CASCADE;
DROP FUNCTION IF EXISTS fn_cleanup_points_log CASCADE;
DROP FUNCTION IF EXISTS fn_close_daily_progress CASCADE;
DROP FUNCTION IF EXISTS fn_generate_weekly_summary CASCADE;

SET session_replication_role = DEFAULT;
