package com.tinatang.imagecolorpicker.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinatang.imagecolorpicker.picker.ImageColorPicker
import com.tinatang.imagecolorpicker.rememberColorPickerController
import com.tinatang.imagecolorpicker.utils.UrlToBitmap
import com.tinatang.imagecolorpicker.utils.drawWaterDropBorder
import com.tinatang.imagecolorpicker.utils.hollowWhiteSquare
import com.tinatang.imagecolorpicker.utils.solidWhiteCircle
import com.tinatang.imagecolorpicker.utils.toWaterDropBitmap

@Composable
fun ImageColorPickerScreen() {
    val imageUrl = "https://i.imgur.com/DJWfzBr.jpeg"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 使用 UrlToBitmap 函數來加載圖片
        UrlToBitmap(
            imageURL = imageUrl,
            context = LocalContext.current,
            onSuccess = {
                // 顯示主畫面
                MainScreen(it.asImageBitmap())
            },
            onError = {
                // 顯示錯誤信息
                it.printStackTrace()
            }
        )
    }
}

@Composable
fun MainScreen(imageBitmap: ImageBitmap) {
    // 選擇的顏色
    var selectedColor by remember { mutableStateOf(Color.Transparent) }
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
        contentColor = Color.White
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp)
            ) {
                EditorScreen(
                    imageBitmap = imageBitmap,
                    onColorChanged = {
                        selectedColor = it
                    }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Selected Color",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = selectedColor,
                                shape = CircleShape
                            )
                            .size(50.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun EditorScreen(
    imageBitmap: ImageBitmap,
    onColorChanged: (Color) -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            controller = controller,
            paletteImageBitmap = imageBitmap,
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
            }
        )
    }
}