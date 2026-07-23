package com.voiddrift.game.domain.model

enum class ItemType {
    TORCH_REFILL,       // Restores torch radius by +80f
    HEALTH_POTION,      // Restores +30 HP
    CRYSTAL_SHARD,      // Score multiplier pickup
    VOID_WARD,          // Slows nearby void corruption for 10s
    WEAPON_UPGRADE      // Increases attack range/damage
}

data class Item(
    val type: ItemType,
    val quantity: Int = 1
)
