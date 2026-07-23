package com.voidrift.game.core.math

import kotlin.math.abs
import kotlin.math.min

/**
 * Axis-Aligned Bounding Box.
 * All coordinates in world-space pixels.
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
    val width:  Float get() = halfW * 2f
    val height: Float get() = halfH * 2f

    /** True when this AABB intersects [other]. */
    fun overlaps(other: AABB): Boolean =
        left < other.right && right > other.left &&
        top  < other.bottom && bottom > other.top

    /** Returns the minimum translation vector to push [this] out of [other]. */
    fun mtv(other: AABB): Vector2D {
        val ox = min(right - other.left, other.right - left)
        val oy = min(bottom - other.top, other.bottom - top)
        return if (ox < oy) {
            if (centerX < other.centerX) Vector2D(-ox, 0f) else Vector2D(ox, 0f)
        } else {
            if (centerY < other.centerY) Vector2D(0f, -oy) else Vector2D(0f, oy)
        }
    }

    fun translate(dx: Float, dy: Float): AABB =
        copy(centerX = centerX + dx, centerY = centerY + dy)

    fun moveTo(cx: Float, cy: Float): AABB = copy(centerX = cx, centerY = cy)

    fun contains(px: Float, py: Float): Boolean =
        px in left..right && py in top..bottom

    companion object {
        fun fromCenter(cx: Float, cy: Float, halfSize: Float) =
            AABB(cx, cy, halfSize, halfSize)
    }
}
