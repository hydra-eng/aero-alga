package com.aeroalga.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeroalga.app.data.repository.NodeRepository
import com.aeroalga.app.ui.components.DotMatrixBar
import com.aeroalga.app.ui.components.DotMatrixDisplay
import com.aeroalga.app.ui.components.DualTrendLineChart
import com.aeroalga.app.ui.theme.*

@Composable
fun AnalyticsScreen(
    repository: NodeRepository,
    modifier: Modifier = Modifier
) {
    val telemetry by repository.latestTelemetry.collectAsState()
    val activeNode by repository.activeNodeId.collectAsState()

    var co2Points by remember { mutableStateOf(listOf(22f, 25f, 29f, 34f, 32f, 36f, 40f, 38f, 42f, 45f)) }
    var ntuPoints by remember { mutableStateOf(listOf(150f, 170f, 185f, 210f, 240f, 260f, 280f, 305f, 320f, 340f)) }

    LaunchedEffect(activeNode) {
        val history = repository.fetchHistory(activeNode, 24)
        if (history.isNotEmpty()) {
            co2Points = history.map { it.co2Efficiency }
            ntuPoints = history.map { it.turbidityNtu }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BioBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Column {
            Text(
                text = "MATRIX ANALYTICS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = BioTextPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "NODE: $activeNode · CIRCULAR BIO-ECONOMY",
                style = MaterialTheme.typography.labelSmall,
                color = BioTextMuted
            )
        }

        // 24H TREND CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BioSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BioBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("24H INTEGRATION PROFILE", style = MaterialTheme.typography.labelSmall)
                        Text("CO₂ Capture vs. Biomass Density", fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = BioTextMuted)
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(BioLime))
                        Text("CO₂ FIX %", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioLime)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(BioCyan))
                        Text("TURBIDITY NTU", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioCyan)
                    }
                }

                DualTrendLineChart(
                    co2ReductionPoints = co2Points,
                    turbidityPoints = ntuPoints
                )
            }
        }

        // CIRCULAR ECONOMY CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BioSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BioBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CIRCULAR MATRIX", style = MaterialTheme.typography.labelSmall)
                        Text("Yield & Nutrient Extraction", fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = BioTextMuted)
                }

                // 2x2 Matrix Yield Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MatrixYieldMetricBox(
                        title = "Treated Air Volume",
                        valueString = "${telemetry.cumulativeVolumeL.toInt()} L",
                        subtitle = "CUMULATIVE THROUGHPUT",
                        activeColor = BioTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MatrixYieldMetricBox(
                        title = "CO₂ Sequestered",
                        valueString = "${String.format("%.2f", telemetry.co2SequesteredKg)} KG",
                        subtitle = "1.83 KG/KG BIOMASS",
                        activeColor = BioLime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MatrixYieldMetricBox(
                        title = "Carbon Credits",
                        valueString = "${String.format("%.4f", telemetry.carbonCreditsT)} T",
                        subtitle = "tCO2e ACCRUED",
                        activeColor = BioCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MatrixYieldMetricBox(
                        title = "Biomass Harvested",
                        valueString = "${String.format("%.2f", telemetry.biomassAccumKg)} KG",
                        subtitle = "DRY WEIGHT ACCUM",
                        activeColor = BioLime,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Wastewater Nutrient Recovery (Segmented LED Bars)
                HorizontalDivider(color = BioBorderSoft)

                Text(
                    text = "WASTEWATER NUTRIENT REMOVAL MATRIX",
                    style = MaterialTheme.typography.labelSmall
                )

                // Nitrogen
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("NITROGEN (N) RECOVERY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextMuted)
                        DotMatrixDisplay(
                            text = "${String.format("%.1f", telemetry.nRemovalPct)}%",
                            activeColor = BioLime,
                            dotRadius = 1.1.dp,
                            dotSpacing = 0.8.dp,
                            showInactiveDots = false
                        )
                    }
                    DotMatrixBar(
                        value = telemetry.nRemovalPct,
                        min = 0f,
                        max = 100f,
                        segmentCount = 24,
                        defaultColor = BioLime
                    )
                }

                // Phosphorus
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PHOSPHORUS (P) RECOVERY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextMuted)
                        DotMatrixDisplay(
                            text = "${String.format("%.1f", telemetry.pRemovalPct)}%",
                            activeColor = BioCyan,
                            dotRadius = 1.1.dp,
                            dotSpacing = 0.8.dp,
                            showInactiveDots = false
                        )
                    }
                    DotMatrixBar(
                        value = telemetry.pRemovalPct,
                        min = 0f,
                        max = 100f,
                        segmentCount = 24,
                        defaultColor = BioCyan
                    )
                }

                Text(
                    text = "TARGET THRESHOLD ≥ 85.0% BIO-REDUCTION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = BioTextMutedDim
                )
            }
        }
    }
}

@Composable
fun MatrixYieldMetricBox(
    title: String,
    valueString: String,
    subtitle: String,
    activeColor: androidx.compose.ui.graphics.Color = BioTextPrimary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BioSurfaceVariant)
            .border(1.dp, BioBorderSoft, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(2.dp))
        DotMatrixDisplay(
            text = valueString,
            activeColor = activeColor,
            dotRadius = 1.2.dp,
            dotSpacing = 0.8.dp,
            showInactiveDots = false
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = BioTextMutedDim)
    }
}
