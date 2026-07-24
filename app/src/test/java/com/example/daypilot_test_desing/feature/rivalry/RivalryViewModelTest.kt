package com.example.daypilot_test_desing.feature.rivalry

import com.example.daypilot_test_desing.core.data.model.RankingData
import com.example.daypilot_test_desing.core.data.repository.RankingRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RivalryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: RankingRepository

    private val me = RankingData(id = "u1", name = "Ana", points = 100, streak = 3, level = 2)
    private val other = RankingData(id = "u2", name = "Carlos", points = 150, streak = 5, level = 3)

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
        repo = mockk()
        coEvery { repo.getCurrentUserId() } returns "u1"
    }

    private fun buildViewModel() = RivalryViewModel(repo)

    @Test
    fun `init computes the current user's position within the ranking`() = runTest {
        coEvery { repo.getRanking() } returns listOf(other, me)

        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.currentUserName)
        assertEquals(2, state.currentUserPosition)
        assertEquals(100, state.currentUserPoints)
    }

    @Test
    fun `when the current user is not in the ranking it falls back to getCurrentUserData`() = runTest {
        coEvery { repo.getRanking() } returns listOf(other)
        coEvery { repo.getCurrentUserData() } returns me

        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.currentUserName)
        assertEquals(100, state.currentUserPoints)
        assertEquals(2, state.currentUserPosition) // not found -> ranking.size + 1
    }

    @Test
    fun `awaitLoad returns false when the repository fails, without crashing`() = runTest {
        coEvery { repo.getRanking() } throws RuntimeException("fetch failed")

        val viewModel = buildViewModel()
        val result = viewModel.awaitLoad()

        assertFalse(result)
    }
}
