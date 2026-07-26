package com.example.daypilot_test_desing.feature.friends

import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.model.SearchUserData
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchFriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: FriendRepository

    private val existingFriend = SearchUserData(id = "u3", name = "Maria", email = "maria@daypilot.test", points = 80, streak = 5)
    private val strangerResult = SearchUserData(id = "u4", name = "Javier", email = "javier@daypilot.test", points = 10, streak = 0)

    @Before
    fun setUp() {
        repo = mockk()
        coEvery { repo.getPendingSentRequestUserIds() } returns emptyList()
        coEvery { repo.getFriendIds() } returns listOf("u3")
    }

    private fun buildViewModel() = SearchFriendsViewModel(repo)

    @Test
    fun `search filters out existing friends from the results`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        coEvery { repo.searchUsers("jav") } returns listOf(existingFriend, strangerResult)

        viewModel.search("jav")
        advanceUntilIdle()

        val results = viewModel.uiState.value.searchResults
        assertEquals(listOf(strangerResult.copy(hasPendingRequest = false)), results)
    }

    @Test
    fun `addFriend failure rolls back the optimistic sent-request state`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        coEvery { repo.searchUsers("jav") } returns listOf(strangerResult)
        viewModel.search("jav")
        advanceUntilIdle()

        coEvery { repo.addFriend("u4") } throws RuntimeException("send failed")

        viewModel.addFriend("u4")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.requestJustSent)
        assertFalse(state.sentRequestUserIds.contains("u4"))
        assertEquals(R.string.error_add_friend, state.userMessage)
    }

    @Test
    fun `init preloads pending sent requests so results show them as already pending`() = runTest {
        coEvery { repo.getPendingSentRequestUserIds() } returns listOf("u4")
        val viewModel = buildViewModel()
        advanceUntilIdle()
        coEvery { repo.searchUsers("jav") } returns listOf(strangerResult)

        viewModel.search("jav")
        advanceUntilIdle()

        val result = viewModel.uiState.value.searchResults.single()
        assertTrue(result.hasPendingRequest)
    }
}
