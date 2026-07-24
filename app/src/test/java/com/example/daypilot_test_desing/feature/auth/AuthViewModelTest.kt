package com.example.daypilot_test_desing.feature.auth

import com.example.daypilot_test_desing.core.data.repository.AuthRepository
import com.example.daypilot_test_desing.core.data.repository.RegisterOutcome
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = AuthViewModel(repo)
    }

    @Test
    fun `login with valid credentials authenticates and calls onSuccess`() = runTest {
        coEvery { repo.login("ana@daypilot.test", "password123") } returns Unit
        var successCalled = false

        viewModel.login("ana@daypilot.test", "password123") { successCalled = true }
        realAdvanceUntilIdle()

        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.loginLoading)
        assertEquals("", viewModel.uiState.value.loginError)
    }

    @Test
    fun `login with a blank field fails fast without calling the repository`() = runTest {
        var successCalled = false

        viewModel.login("", "password123") { successCalled = true }
        realAdvanceUntilIdle()

        assertFalse(successCalled)
        assertEquals("Please enter your email and password.", viewModel.uiState.value.loginError)
        coVerify(exactly = 0) { repo.login(any(), any()) }
    }

    @Test
    fun `register that already exists reports the right error instead of succeeding`() = runTest {
        coEvery {
            repo.register("Ana", "ana", "ana@daypilot.test", "password123", "ES")
        } returns RegisterOutcome.AlreadyExists
        var successCalled = false

        viewModel.register("Ana", "ana", "ana@daypilot.test", "password123", "ES") { successCalled = true }
        realAdvanceUntilIdle()

        assertFalse(successCalled)
        assertFalse(viewModel.uiState.value.registerLoading)
        assertEquals("An account with this email already exists.", viewModel.uiState.value.registerError)
    }
}
