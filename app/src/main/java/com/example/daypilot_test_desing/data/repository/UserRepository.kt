package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.data.model.UserProfile
import com.example.daypilot_test_desing.data.model.WeeklySummaryData

interface UserRepository {
    fun getCurrentUser(): UserProfile
    fun getWeeklySummary(): WeeklySummaryData
    fun updateProfile(name: String, username: String, region: TimeZoneRegion)
}
