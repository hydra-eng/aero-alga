package com.aeroalga.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioBorderSoft
import com.aeroalga.app.ui.theme.BioLime
import com.aeroalga.app.ui.theme.BioSurfaceVariant
import com.aeroalga.app.ui.theme.BioWarn

@Composable
fun HomeostasisSafeZoneBar(
    currentVal: Float,
    minVal: Float,
    maxVal: Float,
    safeMin: Float,
    safeMax: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalRange = (maxVal - minVal).coerceAtLeast(0.001f)

        // Background track
        drawRoundRect(
            color = BioSurfaceVariant,
            size = Size(width, height),
            cornerRadius = CornerRadius(height / 2, height / 2)
        )

        // Safe zone green segment
        val safeLeftPct = ((safeMin - minVal) / totalRange).coerceIn(0f, 1f)
        val safeRightPct = ((safeMax - minVal) / totalRange).coerceIn(0f, 1f)
        val safeWidth = (safeRightPct - safeLeftPct) * width
        drawRoundRect(
            color = BioLime.copy(alpha = 0.25f),
            topLeft = Offset(safeLeftPct * width, 0f),
            size = Size(safeWidth, height),
            cornerRadius = CornerRadius(height / 2, height / 2)
        )

        // Indicator Marker
        val currentPct = ((currentVal - minVal) / totalRange).coerceIn(0f, 1f)
        val markerX = currentPct * width
        val isSafe = currentVal in safeMin..safeMax
        val markerColor = if (isSafe) BioLime else BioWarn

        drawCircle(
            color = markerColor,
            radius = height * 0.9f,
            center = Offset(markerX, height / 2)
        )
        drawCircle(
            color = BioBorderSoft,
            radius = height * 0.9f,
            center = Offset(markerX, height / 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}
