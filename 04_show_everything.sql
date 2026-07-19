SELECT
    u.id,
    u.email,
    u.name,
    u.username,
    u.region,
    u.level,
    u.total_points_historical,
    u.points_to_next_level,
    u.pending_steps_goal,
    u.pending_steps_goal_date,
    u.created_at,

    us.current_streak,
    us.longest_streak,
    us.last_active_date,

    dp.date              AS today_progress_date,
    dp.steps              AS today_steps,
    dp.tasks_completed     AS today_tasks_completed,
    dp.tasks_points        AS today_tasks_points,
    dp.steps_points        AS today_steps_points,
    dp.wellness_points     AS today_wellness_points,
    dp.timer_points        AS today_timer_points,
    dp.tech_health_points  AS today_tech_health_points,
    dp.total_points        AS today_total_points,

    uws.week_start,
    uws.total_steps           AS week_total_steps,
    uws.total_tasks_completed AS week_total_tasks_completed,
    uws.total_points          AS week_total_points,
    uws.best_streak           AS week_best_streak,

    tasks.tasks,
    habits.habits_daily,
    logs.user_daily_log,
    thc.tech_health_apps,
    thg.tech_health_groups,
    pl.points_log,
    fr_sent.friend_requests_sent,
    fr_recv.friend_requests_received,
    fr.friends,
    react_sent.reactions_sent,
    react_recv.reactions_received,
    notif.notifications

FROM users u

LEFT JOIN user_streaks us      ON us.user_id = u.id
LEFT JOIN daily_progress dp    ON dp.user_id = u.id
LEFT JOIN user_weekly_summary uws ON uws.user_id = u.id

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'title', t.title,
            'description', t.description,
            'category', t.category,
            'difficulty', t.difficulty,
            'estimated_minutes', t.estimated_minutes,
            'reminder_enabled', t.reminder_enabled,
            'is_recurring', t.is_recurring,
            'created_at', t.created_at,
            'occurrences', (
                SELECT json_agg(
                    jsonb_build_object(
                        'date', td.date,
                        'is_completed', td.is_completed,
                        'completed_at', td.completed_at,
                        'is_earned', td.is_earned
                    ) ORDER BY td.date
                )
                FROM task_days td WHERE td.task_id = t.id
            )
        ) ORDER BY t.created_at
    ) AS tasks
    FROM tasks t WHERE t.user_id = u.id
) tasks ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'date', hd.date,
            'steps', hd.steps,
            'steps_goal', hd.steps_goal,
            'timer_point_earned', hd.timer_point_earned,
            'updated_at', hd.updated_at
        ) ORDER BY hd.date DESC
    ) AS habits_daily
    FROM habits_daily hd WHERE hd.user_id = u.id
) habits ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'date', l.date,
            'steps', l.steps,
            'steps_goal', l.steps_goal,
            'tasks_completed', l.tasks_completed,
            'tasks_points', l.tasks_points,
            'steps_points', l.steps_points,
            'wellness_points', l.wellness_points,
            'timer_points', l.timer_points,
            'tech_health_points', l.tech_health_points,
            'total_points', l.total_points,
            'streak_at_day', l.streak_at_day
        ) ORDER BY l.date DESC
    ) AS user_daily_log
    FROM user_daily_log l WHERE l.user_id = u.id
) logs ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'app_name', c.app_name,
            'app_package', c.app_package,
            'limit_hours', c.limit_hours,
            'is_active', c.is_active,
            'pending_active', c.pending_active,
            'pending_limit_hours', c.pending_limit_hours,
            'is_violated_today', c.is_violated_today,
            'pending_delete', c.pending_delete
        )
    ) AS tech_health_apps
    FROM tech_health_config c WHERE c.user_id = u.id
) thc ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'group_name', g.group_name,
            'limit_hours', g.limit_hours,
            'is_active', g.is_active,
            'pending_active', g.pending_active,
            'pending_limit_hours', g.pending_limit_hours,
            'is_violated_today', g.is_violated_today,
            'pending_delete', g.pending_delete,
            'apps', (
                SELECT json_agg(jsonb_build_object('app_name', a.app_name, 'app_package', a.app_package))
                FROM tech_health_group_apps a WHERE a.group_id = g.id
            )
        )
    ) AS tech_health_groups
    FROM tech_health_group_config g WHERE g.user_id = u.id
) thg ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'points', p.points,
            'source', p.source,
            'day_key', p.day_key,
            'created_at', p.created_at
        ) ORDER BY p.created_at DESC
    ) AS points_log
    FROM points_log p WHERE p.user_id = u.id
) pl ON true

LEFT JOIN LATERAL (
    SELECT json_agg(jsonb_build_object('to_user', ru.username, 'created_at', r.created_at)) AS friend_requests_sent
    FROM friend_requests r
    JOIN users ru ON ru.id = r.to_user_id
    WHERE r.from_user_id = u.id
) fr_sent ON true

LEFT JOIN LATERAL (
    SELECT json_agg(jsonb_build_object('from_user', fu.username, 'created_at', r.created_at)) AS friend_requests_received
    FROM friend_requests r
    JOIN users fu ON fu.id = r.from_user_id
    WHERE r.to_user_id = u.id
) fr_recv ON true

LEFT JOIN LATERAL (
    SELECT json_agg(jsonb_build_object('friend', fuser.username, 'since', f.created_at)) AS friends
    FROM friends f
    JOIN users fuser ON fuser.id = CASE WHEN f.requester_id = u.id THEN f.receiver_id ELSE f.requester_id END
    WHERE f.requester_id = u.id OR f.receiver_id = u.id
) fr ON true

LEFT JOIN LATERAL (
    SELECT json_agg(jsonb_build_object('to_user', tu.username, 'type', r.type, 'created_at', r.created_at)) AS reactions_sent
    FROM reactions r
    JOIN users tu ON tu.id = r.to_user_id
    WHERE r.from_user_id = u.id
) react_sent ON true

LEFT JOIN LATERAL (
    SELECT json_agg(jsonb_build_object('from_user', fu.username, 'type', r.type, 'created_at', r.created_at)) AS reactions_received
    FROM reactions r
    JOIN users fu ON fu.id = r.from_user_id
    WHERE r.to_user_id = u.id
) react_recv ON true

LEFT JOIN LATERAL (
    SELECT json_agg(
        jsonb_build_object(
            'type', n.type, 'title', n.title, 'body', n.body,
            'is_read', n.is_read, 'created_at', n.created_at
        ) ORDER BY n.created_at DESC
    ) AS notifications
    FROM notifications n WHERE n.user_id = u.id
) notif ON true

ORDER BY u.username;
