package com.tinatang.imagecolorpicker.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

internal val Color.hexCode: String
    inline get() {
        val a: Int = (alpha * 255).toInt()
        val r: Int = (red * 255).toInt()
        val g: Int = (green * 255).toInt()
        val b: Int = (blue * 255).toInt()
        return a.hex + r.hex + g.hex + b.hex
    }

private val Int.hex get() = this.toString(16).padStart(2, '0')

/** Converts an HS(V) color to a coordinate on the hue/saturation circle. */
internal fun hsvToCoord(h: Float, s: Float, center: Offset) =
    Offset.fromAngle(hueToAngle(h), s * center.minCoordinate) + center

internal fun hueToAngle(hue: Float) = -hue.toRadians()

internal fun Color.toHSV(): Triple<Float, Float, Float> {
    val cmax = maxOf(red, green, blue)
    val cmin = minOf(red, green, blue)
    val diff = cmax - cmin

    val h = if (diff == 0f) {
        0.0f
    } else if (cmax == red) {
        (60 * ((green - blue) / diff) + 360f) % 360f
    } else if (cmax == green) {
        (60 * ((blue - red) / diff) + 120f) % 360f
    } else {
        (60 * ((red - green) / diff) + 240f) % 360f // if (cmax == blue)
    }
    val s = if (cmax == 0f) 0f else diff / cmax
    val v = cmax

    return Triple(h, s, v)
}



