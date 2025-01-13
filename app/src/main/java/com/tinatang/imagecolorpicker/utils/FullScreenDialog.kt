package com.tinatang.imagecolorpicker.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tinatang.imagecolorpicker.screen.EditorScreen

@Composable
fun FullScreenDialog(
    imageBitmap: ImageBitmap,
    onDismissRequest: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.Transparent) }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    EditorScreen(
                        imageBitmap = imageBitmap,
                        onColorChanged = {
                            selectedColor = it
                        }
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = selectedColor,
                                shape = CircleShape
                            )
                            .size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box {
                        Button(onClick = onDismissRequest) {
                            Text("Dismiss")
                        }
                    }
                }

            }
        }
    }
}