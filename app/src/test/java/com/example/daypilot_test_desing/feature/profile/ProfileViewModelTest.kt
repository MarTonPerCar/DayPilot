package com.example.daypilot_test_desing.feature.profile

import com.example.daypilot_test_desing.core.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.core.data.model.WeeklySummaryData
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepo: UserRepository
    private lateinit var progressRepo: ProgressRepository

    private val user = UserProfile(id = "u1", name = "Ana", username = "ana", email = "ana@daypilot.test", level = 3, totalPoints = 340)

    @Before
    fun setUp() {
        userRepo = mockk()
        progressRepo = mockk()
        coEvery { userRepo.getCurrentUser() } returns user
        coEvery { userRepo.getWeeklySummary() } returns WeeklySummaryData(200, 12, 30_000, 5)
        coEvery { progressRepo.getTodayProgress() } returns DailyProgressDto(userId = "u1", date = "2026-07-24", totalPoints = 60)
        coEvery { progressRepo.getRankingPosition() } returns 2
    }

    private fun buildViewModel() = ProfileViewModel(userRepo, progressRepo)

    @Test
    fun `init combines user, weekly summary and today progress`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.name)
        assertEquals(3, state.level)
        assertEquals(340, state.totalPoints)
        assertEquals(2, state.rankingPosition)
        assertEquals(60, state.pointsToday)
        assertEquals(200, state.weeklySummary.totalPoints)
    }

    @Test
    fun `updateProfile success reloads and reports success`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()
        coEvery { userRepo.updateProfile("Ana G", "anag", TimeZoneRegion.EUROPE_MADRID) } returns Unit

        viewModel.updateProfile("Ana G", "anag", TimeZoneRegion.EUROPE_MADRID)
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSavingProfile)
        assertTrue(state.profileSaveSuccess)
    }

    @Test
    fun `updateProfile failure reports the error without crashing`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()
        coEvery {
            userRepo.updateProfile("Ana G", "anag", TimeZoneRegion.EUROPE_MADRID)
        } throws RuntimeException("update failed")

        viewModel.updateProfile("Ana G", "anag", TimeZoneRegion.EUROPE_MADRID)
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSavingProfile)
        assertTrue(state.profileSaveError)
    }
}
