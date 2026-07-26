package com.example.daypilot_test_desing.feature.session

import com.example.daypilot_test_desing.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// AppSessionViewModel has no injected repository at all — it drives the real Supabase Auth
// plugin directly. There's nothing to mock here, so this covers the manual state-machine
// transitions the rest of the app relies on (login/register success, retry-after-failure,
// startup data finishing) instead.
//
// Not covered: init{}'s own session-restore check (supabase.auth.awaitInitialization() ->
// Unauthenticated/DataLoading). The MainDispatcherRule's StandardTestDispatcher never runs that
// coroutine unless something advances it, which these tests don't need to do.
class AppSessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `notifyAuthenticated moves to DataLoading`() {
        val viewModel = AppSessionViewModel()

        viewModel.notifyAuthenticated()

        assertEquals(AppSessionViewModel.State.DataLoading, viewModel.state.value)
    }

    @Test
    fun `markDataLoaded moves DataLoading to Authenticated`() {
        val viewModel = AppSessionViewModel()

        viewModel.notifyAuthenticated()
        viewModel.markDataLoaded()

        assertEquals(AppSessionViewModel.State.Authenticated, viewModel.state.value)
    }

    @Test
    fun `a failed startup load can be retried back into DataLoading`() {
        val viewModel = AppSessionViewModel()

        viewModel.markDataLoadFailed()
        assertEquals(AppSessionViewModel.State.DataLoadFailed, viewModel.state.value)

        viewModel.retryDataLoad()
        assertEquals(AppSessionViewModel.State.DataLoading, viewModel.state.value)
    }
}
