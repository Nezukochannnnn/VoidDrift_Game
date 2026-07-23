package com.voiddrift.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiddrift.game.domain.model.ScoreRecord
import com.voiddrift.game.domain.usecase.GetHighScoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    getHighScoresUseCase: GetHighScoresUseCase
) : ViewModel() {

    val highScores: StateFlow<List<ScoreRecord>> = getHighScoresUseCase(limit = 20)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
