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

/**
 * ImageColorPicker allows you to get colors from any images by tapping on the desired color.
 *
 * @param modifier [Modifier] to decorate the internal Canvas.
 * @param controller Allows you to control and interacts with color pickers and all relevant subcomponents.
 * @param paletteImageBitmap [ImageBitmap] to draw the palette.
 * @param wheelImageBitmap [ImageBitmap] to draw the wheel.
 * @param drawOnPosSelected to draw anything on the canvas when [ColorPickerController.selectedPoint] changes
 * @param drawDefaultWheelIndicator should the indicator be drawn on the canvas. Defaults to false if either [wheelImageBitmap] or [drawOnPosSelected] are not null.
 * @param paletteContentScale Represents a rule to apply to scale a source rectangle to be inscribed into a destination.
 * @param previewImagePainter Display an image instead of the palette on the inspection preview mode on Android Studio.
 * @param onColorChanged Color changed listener.
 */

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
    onColorChanged: (colorEnvelope: ColorEnvelope) -> Unit = {},
    onPositionChanged: (position: Offset) -> Unit = {},
    onImageChanged: (image: ImageBitmap) -> Unit = {}
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
        // 計算 selected point 的位置
        val radius = 30 * density
        val centerX = selectedPosition.x / scale - offset.x / scale
        val centerY = selectedPosition.y / scale - offset.y / scale
        // 計算裁減區域，確保不會超過圖片邊界
        val left = (centerX - radius).coerceIn(0f, width.toFloat())
        val top = (centerY - radius).coerceIn(0f, height.toFloat())
        val right = (centerX + radius).coerceIn(0f, width.toFloat())
        val bottom = (centerY + radius).coerceIn(0f, height.toFloat())
        // 確保裁減區域的寬度、高度 > 0
        val croppedWidth = (right - left).coerceAtLeast(1f).toInt()
        val croppedHeight = (bottom - top).coerceAtLeast(1f).toInt()
        // 繪製裁減圖形
        val clipRect = Rect(
            left.toInt(),
            top.toInt(),
            left.toInt() + croppedWidth,
            top.toInt() + croppedHeight
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
                Log.d(com.tinatang.imagecolorpicker.picker.TAG, "Color origPoint: $origPoint, imPoint: $imPoint")
                val px = imageBitmap.getPixel(imPoint.roundToInt())
                val newPoint = imPoint * scale + offset
                Log.d(com.tinatang.imagecolorpicker.picker.TAG, "Color newPoint: $newPoint")
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
    )
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

// 截取图像的扩展函数
private fun ImageBitmap.toCroppedImage(rect: Rect): Bitmap? {
    val androidBitmap = this.asAndroidBitmap() // 将 ImageBitmap 转换为 Bitmap
    val croppedBitmap = androidBitmap.config?.let {
        Bitmap.createBitmap(
        rect.width(),
        rect.height(),
            it
        )
    }

    croppedBitmap?.let {
        val canvas = android.graphics.Canvas(croppedBitmap)
        val srcRect = Rect(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
        val dstRect = Rect(0, 0, croppedBitmap.width, croppedBitmap.height)

        canvas.drawBitmap(androidBitmap, srcRect, dstRect, null)
    }
    return croppedBitmap
}