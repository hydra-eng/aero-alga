package com.aeroalga.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioDanger
import com.aeroalga.app.ui.theme.BioLedOff
import com.aeroalga.app.ui.theme.BioLime
import com.aeroalga.app.ui.theme.BioWarn
import kotlin.math.roundToInt

/**
 * Segmented LED Dot Matrix Bar Composable for Homeostasis & Nutrient Recovery
 */
@Composable
fun DotMatrixBar(
    value: Float,
    min: Float,
    max: Float,
    safeMin: Float? = null,
    safeMax: Float? = null,
    modifier: Modifier = Modifier,
    segmentCount: Int = 18,
    height: Dp = 8.dp,
    defaultColor: Color = BioLime
) {
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 500),
        label = "MatrixBarAnimation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val barHeight = size.height
        val gap = 3f
        val pipWidth = (totalWidth - (gap * (segmentCount - 1))) / segmentCount
        val litCount = (animatedFraction * segmentCount).roundToInt()

        for (i in 0 until segmentCount) {
            val startX = i * (pipWidth + gap)
            val isLit = i < litCount

            val color = if (isLit) {
                if (safeMin != null && safeMax != null) {
                    if (value < safeMin || value > safeMax) {
                        if (value < safeMin - (safeMax - safeMin) * 0.3f || value > safeMax + (safeMax - safeMin) * 0.3f) {
                            BioDanger
                        } else {
                            BioWarn
                        }
                    } else {
                        defaultColor
                    }
                } else {
                    defaultColor
                }
            } else {
                BioLedOff
            }

            // Draw rounded LED segment
            drawRoundRect(
                color = color,
                topLeft = Offset(startX, 0f),
                size = Size(pipWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Draw soft glow if lit
            if (isLit) {
                drawRoundRect(
                    color = color.copy(alpha = 0.25f),
                    topLeft = Offset(startX - 1f, -1f),
                    size = Size(pipWidth + 2f, barHeight + 2f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
    }
}
