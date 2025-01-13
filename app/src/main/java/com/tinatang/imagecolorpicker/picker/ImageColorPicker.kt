package com.tinatang.imagecolorpicker.picker

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import com.tinatang.imagecolorpicker.ColorPickerController
import com.tinatang.imagecolorpicker.PaletteContentScale
import com.tinatang.imagecolorpicker.data.ColorEnvelope
import com.tinatang.imagecolorpicker.extension.emptyPaint
import com.tinatang.imagecolorpicker.extension.getPixel
import com.tinatang.imagecolorpicker.extension.roundToInt
import com.tinatang.imagecolorpicker.extension.scaledSize
import com.tinatang.imagecolorpicker.extension.size

private const val TAG = "ImageColorPicker"

@Composable
fun ImageColorPicker(
    modifier: Modifier,
    controller: ColorPickerController,
    paletteImageBitmap: ImageBitmap,
    wheelImageBitmap: ImageBitmap? = null,
    drawOnPosSelected: (DrawScope.() -> Unit)? = null,
    drawDefaultWheelIndicator: Boolean = wheelImageBitmap == null && drawOnPosSelected == null,
    paletteContentScale: PaletteContentScale = PaletteContentScale.FIT,
    previewImagePainter: Painter? = null,
    scaleImageSize: Int,
    onColorChanged: (colorEnvelope: ColorEnvelope) -> Unit = {},
    onPositionChanged: (position: Offset) -> Unit = {},
    onImageChanged: (image: ImageBitmap) -> Unit = {},
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current.density

    if (LocalInspectionMode.current && previewImagePainter != null) {
        Image(
            modifier = modifier.fillMaxSize(),
            painter = previewImagePainter,
            contentDescription = null,
        )
        return
    }

    val controllerBitmap: ImageBitmap? by controller.paletteBitmap.collectAsState(
        initial = paletteImageBitmap,
    )
    val imageBitmap = controllerBitmap ?: paletteImageBitmap

    val width = remember(imageBitmap) { imageBitmap.width }
    val height = remember(imageBitmap) { imageBitmap.height }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var selectedPosition by remember { mutableStateOf(Offset.Zero) }

    // 更新選擇的位置
    LaunchedEffect(selectedPosition) {
        // 更新圖片裁減區域
        val clipRect = updateScaleImgRect(
            selectedPosition = selectedPosition,
            scaleImageSize = scaleImageSize,
            density = density,
            scale = scale,
            offset = offset
        )
        // 裁減圖片
        val croppedBitmap = imageBitmap.toCroppedImage(clipRect)
        // 將裁減後的圖片傳回
        croppedBitmap?.let { onImageChanged(it.asImageBitmap()) }

        onPositionChanged(selectedPosition)
    }

    LaunchedEffect(key1 = imageBitmap) {
        controller.setup { point ->
            val origPoint = (point - offset) / scale
            val imPoint = Offset(
                origPoint.x.coerceIn(0f, width - 1f),
                origPoint.y.coerceIn(0f, height - 1f),
            )
            Log.d(TAG, "origPoint: $origPoint, imPoint: $imPoint")
            val px = imageBitmap.getPixel(imPoint.roundToInt())
            val newPoint = imPoint * scale + offset
            Log.d(TAG, "newPoint: $newPoint")
            selectedPosition = newPoint
            px to newPoint
        }
    }

    ColorPicker(
        modifier = modifier,
        controller = controller,
        wheelImageBitmap = wheelImageBitmap,
        drawOnPosSelected = drawOnPosSelected,
        drawDefaultWheelIndicator = drawDefaultWheelIndicator,
        onColorChanged = onColorChanged,
        sizeChanged = { size ->
            val metrics = when (paletteContentScale) {
                PaletteContentScale.FIT -> {
                    getMetricsForFit(imageBitmap.size, size)
                }

                PaletteContentScale.CROP -> {
                    getMetricsForCrop(imageBitmap.size, size)
                }
            }
            scale = metrics.first
            offset = metrics.second
        },
        setup = {
            controller.setup { point ->
                val origPoint = (point - offset) / scale
                val imPoint = Offset(
                    origPoint.x.coerceIn(0f, width - 1f),
                    origPoint.y.coerceIn(0f, height - 1f),
                )
                Log.d(TAG, "Color origPoint: $origPoint, imPoint: $imPoint")
                val px = imageBitmap.getPixel(imPoint.roundToInt())
                val newPoint = imPoint * scale + offset
                Log.d(TAG, "Color newPoint: $newPoint")
                //selectedPosition = newPoint
                px to newPoint
            }
        },
        draw = {
            drawImageRect(
                image = imageBitmap,
                dstOffset = offset.roundToInt(),
                dstSize = scaledSize(imageBitmap, scale),
                paint = emptyPaint,
            )
        },
        onDragEnd = onDragEnd
    )
}

fun updateScaleImgRect(
    selectedPosition: Offset,
    scaleImageSize: Int,
    density: Float,
    scale: Float,
    offset: Offset,
): Rect {
    // 計算 selected point 的位置
    val radius = scaleImageSize * density
    val centerX = selectedPosition.x / scale - offset.x / scale
    val centerY = selectedPosition.y / scale - offset.y / scale

    // 計算裁減區域，確保不會超過圖片邊界
    val left = (centerX - radius / 2)
    val top = (centerY - radius / 2)
    val right = (centerX + radius / 2)
    val bottom = (centerY + radius / 2)

    // 繪製裁減圖形
    val clipRect = Rect(
        left.toInt(),
        top.toInt(),
        right.toInt(),
        bottom.toInt()
    )
    return clipRect
}

private fun getMetricsForFit(imSize: IntSize, targetSize: IntSize) =
    if (imSize.width * targetSize.height > targetSize.width * imSize.height) {
        scaleToWidth(imSize, targetSize)
    } else {
        scaleToHeight(imSize, targetSize)
    }

private fun getMetricsForCrop(imSize: IntSize, targetSize: IntSize) =
    if (imSize.width * targetSize.height > targetSize.width * imSize.height) {
        scaleToHeight(imSize, targetSize)
    } else {
        scaleToWidth(imSize, targetSize)
    }

private fun scaleToWidth(imSize: IntSize, targetSize: IntSize): Pair<Float, Offset> {
    val scale = targetSize.width.toFloat() / imSize.width
    val offset = Offset(0f, (targetSize.height - imSize.height * scale) * 0.5f)
    return scale to offset
}

private fun scaleToHeight(imSize: IntSize, targetSize: IntSize): Pair<Float, Offset> {
    val scale = targetSize.height.toFloat() / imSize.height
    val offset = Offset((targetSize.width - imSize.width * scale) * 0.5f, 0f)
    return scale to offset
}

// 截圖方法
private fun ImageBitmap.toCroppedImage(rect: Rect): Bitmap? {
    val androidBitmap = this.asAndroidBitmap()
    val croppedBitmap = androidBitmap.config?.let {
        Bitmap.createBitmap(
        rect.width(),
        rect.height(),
            it
        )
    }

    croppedBitmap?.let {
        val canvas = android.graphics.Canvas(croppedBitmap)
        val srcRect = Rect(rect.left, rect.top, rect.right, rect.bottom)
        val dstRect = Rect(0, 0, croppedBitmap.width, croppedBitmap.height)

        canvas.drawBitmap(androidBitmap, srcRect, dstRect, null)
    }
    return croppedBitmap
}