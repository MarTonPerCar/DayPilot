SET session_replication_role = replica;

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

DO $$
BEGIN
    PERFORM cron.unschedule('cleanup-completed-tasks');
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

DO $$
BEGIN
    PERFORM cron.unschedule('apply-pending-steps-goals');
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

DROP TABLE IF EXISTS notifications CASCADE;

DROP TABLE IF EXISTS reactions CASCADE;

DROP TABLE IF EXISTS friend_requests CASCADE;
DROP TABLE IF EXISTS friends CASCADE;

DROP TABLE IF EXISTS user_weekly_summary CASCADE;
DROP TABLE IF EXISTS user_daily_log CASCADE;
DROP TABLE IF EXISTS daily_progress CASCADE;
DROP TABLE IF EXISTS points_log CASCADE;

DROP TABLE IF EXISTS tech_health_group_apps CASCADE;
DROP TABLE IF EXISTS tech_health_group_config CASCADE;
DROP TABLE IF EXISTS tech_health_config CASCADE;

DROP TABLE IF EXISTS habits_daily CASCADE;

DROP TABLE IF EXISTS task_days CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;

DROP TABLE IF EXISTS user_streaks CASCADE;

DROP TABLE IF EXISTS users CASCADE;

DROP VIEW IF EXISTS friends_ranking CASCADE;
DROP VIEW IF EXISTS calendar_tasks CASCADE;

DROP FUNCTION IF EXISTS fn_sync_habits_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_task_completed_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_sync_points_to_progress CASCADE;
DROP FUNCTION IF EXISTS fn_update_level CASCADE;
DROP FUNCTION IF EXISTS fn_update_streak CASCADE;
DROP FUNCTION IF EXISTS fn_limit_daily_log CASCADE;
DROP FUNCTION IF EXISTS fn_seed_daily_progress CASCADE;
DROP FUNCTION IF EXISTS fn_cleanup_completed_tasks CASCADE;
DROP FUNCTION IF EXISTS fn_cleanup_points_log CASCADE;
DROP FUNCTION IF EXISTS fn_close_daily_progress CASCADE;
DROP FUNCTION IF EXISTS fn_generate_weekly_summary CASCADE;
DROP FUNCTION IF EXISTS fn_apply_pending_steps_goals CASCADE;

SET session_replication_role = DEFAULT;
