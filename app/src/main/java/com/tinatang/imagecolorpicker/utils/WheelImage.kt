package com.tinatang.imagecolorpicker.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.tinatang.imagecolorpicker.theme.shadowColorOne
import com.tinatang.imagecolorpicker.theme.shadowColorTwo

@Composable
fun solidWhiteCircle(): ImageBitmap {
    val density = LocalDensity.current.density

    // 設定圓形的尺寸
    val sizeInPx = 1.dp.value * density
    val bitmap = Bitmap.createBitmap(sizeInPx.toInt(), sizeInPx.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = Color.WHITE  // 設置顏色為透明
        style = Paint.Style.FILL  // 設置為實心填充
    }

    // 繪製實心白色圓形
    canvas.drawCircle(sizeInPx / 2, sizeInPx / 2, sizeInPx / 2, paint)

    return bitmap.asImageBitmap()
}

@Composable
fun hollowWhiteSquare(): ImageBitmap {
    val density = LocalDensity.current.density

    // 設定正方形的尺寸
    val sizeInDp = 10.dp
    val sizeInPx = sizeInDp.value * density

    // 設定圓角的半徑
    val cornerRadiusDp = 2.dp
    val cornerRadiusPx = cornerRadiusDp.value * density

    // 創建 bitmap 和 canvas
    val bitmap = Bitmap.createBitmap(sizeInPx.toInt(), sizeInPx.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 創建畫筆，設置為空心正方形（STROKE）
    val paint = Paint().apply {
        color = Color.WHITE  // 設置顏色為白色
        style = Paint.Style.STROKE  // 設置為空心
        strokeWidth = 5f  // 設置邊框的寬度
        isAntiAlias = true  // 開啟抗鋸齒
        // 設定陰影
        setShadowLayer(3f, 0f, 1f, shadowColorOne.toArgb())
    }

    // 計算正方形的四條邊
    val halfStrokeWidth = paint.strokeWidth / 2f
    val left = halfStrokeWidth
    val top = halfStrokeWidth
    val right = sizeInPx - halfStrokeWidth
    val bottom = sizeInPx - halfStrokeWidth

    // 绘制带圆角的空心正方形
    canvas.drawRoundRect(left, top, right, bottom, cornerRadiusPx, cornerRadiusPx, paint)

    return bitmap.asImageBitmap()
}

/** 將圖片裁減成水滴形狀 */
fun ImageBitmap.toWaterDropBitmap(): ImageBitmap {
    // 設定裁減大小
    val size = this.width.coerceAtMost(this.height)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 定義畫筆
    val paint = Paint().apply {
        isAntiAlias = true
    }

    // 合併圓形和倒三角形
    val finalPath = drawWaterDropPath(size)
    // 設定畫布裁減路徑
    canvas.clipPath(finalPath.asAndroidPath())
    // 將圖片裁減成路徑形狀
    canvas.drawBitmap(this.asAndroidBitmap(), 0f, 0f, paint)

    return bitmap.asImageBitmap()
}

/** 畫出水滴形狀路徑 */
fun drawWaterDropPath(
    size: Int
): Path {
    // 設定圖片大小
    val width = size.toFloat()
    val height = size.toFloat()
    // 倒三角形的高度
    val triangleHeight = height * 0.2f // 設置倒三角形的高度為總高度的30%

    // 定義圓形路徑，設定水平置中
    val circlePath = Path().apply {
        val left = (width - width * 0.9f) / 2
        val top = 5f
        val right = left + width * 0.9f
        val bottom = height * 0.9f
        addArc(Rect(left, top, right, bottom), 0f, 360f)
    }

    // 定義倒三角形的路徑
    val trianglePath = Path().apply {
        val triangleTopY = height - triangleHeight

        moveTo(width / 2, height)
        lineTo(width * 0.35f, triangleTopY)
        lineTo(width * 0.65f, triangleTopY)
        close() // 關閉路徑，形成倒三角形
    }

    // 合併圓形和倒三角形
    val path = Path().apply {
        addPath(circlePath)
        addPath(trianglePath)
    }
    return path
}

fun drawWaterDropBorder(size: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 定義畫筆(第一層陰影)
    val paintShadowOne = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        setShadowLayer(3f, 0f, 1f, shadowColorOne.toArgb())
    }

    // 定義畫筆(第二層陰影)
    val paintShadowTwo = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        setShadowLayer(2f, 0f, 1f, shadowColorTwo.toArgb())
    }

    // 合併圓形和倒三角形
    val borderPath = drawWaterDropPath(size)
    canvas.save()
    canvas.drawPath(borderPath.asAndroidPath(), paintShadowOne)
    canvas.drawPath(borderPath.asAndroidPath(), paintShadowTwo)
    canvas.restore()

    return bitmap.asImageBitmap()
}