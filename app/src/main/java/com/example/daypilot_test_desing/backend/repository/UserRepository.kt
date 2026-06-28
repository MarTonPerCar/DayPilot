package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.model.UserProfile
import com.example.daypilot_test_desing.backend.model.WeeklySummaryData

interface UserRepository {
    suspend fun getCurrentUser(): UserProfile
    suspend fun getWeeklySummary(): WeeklySummaryData
    suspend fun updateProfile(name: String, username: String, region: TimeZoneRegion)
}
