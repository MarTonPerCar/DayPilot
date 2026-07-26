package com.example.daypilot_test_desing.data.supabase

import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.support.SharedFakeSupabaseClient
import com.example.daypilot_test_desing.support.fakeLogin
import io.ktor.client.engine.mock.MockRequestHandleScope
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
private fun tomorrow(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, 1)
    return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(cal.time)
}
private fun yesterday(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, -1)
    return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(cal.time)
}

@RunWith(RobolectricTestRunner::class)
class SupabaseStepsRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() = runBlocking {
        initSupabaseSettingsForTest()
        shared.resetAuth()
        prefs = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("test_steps_${System.nanoTime()}", android.content.Context.MODE_PRIVATE)
    }

    @Test
    fun `getCurrentSteps and setSteps hold local state only`() {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(0, repo.getCurrentSteps())
        repo.setSteps(1234)
        assertEquals(1234, repo.getCurrentSteps())
    }

    @Test
    fun `getGoalSteps defaults to 10000 when unset`() {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(10_000, repo.getGoalSteps())
    }

    @Test
    fun `getGoalSteps applies a pending goal once its effective date has passed`() {
        prefs.edit()
            .putInt("steps_goal", 8_000)
            .putInt("pending_goal", 12_000)
            .putString("goal_change_date", yesterday())
            .apply()
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(12_000, repo.getGoalSteps())
        assertEquals(-1, prefs.getInt("pending_goal", -1))
        assertEquals("", prefs.getString("goal_change_date", ""))
    }

    @Test
    fun `getGoalSteps leaves a future-dated pending goal untouched`() {
        prefs.edit()
            .putInt("steps_goal", 8_000)
            .putInt("pending_goal", 12_000)
            .putString("goal_change_date", tomorrow())
            .apply()
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(8_000, repo.getGoalSteps())
        assertEquals(12_000, prefs.getInt("pending_goal", -1))
    }

    @Test
    fun `getPendingGoal returns null when unset, value when set`() {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        assertNull(repo.getPendingGoal())

        prefs.edit().putInt("pending_goal", 15_000).apply()
        assertEquals(15_000, repo.getPendingGoal())
    }

    @Test
    fun `canChangeGoal is always true`() {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        assertTrue(repo.canChangeGoal())
    }

    @Test
    fun `configureGoal writes the pending goal and tomorrow's effective date synchronously`() = runTest {
        val client = fakeSupabaseClient { request ->
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.configureGoal(9_500)

        assertEquals(9_500, prefs.getInt("pending_goal", -1))
        assertEquals(tomorrow(), prefs.getString("goal_change_date", null))
    }

    @Test
    fun `getPointsEarned returns 0 when nobody is logged in`() = runTest {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(0, repo.getPointsEarned())
    }

    @Test
    fun `getPointsEarned maps milestone level to the matching point value`() = runTest {
        val client = fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/habits_daily"))
            respond("""[{"steps_milestone_level":2}]""", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        assertEquals(30, repo.getPointsEarned())
    }

    @Test
    fun `getPointsEarned returns 0 when no row is found`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        assertEquals(0, repo.getPointsEarned())
    }

    @Test
    fun `getPointsEarned returns 0 when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        assertEquals(0, repo.getPointsEarned())
    }

    @Test
    fun `syncSteps upserts against the user_id,date conflict target`() = runTest {
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            captured = String(request.body.toByteArray())
            assertTrue(request.url.encodedPath.endsWith("/habits_daily"))
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.syncSteps(steps = 4200, goal = 8_000)

        assertTrue(captured!!.contains("\"steps\":4200"))
        assertTrue(captured.contains("\"steps_goal\":8000"))
        assertTrue(captured.contains("\"date\":\"${today()}\""))
    }

    @Test
    fun `syncSteps is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        repo.syncSteps(steps = 100, goal = 10_000)
    }

    @Test
    fun `hydrateGoalFromServer hydrates from the latest habits_daily row on first run`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/habits_daily") ->
                    respond("""[{"user_id":"u1","date":"${today()}","steps_goal":11000}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"pending_steps_goal":null,"pending_steps_goal_date":null}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.hydrateGoalFromServer()

        assertEquals(11_000, prefs.getInt("steps_goal", -1))
    }

    @Test
    fun `hydrateGoalFromServer defaults to 10000 when no row is found on first run`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.hydrateGoalFromServer()

        assertEquals(10_000, prefs.getInt("steps_goal", -1))
    }

    @Test
    fun `hydrateGoalFromServer skips the hydrate query once steps_goal already exists`() = runTest {
        prefs.edit().putInt("steps_goal", 7_777).apply()
        val client = fakeSupabaseClient { request ->
            assertFalse(request.url.encodedPath.endsWith("/habits_daily"))
            respond("""[{"pending_steps_goal":null,"pending_steps_goal_date":null}]""", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.hydrateGoalFromServer()

        assertEquals(7_777, prefs.getInt("steps_goal", -1))
    }

    @Test
    fun `hydrateGoalFromServer adopts a pending goal queued from another device`() = runTest {
        prefs.edit().putInt("steps_goal", 7_777).apply()
        val client = fakeSupabaseClient { request ->
            respond(
                """[{"pending_steps_goal":13000,"pending_steps_goal_date":"${tomorrow()}"}]""",
                headers = jsonHeaders
            )
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        repo.hydrateGoalFromServer()

        assertEquals(13_000, prefs.getInt("pending_goal", -1))
        assertEquals(tomorrow(), prefs.getString("goal_change_date", null))
    }

    @Test
    fun `getWeeklyStats returns defaults when nobody is logged in`() = runTest {
        val repo = SupabaseStepsRepository(prefs, fakeSupabaseClient { error("no HTTP expected") })
        val stats = repo.getWeeklyStats()
        assertEquals(0, stats.totalSteps7Days)
        assertEquals(0, stats.bestDaySteps)
        assertEquals(0, stats.dailyAverage)
        assertEquals(0, stats.goalStreak)
    }

    @Test
    fun `getWeeklyStats computes totals, best day, average and a goal-met streak`() = runTest {
        val client = fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/user_daily_log"))
            respond(
                """[
                    {"user_id":"u1","date":"d3","steps":12000,"steps_goal":10000,"tasks_completed":2,"total_points":5},
                    {"user_id":"u1","date":"d2","steps":9000,"steps_goal":10000,"tasks_completed":1,"total_points":3},
                    {"user_id":"u1","date":"d1","steps":11000,"steps_goal":10000,"tasks_completed":0,"total_points":1}
                ]""",
                headers = jsonHeaders
            )
        }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        val stats = repo.getWeeklyStats()

        assertEquals(32_000, stats.totalSteps7Days)
        assertEquals(12_000, stats.bestDaySteps)
        assertEquals(32_000 / 3, stats.dailyAverage)
        // Streak breaks at the first day (in server order) that misses its goal.
        assertEquals(1, stats.goalStreak)
    }

    @Test
    fun `getWeeklyStats returns defaults when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseStepsRepository(prefs, client)

        val stats = repo.getWeeklyStats()
        assertEquals(0, stats.totalSteps7Days)
    }
}
