package com.example.daypilot_test_desing.feature.notifications

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.core.data.repository.NotificationRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
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
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: NotificationRepository

    private val item1 = NotificationData(id = "n1", title = "T1", message = "M1", timeAgo = "1h", type = NotificationType.SOCIAL, isRead = false)
    private val item2 = NotificationData(id = "n2", title = "T2", message = "M2", timeAgo = "2h", type = NotificationType.TASK, isRead = false)

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
        NotificationHub.init(ApplicationProvider.getApplicationContext())
        NotificationHub.clear()
        repo = mockk()
    }

    private fun buildViewModel() = NotificationsViewModel(ApplicationProvider.getApplicationContext(), repo)

    @Test
    fun `awaitLoad merges the server's notifications into the shared hub`() = runTest {
        coEvery { repo.getCurrentUserId() } returns "u1"
        coEvery { repo.getAll("u1") } returns listOf(item1, item2)
        val viewModel = buildViewModel()

        // Deliberately not advancing the dispatcher past this direct suspend call: doing so
        // would also let subscribeToRealtime()'s nested launch run, which opens a real
        // Supabase realtime socket — reading NotificationHub's own state directly instead
        // proves the merge happened without needing that risk.
        val result = viewModel.awaitLoad()

        assertTrue(result)
        assertEquals(listOf(item1, item2), NotificationHub.repo.notificationsFlow.value)
    }

    @Test
    fun `markAsRead flips only the matching notification, locally and server-side`() = runTest {
        NotificationHub.repo.mergeServerNotifications(listOf(item1, item2))
        coEvery { repo.markAsRead("n1") } returns Unit
        val viewModel = buildViewModel()

        viewModel.markAsRead("n1")
        realAdvanceUntilIdle()

        val stored = NotificationHub.repo.notificationsFlow.value
        assertTrue(stored.first { it.id == "n1" }.isRead)
        assertTrue(!stored.first { it.id == "n2" }.isRead)
        coVerify { repo.markAsRead("n1") }
    }

    @Test
    fun `markAllAsRead flips everything, locally and server-side`() = runTest {
        NotificationHub.repo.mergeServerNotifications(listOf(item1, item2))
        coEvery { repo.getCurrentUserId() } returns "u1"
        coEvery { repo.markAllAsRead("u1") } returns Unit
        val viewModel = buildViewModel()

        viewModel.markAllAsRead()
        realAdvanceUntilIdle()

        assertTrue(NotificationHub.repo.notificationsFlow.value.all { it.isRead })
        coVerify { repo.markAllAsRead("u1") }
    }
}
