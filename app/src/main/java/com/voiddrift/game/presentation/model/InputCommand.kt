package com.voiddrift.game.presentation.model

import com.voiddrift.game.domain.model.Vector2D

sealed class InputCommand {
    data class Move(val delta: Vector2D) : InputCommand()
    data class Thrust(val direction: Vector2D) : InputCommand()
    data object FireWeapon : InputCommand()
    data object ActivatePowerUp : InputCommand()
    data object PauseGame : InputCommand()
    data object ResumeGame : InputCommand()
    data object RestartGame : InputCommand()
}
