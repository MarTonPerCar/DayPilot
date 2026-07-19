package com.example.daypilot_test_desing.core.navigation

object DayPilotDestinations {
    const val LOADING = "loading"

    const val AUTH = "auth"

    const val HOME          = "home"
    const val FRIENDS       = "friends"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE       = "profile"

    const val SEARCH_FRIENDS = "search_friends"

    const val CALENDAR   = "calendar"
    const val HABITS     = "habits"
    const val PROGRESS   = "progress"
    const val RIVALRY    = "rivalry"

    const val REMINDERS   = "reminders"
    const val TECH_HEALTH = "tech_health"
    const val TIMER_HUB = "timer_hub"
    const val POMODORO  = "pomodoro/{sessions}"
    const val TIMER     = "timer/{timerMode}/{minutes}"

    fun timerRoute(mode: String, minutes: Int) = "timer/$mode/$minutes"
    fun pomodoroRoute(sessions: Int)           = "pomodoro/$sessions"

    const val SETTINGS = "settings"
    const val EDIT_PROFILE    = "edit_profile"
    const val RESET_PASSWORD  = "reset_password"
}
