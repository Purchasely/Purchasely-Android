package com.purchasely.samplev2.presentation.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * Modifier extension.
 */
fun Modifier.drawTwoColors(
    topColor: Color,
    bottomColor: Color
) = this.then(
    Modifier.drawBehind {

        val width = size.width
        val height = size.height

        drawRect(
            color = topColor,
            size = Size(width, height / 3)
        )

        drawRect(
            color = bottomColor,
            topLeft = Offset(0f, (height / 3)),
            size = Size(width, (height / 3) * 2)
        )
    }
)