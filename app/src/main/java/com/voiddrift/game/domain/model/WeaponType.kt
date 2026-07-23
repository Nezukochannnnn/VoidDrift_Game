package com.voiddrift.game.domain.model

enum class WeaponType(
    val displayName: String,
    val fireRateMs: Long,
    val baseDamage: Float,
    val energyCost: Float
) {
    PLASMA_SINGLE(
        displayName = "Pulse Plasma",
        fireRateMs = 150L,
        baseDamage = 25f,
        energyCost = 2f
    ),
    SPREAD_SHOT(
        displayName = "Void Splitter",
        fireRateMs = 250L,
        baseDamage = 18f,
        energyCost = 5f
    ),
    LASER_BEAM(
        displayName = "Neutron Beam",
        fireRateMs = 80L,
        baseDamage = 12f,
        energyCost = 3f
    ),
    HOMING_MISSILE(
        displayName = "Vortex Torpedo",
        fireRateMs = 400L,
        baseDamage = 60f,
        energyCost = 15f
    )
}
