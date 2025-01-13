package com.tinatang.imagecolorpicker.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.tinatang.imagecolorpicker.ColorPickerController
import com.tinatang.imagecolorpicker.data.ColorEnvelope
import com.tinatang.imagecolorpicker.extension.drawImageCenterAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ColorPicker allows you to get colors from a palette by tapping on the desired color.
 * See HsvColorPicker and ImageColorPicker for more concrete functions.
 *
 * @param modifier [Modifier] to decorate the internal Canvas.
 * @param controller Allows you to control and interacts with color pickers and all relevant subcomponents.
 * @param wheelImageBitmap [ImageBitmap] to draw the wheel.
 * @param drawOnPosSelected to draw anything on the canvas when [ColorPickerController.selectedPoint] changes
 * @param drawDefaultWheelIndicator should the indicator be drawn on the canvas. Defaults to false if either [wheelImageBitmap] or [drawOnPosSelected] are not null.
 * @param onColorChanged Color changed listener.
 */
@Composable
internal fun ColorPicker(
    modifier: Modifier,
    controller: ColorPickerController,
    wheelImageBitmap: ImageBitmap? = null,
    drawOnPosSelected: (DrawScope.() -> Unit)? = null,
    drawDefaultWheelIndicator: Boolean = wheelImageBitmap == null && drawOnPosSelected == null,
    onColorChanged: (colorEnvelope: ColorEnvelope) -> Unit = {},
    sizeChanged: (IntSize) -> Unit = { _ -> },
    setup: ColorPickerController.() -> Unit,
    draw: Canvas.(size: Size) -> Unit,
    onDragEnd: () -> Unit,
) {
    var initialized by remember { mutableStateOf(false) }

    val debounceDuration = controller.debounceDuration
    DisposableEffect(key1 = controller, key2 = debounceDuration) {
        controller.coroutineScope.launch(Dispatchers.Main) {
            controller.getColorFlow(debounceDuration ?: 0).collect {
                onColorChanged(it)
            }
        }
        onDispose { controller.releaseBitmap() }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                if (size.width != 0 && size.height != 0) {
                    sizeChanged(size)
                    controller.canvasSize = size.toSize()
                    if (!initialized) {
                        controller.wheelBitmap = wheelImageBitmap
                        controller.setup()
                        initialized = true
                    }
                }
            }
            // 點按
            /*.pointerInput(Unit) {
                detectTapGestures { offset ->
                    controller.selectByCoordinate(offset, true)
                }
            }*/
            // 按壓
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        controller.selectByCoordinate(change.position, true)
                    },
                    // 拖曳結束時
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            },
    ) {
        drawIntoCanvas { canvas ->
            // draw bitmap on the canvas.
            canvas.save()
            canvas.clipRect(0f, 0f, size.width, size.height)
            canvas.draw(size)
            canvas.restore()

            // draw wheel bitmap on the canvas.
            canvas.drawWheel(
                point = controller.selectedPoint.value,
                wheelBitmap = controller.wheelBitmap,
                drawDefaultWheelIndicator = drawDefaultWheelIndicator,
                wheelRadiusPx = controller.wheelRadius.toPx(),
                wheelPaint = controller.wheelPaint,
            )

            drawOnPosSelected?.let { it() }
        }
        controller.reviseTick.intValue
    }
}

private fun Canvas.drawWheel(
    point: Offset,
    wheelBitmap: ImageBitmap?,
    drawDefaultWheelIndicator: Boolean,
    wheelRadiusPx: Float,
    wheelPaint: Paint,
) {
    wheelBitmap?.let { drawImageCenterAt(it, point) }
    if (drawDefaultWheelIndicator) {
        drawCircle(point, wheelRadiusPx, wheelPaint)
    }
}