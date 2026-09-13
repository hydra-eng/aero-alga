package com.aeroalga.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioBorderSoft
import com.aeroalga.app.ui.theme.BioCyan
import com.aeroalga.app.ui.theme.BioLime

@Composable
fun DualTrendLineChart(
    co2ReductionPoints: List<Float>,
    turbidityPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = (height / gridLines) * i
            drawLine(
                color = BioBorderSoft,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        if (co2ReductionPoints.size < 2) return@Canvas

        val stepX = width / (co2ReductionPoints.size - 1)
        val maxCo2 = 70f
        val minCo2 = 0f

        // Draw CO2 Reduction Curve (Lime)
        val co2Path = Path()
        val fillPath = Path()

        co2ReductionPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (1f - (value - minCo2) / (maxCo2 - minCo2)).coerceIn(0f, 1f)
            val y = normalizedY * height

            if (index == 0) {
                co2Path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                co2Path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(BioLime.copy(alpha = 0.18f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw line
        drawPath(
            path = co2Path,
            color = BioLime,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Turbidity Curve (Cyan dashed)
        if (turbidityPoints.size >= 2) {
            val maxNtu = 500f
            val minNtu = 0f
            val ntuPath = Path()

            turbidityPoints.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedY = (1f - (value - minNtu) / (maxNtu - minNtu)).coerceIn(0f, 1f)
                val y = normalizedY * height

                if (index == 0) {
                    ntuPath.moveTo(x, y)
                } else {
                    ntuPath.lineTo(x, y)
                }
            }

            drawPath(
                path = ntuPath,
                color = BioCyan,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
