package com.aeroalga.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioBorderSoft
import com.aeroalga.app.ui.theme.BioCyan

@Composable
fun AnimatedFlowTrack(
    modifier: Modifier = Modifier,
    color: Color = BioCyan,
    durationMs: Int = 2200
) {
    val transition = rememberInfiniteTransition(label = "flow_track")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_runner"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val width = size.width
        val y = size.height / 2

        // Dotted pipe track
        drawLine(
            color = BioBorderSoft,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )

        // Animated particle runner
        val runnerX = progress * width
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = 5.dp.toPx(),
            center = Offset(runnerX, y)
        )
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(runnerX, y)
        )
    }
}
