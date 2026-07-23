package com.voiddrift.game.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2D = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D(0f, 0f)

    fun magnitude(): Float = sqrt(x * x + y * y)

    fun normalized(): Vector2D {
        val mag = magnitude()
        return if (mag > 0.0001f) Vector2D(x / mag, y / mag) else Vector2D(0f, 0f)
    }

    fun distanceTo(other: Vector2D): Float = (this - other).magnitude()

    fun angle(): Float = atan2(y, x)

    fun rotate(angleDegrees: Float): Vector2D {
        val rad = Math.toRadians(angleDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        return Vector2D(
            x = x * cosA - y * sinA,
            y = x * sinA + y * cosA
        )
    }

    fun clampMagnitude(maxMag: Float): Vector2D {
        val mag = magnitude()
        return if (mag > maxMag && mag > 0f) {
            normalized() * maxMag
        } else {
            this
        }
    }

    companion object {
        val Zero = Vector2D(0f, 0f)
        val Up = Vector2D(0f, -1f)
        val Down = Vector2D(0f, 1f)
        val Left = Vector2D(-1f, 0f)
        val Right = Vector2D(1f, 0f)
    }
}
