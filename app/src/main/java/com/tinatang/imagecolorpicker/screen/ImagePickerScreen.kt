package com.tinatang.imagecolorpicker.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import com.tinatang.imagecolorpicker.picker.ImageColorPicker
import com.tinatang.imagecolorpicker.rememberColorPickerController
import com.tinatang.imagecolorpicker.utils.FullScreenDialog
import com.tinatang.imagecolorpicker.utils.UrlToBitmap
import com.tinatang.imagecolorpicker.utils.drawWaterDropBorder
import com.tinatang.imagecolorpicker.utils.hollowWhiteSquare
import com.tinatang.imagecolorpicker.utils.solidWhiteCircle
import com.tinatang.imagecolorpicker.utils.toWaterDropBitmap

@Composable
fun ImageColorPickerScreen() {
    val imageUrl = "https://i.imgur.com/DJWfzBr.jpeg"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 使用 UrlToBitmap 函數來加載圖片
        UrlToBitmap(
            imageURL = imageUrl,
            context = LocalContext.current,
            onSuccess = {
                // 成功回調，顯示加載的圖片
                TestScreen(imageBitmap = it.asImageBitmap())
                //EditorScreen(it.asImageBitmap())
            },
            onError = {
                // 顯示錯誤信息
                it.printStackTrace()
            }
        )
    }
}

@Composable
fun TestScreen(imageBitmap: ImageBitmap) {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showDialog = true }) {
            Text("show dialog")
        }
    }
    if (showDialog) {
        FullScreenDialog(
            imageBitmap = imageBitmap,
            scaleImageSize = 50,
            onDismissRequest = {
                showDialog = false
            }
        )
    }
}

@Composable
fun EditorScreen(
    imageBitmap: ImageBitmap,
    scaleImageSize: Int,
    onColorChanged: (Color) -> Unit,
    onDragEnd: () -> Unit
) {
    val TAG = "EditorScreen"
    val controller = rememberColorPickerController()
    var hexCode by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.Transparent) }
    var selectedPoint by remember { mutableStateOf(Offset(0f, 0f)) }

    val solidWhiteCircle = solidWhiteCircle()
    val hollowWhiteSquare = hollowWhiteSquare()
    var scaleBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(color) {
        onColorChanged(color)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ImageColorPicker(
            modifier = Modifier.fillMaxSize(),
            controller = controller,
            paletteImageBitmap = imageBitmap,
            scaleImageSize = scaleImageSize,
            onColorChanged = { colorEnvelope ->
                hexCode = colorEnvelope.hexCode
                color = colorEnvelope.color
            },
            onPositionChanged = { point ->
                selectedPoint = point
                Log.d(TAG, "selectedPoint: $selectedPoint")
            },
            previewImagePainter = BitmapPainter(imageBitmap),
            wheelImageBitmap = solidWhiteCircle,
            onImageChanged = {
                scaleBitmap = it
            },
            drawOnPosSelected = {
                scaleBitmap?.let {
                    val borderSize = 16
                    // 計算 scale image border 要顯示的位置
                    val borderTopLeft = Offset(
                        x = selectedPoint.x - it.width / 2 - borderSize / 2,
                        y = selectedPoint.y - it.height - solidWhiteCircle.height / 2 - borderSize / 2
                    )

                    // 繪製以 selected point 為中心放大的圖片
                    drawImage(
                        image = drawWaterDropBorder(it.width + borderSize),
                        topLeft = borderTopLeft
                    )

                    // 計算 scale image 要顯示的位置
                    val topLeft = Offset(
                        x = selectedPoint.x - it.width / 2,
                        y = selectedPoint.y - it.height - solidWhiteCircle.height / 2
                    )

                    // 繪製以 selected point 為中心放大的圖片
                    drawImage(
                        image = it.toWaterDropBitmap(),
                        topLeft = topLeft
                    )

                    // 計算 square image 的位置，將它放在 scaleImage 的正中心
                    val squareTopLeft = Offset(
                        x = selectedPoint.x - solidWhiteCircle.width / 2 - hollowWhiteSquare.width / 2,
                        y = selectedPoint.y - solidWhiteCircle.height / 2 - it.height / 2 - hollowWhiteSquare.height / 2
                    )

                    // 繪製圓形圖像
                    drawImage(
                        image = hollowWhiteSquare,
                        topLeft = squareTopLeft
                    )
                }
            },
            onDragEnd = onDragEnd
        )
    }
}