package com.example.daypilot.main.mainZone.habits.tech

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TechHealthViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TechHealthViewModel(appContext) as T
    }
}