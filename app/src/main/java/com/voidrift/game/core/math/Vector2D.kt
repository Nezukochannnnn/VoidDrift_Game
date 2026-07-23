package com.voidrift.game.core.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    operator fun plus(other: Vector2D)  = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float)   = Vector2D(x * scalar,  y * scalar)
    operator fun div(scalar: Float)     = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Zero
    operator fun unaryMinus()           = Vector2D(-x, -y)

    fun magnitude(): Float = sqrt(x * x + y * y)
    fun magnitudeSq(): Float = x * x + y * y

    fun normalized(): Vector2D {
        val m = magnitude()
        return if (m > 1e-5f) Vector2D(x / m, y / m) else Zero
    }

    fun distanceTo(other: Vector2D): Float = (this - other).magnitude()
    fun distanceSqTo(other: Vector2D): Float = (this - other).magnitudeSq()

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    fun angle(): Float = atan2(y, x)

    fun rotate(angleDeg: Float): Vector2D {
        val rad = Math.toRadians(angleDeg.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        return Vector2D(x * c - y * s, x * s + y * c)
    }

    fun clampMagnitude(maxMag: Float): Vector2D {
        val m = magnitude()
        return if (m > maxMag && m > 0f) normalized() * maxMag else this
    }

    fun lerp(target: Vector2D, t: Float): Vector2D {
        val ct = t.coerceIn(0f, 1f)
        return Vector2D(x + (target.x - x) * ct, y + (target.y - y) * ct)
    }

    companion object {
        val Zero  = Vector2D(0f,  0f)
        val One   = Vector2D(1f,  1f)
        val Up    = Vector2D(0f, -1f)
        val Down  = Vector2D(0f,  1f)
        val Left  = Vector2D(-1f, 0f)
        val Right = Vector2D(1f,  0f)
    }
}
