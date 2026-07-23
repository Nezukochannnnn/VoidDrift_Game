package com.voiddrift.game.domain.model

/**
 * Axis-Aligned Bounding Box used for physics collision resolution.
 * @param centerX  World-space X of the AABB center.
 * @param centerY  World-space Y of the AABB center.
 * @param halfW    Half-width.
 * @param halfH    Half-height.
 */
data class AABB(
    val centerX: Float,
    val centerY: Float,
    val halfW: Float,
    val halfH: Float
) {
    val left:   Float get() = centerX - halfW
    val right:  Float get() = centerX + halfW
    val top:    Float get() = centerY - halfH
    val bottom: Float get() = centerY + halfH

    fun overlaps(other: AABB): Boolean =
        left < other.right && right > other.left &&
        top  < other.bottom && bottom > other.top

    fun translate(dx: Float, dy: Float): AABB =
        copy(centerX = centerX + dx, centerY = centerY + dy)
}
