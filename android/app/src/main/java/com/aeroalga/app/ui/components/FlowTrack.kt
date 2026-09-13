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
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioCyan
import com.aeroalga.app.ui.theme.BioLedOff
import kotlin.math.abs

/**
 * Animated Dot-Matrix Conduit with traveling LED photon clusters
 */
@Composable
fun AnimatedFlowTrack(
    modifier: Modifier = Modifier,
    color: Color = BioCyan,
    durationMs: Int = 2200
) {
    val transition = rememberInfiniteTransition(label = "matrix_flow_track")
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
        val totalWidth = size.width
        val cy = size.height / 2f
        val dotRadius = 1.8f
        val dotSpacing = 8f
        val numDots = (totalWidth / dotSpacing).toInt()

        val runnerCenterPx = progress * totalWidth
        val clusterRadiusPx = 36f

        for (i in 0 until numDots) {
            val cx = i * dotSpacing + (dotSpacing / 2f)
            val distance = abs(cx - runnerCenterPx)

            if (distance < clusterRadiusPx) {
                val intensity = (1f - (distance / clusterRadiusPx)).coerceIn(0f, 1f)
                // Glow halo
                drawCircle(
                    color = color.copy(alpha = intensity * 0.45f),
                    radius = dotRadius * 2.2f,
                    center = Offset(cx, cy)
                )
                // Core
                drawCircle(
                    color = color.copy(alpha = intensity.coerceAtLeast(0.3f)),
                    radius = dotRadius * (1f + intensity * 0.6f),
                    center = Offset(cx, cy)
                )
            } else {
                drawCircle(
                    color = BioLedOff,
                    radius = dotRadius * 0.8f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
