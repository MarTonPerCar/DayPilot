package com.example.daypilot.main.mainZone.habits.steps

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.ZoneId

class StepsViewModelFactory(
    private val appContext: Context,
    private val uid: String,
    private val zoneId: ZoneId
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StepsViewModel(appContext, uid, zoneId) as T
    }
}