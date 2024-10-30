package com.alaje.gendright.ui.settings

import androidx.lifecycle.ViewModel
import com.alaje.gendright.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsViewModel : ViewModel() {
    private val localDataSource = AppContainer.instance?.localDataSource

    private val autoSelectMostRelevant = MutableStateFlow(
        localDataSource?.checkHasAutoSelectMostRelevant() ?: false
    )
    val autoSelectMostRelevantState = autoSelectMostRelevant


    fun updateAutoSelectSettingsState(autoSelectMostRelevant: Boolean) {
        this.autoSelectMostRelevant.value = autoSelectMostRelevant
        localDataSource?.setAutoSelectMostRelevant(autoSelectMostRelevant)
    }

}