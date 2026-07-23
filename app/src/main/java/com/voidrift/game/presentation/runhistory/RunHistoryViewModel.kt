package com.voidrift.game.presentation.runhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.domain.usecase.GetRunHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    getRunHistory: GetRunHistoryUseCase
) : ViewModel() {

    val runs: StateFlow<List<RunRecord>> = getRunHistory(limit = 50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val deepestRun: StateFlow<RunRecord?> = getRunHistory.deepestRun()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
