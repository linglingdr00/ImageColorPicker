package com.tinatang.imagecolorpicker.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

internal const val PI_F = PI.toFloat()
internal const val TO_RADIANS = PI_F / 180f
internal fun Float.toRadians() = this * TO_RADIANS

// These exist for Size but not IntSize
internal val IntSize.center get() = Offset(width * 0.5f, height * 0.5f)
internal operator fun IntSize.times(scale: Float) =
    IntSize((width * scale).roundToInt(), (height * scale).roundToInt())

internal val Offset.minCoordinate get() = min(x, y) // exists for Size but not Offset

internal fun Offset.roundToInt() = IntOffset(x.roundToInt(), y.roundToInt())

internal fun Offset.Companion.fromAngle(radians: Float, magnitude: Float) =
    Offset(cos(radians) * magnitude, sin(radians) * magnitude)