package com.tinatang.imagecolorpicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult

@Composable
fun UrlToBitmap(
    imageURL: String,
    context: Context,
    onSuccess: @Composable (bitmap: Bitmap) -> Unit,
    onError: @Composable (error: Throwable) -> Unit
) {
    // 用來記錄圖片是否已經加載完成
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    val errorState = remember { mutableStateOf<Throwable?>(null) }

    // 使用 LaunchedEffect 來啟動協程
    LaunchedEffect(imageURL) {
        try {
            // 使用 Coil 的 ImageLoader 加載圖片
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageURL)
                .allowHardware(false) // 禁用硬件加速，防止一些顯示問題
                .build()

            val result = loader.execute(request)

            if (result is SuccessResult) {
                // 成功加載圖片
                bitmapState.value = (result.drawable as BitmapDrawable).bitmap
            } else if (result is ErrorResult) {
                // 失敗處理
                errorState.value = result.throwable
            }
        } catch (e: Exception) {
            // 捕獲異常
            errorState.value = e
        }
    }

    // 根據狀態顯示圖片或錯誤
    bitmapState.value?.let {
        onSuccess(it)
    } ?: errorState.value?.let {
        onError(it)
    }
}

@Preview
@Composable
fun PreviewUrlToBitmap() {
    // 假設 URL 和上下文已經提供
    UrlToBitmap(
        imageURL = "https://i.imgur.com/DJWfzBr.jpeg",
        context = LocalContext.current,
        onSuccess = { bitmap ->
            // 顯示圖片成功的回調
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
        },
        onError = { error ->
            // 顯示錯誤的回調
            Text(text = "Error: ${error.localizedMessage}")
        }
    )
}