package com.example.daypilot_test_desing.core.data.local

import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.support.FakeContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalNotificationRepositoryTest {

    private lateinit var context: FakeContext

    @Before
    fun setUp() {
        context = FakeContext()
    }

    private fun notif(id: String, isRead: Boolean = false) =
        NotificationData(id = id, title = "T", message = "M", timeAgo = "1h", type = NotificationType.SOCIAL, isRead = isRead)

    @Test
    fun `notificationsFlow starts empty when nothing was stored`() {
        assertTrue(LocalNotificationRepository(context).notificationsFlow.value.isEmpty())
    }

    @Test
    fun `add prepends the notification and persists across instances`() {
        val repo = LocalNotificationRepository(context)

        repo.add(notif("n1"))

        assertEquals(listOf("n1"), repo.notificationsFlow.value.map { it.id })
        assertEquals(listOf("n1"), LocalNotificationRepository(context).notificationsFlow.value.map { it.id })
    }

    @Test
    fun `add caps the stored list at 50, dropping the oldest`() {
        val repo = LocalNotificationRepository(context)
        repeat(51) { i -> repo.add(notif("n$i")) }

        val stored = repo.notificationsFlow.value
        assertEquals(50, stored.size)
        assertEquals("n50", stored.first().id) // most recent add, prepended
        assertTrue(stored.none { it.id == "n0" }) // oldest, pushed out
    }

    @Test
    fun `markAsRead flips only the matching notification`() {
        val repo = LocalNotificationRepository(context)
        repo.add(notif("n2"))
        repo.add(notif("n1"))

        repo.markAsRead("n1")

        val byId = repo.notificationsFlow.value.associateBy { it.id }
        assertEquals(true, byId.getValue("n1").isRead)
        assertEquals(false, byId.getValue("n2").isRead)
    }

    @Test
    fun `markAllAsRead flips every notification`() {
        val repo = LocalNotificationRepository(context)
        repo.add(notif("n1"))
        repo.add(notif("n2"))

        repo.markAllAsRead()

        assertTrue(repo.notificationsFlow.value.all { it.isRead })
    }

    @Test
    fun `mergeServerNotifications replaces local state entirely`() {
        val repo = LocalNotificationRepository(context)
        repo.add(notif("stale"))

        repo.mergeServerNotifications(listOf(notif("fresh")))

        assertEquals(listOf("fresh"), repo.notificationsFlow.value.map { it.id })
    }

    @Test
    fun `mergeServerNotifications caps at 50`() {
        val repo = LocalNotificationRepository(context)

        repo.mergeServerNotifications((0 until 60).map { notif("n$it") })

        assertEquals(50, repo.notificationsFlow.value.size)
    }

    @Test
    fun `clear empties the stored list and persists across instances`() {
        val repo = LocalNotificationRepository(context)
        repo.add(notif("n1"))

        repo.clear()

        assertTrue(repo.notificationsFlow.value.isEmpty())
        assertTrue(LocalNotificationRepository(context).notificationsFlow.value.isEmpty())
    }

    @Test
    fun `a corrupted stored payload is discarded instead of thrown`() {
        context.getSharedPreferences("daypilot_notifications", 0).edit().putString("notifications", "not json").apply()

        assertTrue(LocalNotificationRepository(context).notificationsFlow.value.isEmpty())
    }
}
