package com.voiddrift.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiddrift.game.domain.model.GameStatus
import com.voiddrift.game.domain.usecase.GetHighScoresUseCase
import com.voiddrift.game.domain.usecase.SaveHighScoreUseCase
import com.voiddrift.game.presentation.engine.GameLoopEngine
import com.voiddrift.game.presentation.model.GameUiState
import com.voiddrift.game.presentation.model.InputCommand
import com.voiddrift.game.presentation.shader.AgslShaderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameLoopEngine: GameLoopEngine,
    private val saveHighScoreUseCase: SaveHighScoreUseCase,
    private val getHighScoresUseCase: GetHighScoresUseCase,
    val agslShaderManager: AgslShaderManager
) : ViewModel() {

    val uiState: StateFlow<GameUiState> = combine(
        gameLoopEngine.gameState,
        getHighScoresUseCase.getTopScore()
    ) { gameState, topScore ->
        GameUiState(
            gameState = gameState,
            highScore = topScore,
            isShaderSupported = agslShaderManager.isAgslSupported,
            isGameOver = gameState.status == GameStatus.GAME_OVER
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameUiState()
    )

    fun startGame(worldWidth: Float, worldHeight: Float) {
        gameLoopEngine.startGame(worldWidth, worldHeight)
    }

    fun handleInput(command: InputCommand) {
        gameLoopEngine.processInput(command)
    }

    fun saveCurrentScore(playerName: String) {
        val currentScore = uiState.value.gameState.score
        val wave = uiState.value.gameState.waveNumber
        if (currentScore > 0) {
            viewModelScope.launch {
                saveHighScoreUseCase(playerName, currentScore, wave)
            }
        }
    }

    fun pauseGame() {
        gameLoopEngine.pauseGame()
    }

    fun resumeGame() {
        gameLoopEngine.resumeGame()
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopEngine.stopGame()
    }
}
