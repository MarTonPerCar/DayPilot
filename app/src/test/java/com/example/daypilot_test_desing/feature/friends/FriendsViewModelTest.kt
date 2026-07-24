package com.example.daypilot_test_desing.feature.friends

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: FriendRepository

    private val request = FriendData(id = "u2", name = "Carlos", email = "carlos@daypilot.test", points = 50, streak = 2)
    private val friend1 = FriendData(id = "u3", name = "Maria", email = "maria@daypilot.test", points = 80, streak = 5)

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
        NotificationHub.init(ApplicationProvider.getApplicationContext())
        NotificationHub.clear()
        repo = mockk()
        coEvery { repo.getFriends() } returns listOf(friend1)
        coEvery { repo.getFriendRequests() } returns listOf(request)
    }

    private fun buildViewModel() = FriendsViewModel(repo)

    @Test
    fun `init loads friends and friend requests`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(friend1), state.friends)
        assertEquals(listOf(request), state.friendRequests)
    }

    @Test
    fun `acceptRequest moves the request into friends optimistically then confirms`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { repo.acceptRequest("u2") } returns Unit
        coEvery { repo.getFriends() } returns listOf(friend1, request)
        coEvery { repo.getFriendRequests() } returns emptyList()

        viewModel.acceptRequest(request.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.justAcceptedRequest)
        assertTrue(state.friends.any { it.id == "u2" })
        assertTrue(state.friendRequests.isEmpty())
    }

    @Test
    fun `removeFriend failure rolls back the optimistic removal`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { repo.removeFriend("u3") } throws RuntimeException("remove failed")

        viewModel.removeFriend("u3")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(friend1), state.friends)
        assertEquals(R.string.error_remove_friend, state.userMessage)
    }
}
