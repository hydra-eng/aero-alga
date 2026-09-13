package com.aeroalga.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ShowChart
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
                text = "Analytics & Circular Economy",
                style = MaterialTheme.typography.headlineMedium,
                color = BioTextPrimary
            )
            Text(
                text = "NODE: $activeNode · HISTORICAL RECOVERY",
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
                        Text("24H TELEMETRY TREND", style = MaterialTheme.typography.labelSmall)
                        Text("CO₂ Capture vs. Biomass Density", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = BioTextMuted)
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(BioLime))
                        Text("CO₂ reduction %", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextMuted)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(BioCyan))
                        Text("Turbidity (NTU)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextMuted)
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
                        Text("CIRCULAR ECONOMY", style = MaterialTheme.typography.labelSmall)
                        Text("Yield & Resource Recovery", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = BioTextMuted)
                }

                // 2x2 Yield Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    YieldMetricBox(
                        title = "Treated Volume",
                        value = "${telemetry.cumulativeVolumeL.toInt()} L",
                        subtitle = "cumulative throughput",
                        modifier = Modifier.weight(1f)
                    )
                    YieldMetricBox(
                        title = "CO₂ Sequestered",
                        value = "${String.format("%.2f", telemetry.co2SequesteredKg)} kg",
                        subtitle = "1.83 kg / kg biomass",
                        highlightColor = BioLime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    YieldMetricBox(
                        title = "Carbon Credits",
                        value = "${String.format("%.4f", telemetry.carbonCreditsT)} t",
                        subtitle = "tCO2e accrued",
                        highlightColor = BioCyan,
                        modifier = Modifier.weight(1f)
                    )
                    YieldMetricBox(
                        title = "Biomass Yield",
                        value = "${String.format("%.2f", telemetry.biomassAccumKg)} kg",
                        subtitle = "dry harvested weight",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Wastewater Nutrient Recovery
                HorizontalDivider(color = BioBorderSoft)

                Text(
                    text = "WASTEWATER NUTRIENT REMOVAL",
                    style = MaterialTheme.typography.labelSmall
                )

                // Nitrogen
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nitrogen removal", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = BioTextMuted)
                        Text("${String.format("%.1f", telemetry.nRemovalPct)}%", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (telemetry.nRemovalPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = BioLime,
                        trackColor = BioSurfaceVariant
                    )
                }

                // Phosphorus
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Phosphorus removal", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = BioTextMuted)
                        Text("${String.format("%.1f", telemetry.pRemovalPct)}%", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (telemetry.pRemovalPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = BioCyan,
                        trackColor = BioSurfaceVariant
                    )
                }

                Text(
                    text = "Target ≥ 90% reduction via dual-algal bio-absorption",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = BioTextMutedDim
                )
            }
        }
    }
}

@Composable
fun YieldMetricBox(
    title: String,
    value: String,
    subtitle: String,
    highlightColor: androidx.compose.ui.graphics.Color = BioTextPrimary,
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
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = highlightColor)
        Text(subtitle, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
    }
}
