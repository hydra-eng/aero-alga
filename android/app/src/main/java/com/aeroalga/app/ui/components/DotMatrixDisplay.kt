package com.aeroalga.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aeroalga.app.ui.theme.BioLedOff
import com.aeroalga.app.ui.theme.BioLime

/**
 * 5x7 Dot-Matrix LED Typography Font Map
 * Each character is represented by 5 columns of 7 bits (0 = off, 1 = on).
 */
private val FONT_5X7: Map<Char, IntArray> = mapOf(
    '0' to intArrayOf(0x3E, 0x51, 0x49, 0x45, 0x3E),
    '1' to intArrayOf(0x00, 0x42, 0x7F, 0x40, 0x00),
    '2' to intArrayOf(0x42, 0x61, 0x51, 0x49, 0x46),
    '3' to intArrayOf(0x21, 0x41, 0x45, 0x4B, 0x31),
    '4' to intArrayOf(0x18, 0x14, 0x12, 0x7F, 0x10),
    '5' to intArrayOf(0x27, 0x45, 0x45, 0x45, 0x39),
    '6' to intArrayOf(0x3C, 0x4A, 0x49, 0x49, 0x30),
    '7' to intArrayOf(0x01, 0x71, 0x09, 0x05, 0x03),
    '8' to intArrayOf(0x36, 0x49, 0x49, 0x49, 0x36),
    '9' to intArrayOf(0x06, 0x49, 0x49, 0x29, 0x1E),
    '.' to intArrayOf(0x00, 0x60, 0x60, 0x00, 0x00),
    ':' to intArrayOf(0x00, 0x36, 0x36, 0x00, 0x00),
    '%' to intArrayOf(0x63, 0x33, 0x18, 0x0C, 0x66),
    '-' to intArrayOf(0x08, 0x08, 0x08, 0x08, 0x08),
    '+' to intArrayOf(0x08, 0x08, 0x3E, 0x08, 0x08),
    ' ' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00),
    'A' to intArrayOf(0x7E, 0x11, 0x11, 0x11, 0x7E),
    'B' to intArrayOf(0x7F, 0x49, 0x49, 0x49, 0x36),
    'C' to intArrayOf(0x3E, 0x41, 0x41, 0x41, 0x22),
    'D' to intArrayOf(0x7F, 0x41, 0x41, 0x22, 0x1C),
    'E' to intArrayOf(0x7F, 0x49, 0x49, 0x49, 0x41),
    'F' to intArrayOf(0x7F, 0x09, 0x09, 0x09, 0x01),
    'L' to intArrayOf(0x7F, 0x40, 0x40, 0x40, 0x40),
    'M' to intArrayOf(0x7F, 0x02, 0x0C, 0x02, 0x7F),
    'N' to intArrayOf(0x7F, 0x04, 0x08, 0x10, 0x7F),
    'O' to intArrayOf(0x3E, 0x41, 0x41, 0x41, 0x3E),
    'P' to intArrayOf(0x7F, 0x09, 0x09, 0x09, 0x06),
    'R' to intArrayOf(0x7F, 0x09, 0x19, 0x29, 0x46),
    'S' to intArrayOf(0x46, 0x49, 0x49, 0x49, 0x31),
    'T' to intArrayOf(0x01, 0x01, 0x7F, 0x01, 0x01),
    'U' to intArrayOf(0x3F, 0x40, 0x40, 0x40, 0x3F),
    'V' to intArrayOf(0x1F, 0x20, 0x40, 0x20, 0x1F),
    'W' to intArrayOf(0x7F, 0x20, 0x18, 0x20, 0x7F),
    'X' to intArrayOf(0x63, 0x14, 0x08, 0x14, 0x63),
    'Y' to intArrayOf(0x07, 0x08, 0x70, 0x08, 0x07)
)

/**
 * Authentic 5x7 LED Dot Matrix Character Display
 */
@Composable
fun DotMatrixDisplay(
    text: String,
    modifier: Modifier = Modifier,
    activeColor: Color = BioLime,
    inactiveColor: Color = BioLedOff,
    dotRadius: Dp = 1.8.dp,
    dotSpacing: Dp = 1.4.dp,
    charSpacing: Dp = 3.dp,
    showInactiveDots: Boolean = true
) {
    val upperText = text.uppercase()
    val numChars = upperText.length
    if (numChars == 0) return

    val colsPerChar = 5
    val rowsPerChar = 7
    val charWidthDp = (dotRadius * 2 * colsPerChar) + (dotSpacing * (colsPerChar - 1))
    val totalWidthDp = (charWidthDp * numChars) + (charSpacing * (numChars - 1))
    val totalHeightDp = (dotRadius * 2 * rowsPerChar) + (dotSpacing * (rowsPerChar - 1))

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .width(totalWidthDp)
                .height(totalHeightDp)
        ) {
            val dotRadiusPx = dotRadius.toPx()
            val dotSpacingPx = dotSpacing.toPx()
            val charSpacingPx = charSpacing.toPx()
            val charWidthPx = (dotRadiusPx * 2 * colsPerChar) + (dotSpacingPx * (colsPerChar - 1))

            var charStartX = 0f

            for (c in upperText) {
                val matrix = FONT_5X7[c] ?: FONT_5X7[' '] ?: intArrayOf(0, 0, 0, 0, 0)

                for (col in 0 until colsPerChar) {
                    val colBits = matrix[col]
                    val x = charStartX + (col * (dotRadiusPx * 2 + dotSpacingPx)) + dotRadiusPx

                    for (row in 0 until rowsPerChar) {
                        val isLit = (colBits and (1 shl row)) != 0
                        val y = (row * (dotRadiusPx * 2 + dotSpacingPx)) + dotRadiusPx

                        if (isLit) {
                            // Soft LED glow halo
                            drawCircle(
                                color = activeColor.copy(alpha = 0.35f),
                                radius = dotRadiusPx * 1.7f,
                                center = Offset(x, y)
                            )
                            // Solid core
                            drawCircle(
                                color = activeColor,
                                radius = dotRadiusPx,
                                center = Offset(x, y)
                            )
                        } else if (showInactiveDots) {
                            drawCircle(
                                color = inactiveColor,
                                radius = dotRadiusPx * 0.85f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
                charStartX += charWidthPx + charSpacingPx
            }
        }
    }
}
