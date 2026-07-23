package com.voidrift.game.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidrift.game.data.local.entity.PlayerProfile
import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.domain.repository.IPlayerRepository
import com.voidrift.game.domain.repository.IRunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeState(
    val profile: PlayerProfile? = null,
    val bestRun: RunRecord? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    playerRepo: IPlayerRepository,
    runRepo: IRunRepository
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        playerRepo.getProfile(),
        runRepo.getDeepestRun()
    ) { profile, bestRun ->
        HomeState(profile = profile, bestRun = bestRun)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())
}
