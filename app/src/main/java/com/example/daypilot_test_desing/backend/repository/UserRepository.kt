package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.model.UserProfile
import com.example.daypilot_test_desing.backend.model.WeeklySummaryData

interface UserRepository {
    fun getCurrentUser(): UserProfile
    fun getWeeklySummary(): WeeklySummaryData
    fun updateProfile(name: String, username: String, region: TimeZoneRegion)
}
