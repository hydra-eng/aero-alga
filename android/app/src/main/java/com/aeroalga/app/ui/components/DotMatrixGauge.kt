package com.aeroalga.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeroalga.app.ui.theme.BioLedOff
import com.aeroalga.app.ui.theme.BioLime
import com.aeroalga.app.ui.theme.BioTextMutedDim
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Radial gauge composed of discrete LED dot matrix pips arranged circularly.
 */
@Composable
fun DotMatrixGauge(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    pipCount: Int = 32,
    activeColor: Color = BioLime,
    inactiveColor: Color = BioLedOff
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 600),
        label = "MatrixGaugeAnimation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val center = Offset(canvasSize / 2f, canvasSize / 2f)
            val radius = (canvasSize / 2f) - 10f
            val pipRadius = 3.6f

            val litCount = ((animatedPercent / 100f) * pipCount).roundToInt()
            val stepAngle = (2 * PI) / pipCount
            val startAngle = -PI / 2 // Start at 12 o'clock

            for (i in 0 until pipCount) {
                val angle = startAngle + (i * stepAngle)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                val isLit = i < litCount

                if (isLit) {
                    // Outer diffuse glow
                    drawCircle(
                        color = activeColor.copy(alpha = 0.35f),
                        radius = pipRadius * 2f,
                        center = Offset(x, y)
                    )
                    // Inner bright LED core
                    drawCircle(
                        color = activeColor,
                        radius = pipRadius,
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = inactiveColor,
                        radius = pipRadius * 0.9f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DotMatrixDisplay(
                text = "${animatedPercent.roundToInt()}%",
                activeColor = activeColor,
                dotRadius = 1.3.dp,
                dotSpacing = 0.9.dp,
                charSpacing = 2.dp,
                showInactiveDots = false
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = BioTextMutedDim,
                letterSpacing = 0.5.sp
            )
        }
    }
}
