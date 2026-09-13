package com.aeroalga.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeroalga.app.data.model.TelemetryData
import com.aeroalga.app.data.repository.NodeRepository
import com.aeroalga.app.ui.components.AnimatedFlowTrack
import com.aeroalga.app.ui.components.EfficiencyRadialRing
import com.aeroalga.app.ui.components.HomeostasisSafeZoneBar
import com.aeroalga.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: NodeRepository,
    modifier: Modifier = Modifier
) {
    val telemetry by repository.latestTelemetry.collectAsState()
    val isConnected by repository.isConnected.collectAsState()
    val activeNode by repository.activeNodeId.collectAsState()
    val scope = rememberCoroutineScope()

    var pumpSliderVal by remember(telemetry.pumpSpeedPct) { mutableFloatStateOf(telemetry.pumpSpeedPct.toFloat()) }
    var ledToggleVal by remember(telemetry.ledAssistOn) { mutableStateOf(telemetry.ledAssistOn) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BioBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AeroAlga BDPA-v2",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BioTextPrimary
                )
                Text(
                    text = "NODE: $activeNode · LIVE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioTextMuted
                )
            }

            // Live Pulse Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BioSurfaceVariant)
                    .border(1.dp, BioBorderSoft, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) BioLime else BioWarn)
                )
                Text(
                    text = if (isConnected) "LIVE" else "SYNC",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) BioLime else BioWarn
                )
            }
        }

        // STATS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeaderStatBox(
                icon = Icons.Default.Air,
                label = "Flow Rate",
                value = "${String.format("%.1f", telemetry.flowRate)} L/min",
                modifier = Modifier.weight(1f)
            )
            HeaderStatBox(
                icon = Icons.Default.Timer,
                label = "Runtime",
                value = formatRuntime(telemetry.runtimeSec),
                modifier = Modifier.weight(1f)
            )
        }

        // PARTICULATE FILTRATION CARD
        FiltrationCard(telemetry)

        // CO2 FIXATION CARD
        Co2FixationCard(telemetry)

        // BIOLOGICAL HOMEOSTASIS SECTION
        Text(
            text = "BIOLOGICAL HOMEOSTASIS",
            style = MaterialTheme.typography.labelSmall,
            color = BioTextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeostasisItemCard(
                icon = Icons.Default.Science,
                iconColor = BioLime,
                title = "pH Level",
                value = String.format("%.2f", telemetry.ph),
                safeRangeText = "safe 7.2–8.5",
                current = telemetry.ph,
                min = 4f, max = 12f, safeMin = 7.2f, safeMax = 8.5f,
                modifier = Modifier.weight(1f)
            )
            HomeostasisItemCard(
                icon = Icons.Default.Thermostat,
                iconColor = BioWarn,
                title = "Water Temp",
                value = "${String.format("%.1f", telemetry.waterTemp)}°C",
                safeRangeText = "safe 20–28°C",
                current = telemetry.waterTemp,
                min = 10f, max = 38f, safeMin = 20f, safeMax = 28f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeostasisItemCard(
                icon = Icons.Default.WaterDrop,
                iconColor = BioCyan,
                title = "Turbidity",
                value = "${telemetry.turbidityNtu.toInt()} NTU",
                safeRangeText = "biomass proxy",
                current = telemetry.turbidityNtu,
                min = 0f, max = 600f, safeMin = 150f, safeMax = 450f,
                modifier = Modifier.weight(1f)
            )
            HomeostasisItemCard(
                icon = Icons.Default.Waves,
                iconColor = BioLime,
                title = "Dissolved O₂",
                value = "${String.format("%.1f", telemetry.dissolvedOxygen)} mg/L",
                safeRangeText = "safe 6–10 mg/L",
                current = telemetry.dissolvedOxygen,
                min = 0f, max = 14f, safeMin = 6f, safeMax = 10f,
                modifier = Modifier.weight(1f)
            )
        }

        // ACTUATION CONTROLS
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
                        Text(
                            text = "ACTUATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = BioTextMuted
                        )
                        Text(
                            text = "Bioreactor Controls",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 16.sp
                        )
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = BioTextMuted)
                }

                // LED Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BioSurfaceVariant)
                        .border(1.dp, BioBorderSoft, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = BioWarn)
                        Column {
                            Text("Photonic LED Assist", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("high-intensity bio-stabilization", fontSize = 11.sp, color = BioTextMuted)
                        }
                    }
                    Switch(
                        checked = ledToggleVal,
                        onCheckedChange = { checked ->
                            ledToggleVal = checked
                            scope.launch {
                                repository.sendControl(activeNode, null, checked)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BioBackground,
                            checkedTrackColor = BioLime,
                            uncheckedTrackColor = BioSurface
                        )
                    )
                }

                // Fluidic Pump Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BioSurfaceVariant)
                        .border(1.dp, BioBorderSoft, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = BioCyan)
                            Text("Fluidic Pump Speed", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            "${pumpSliderVal.toInt()}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = BioCyan
                        )
                    }
                    Slider(
                        value = pumpSliderVal,
                        onValueChange = { pumpSliderVal = it },
                        onValueChangeFinished = {
                            scope.launch {
                                repository.sendControl(activeNode, pumpSliderVal.toInt(), null)
                            }
                        },
                        valueRange = 10f..100f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = BioCyan,
                            activeTrackColor = BioCyan,
                            inactiveTrackColor = BioBorderSoft
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10% · idle", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
                        Text("100% · max intake", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
                    }
                }
            }
        }
    }
}

@Composable
fun FiltrationCard(telemetry: TelemetryData) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BioSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BioBorder, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PARTICULATE FILTRATION", style = MaterialTheme.typography.labelSmall)
                    Text("PM2.5 / PM10 Removal", style = MaterialTheme.typography.headlineMedium, fontSize = 15.sp)
                }
                Icon(Icons.Default.FilterList, contentDescription = null, tint = BioTextMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill("Inlet", "${String.format("%.1f", telemetry.pm25In)}", "µg/m³ PM2.5")
                EfficiencyRadialRing(value = telemetry.pmEfficiency, color = BioLime)
                MetricPill("Outlet", "${String.format("%.1f", telemetry.pm25Out)}", "µg/m³ PM2.5")
            }

            AnimatedFlowTrack(color = BioCyan)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryStatBox("PM10 in", "${String.format("%.1f", telemetry.pm10In)} µg/m³", Modifier.weight(1f))
                SecondaryStatBox("PM10 out", "${String.format("%.1f", telemetry.pm10Out)} µg/m³", Modifier.weight(1f))
            }

            Text(
                text = "η_PM = ((PM_in − PM_out) / PM_in) × 100 = ${String.format("%.1f", telemetry.pmEfficiency)}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BioTextMutedDim
            )
        }
    }
}

@Composable
fun Co2FixationCard(telemetry: TelemetryData) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BioSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BioBorder, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GASEOUS EXCHANGE", style = MaterialTheme.typography.labelSmall)
                    Text("Biological CO₂ Fixation", style = MaterialTheme.typography.headlineMedium, fontSize = 15.sp)
                }
                Icon(Icons.Default.Eco, contentDescription = null, tint = BioTextMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill("Inlet", "${telemetry.co2In.toInt()}", "ppm CO₂")
                EfficiencyRadialRing(value = telemetry.co2Efficiency, color = BioCyan)
                MetricPill("Outlet", "${telemetry.co2Out.toInt()}", "ppm CO₂")
            }

            AnimatedFlowTrack(color = BioLime, durationMs = 3200)

            Text(
                text = "η_CO2 = ((CO2_in − CO2_out) / CO2_in) × 100 = ${String.format("%.1f", telemetry.co2Efficiency)}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BioTextMutedDim
            )
        }
    }
}

@Composable
fun MetricPill(title: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BioSurfaceVariant)
            .border(1.dp, BioBorderSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(unit, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
    }
}

@Composable
fun HomeostasisItemCard(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    value: String,
    safeRangeText: String,
    current: Float,
    min: Float,
    max: Float,
    safeMin: Float,
    safeMax: Float,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BioSurface),
        modifier = modifier.border(1.dp, BioBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall)
            }
            Text(value, fontFamily = FontFamily.Monospace, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            HomeostasisSafeZoneBar(
                currentVal = current,
                minVal = min,
                maxVal = max,
                safeMin = safeMin,
                safeMax = safeMax,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(safeRangeText, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
        }
    }
}

@Composable
fun HeaderStatBox(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BioSurface)
            .border(1.dp, BioBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = BioCyan, modifier = Modifier.size(20.dp))
        Column {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
            Text(value, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SecondaryStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BioSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextPrimary)
    }
}

fun formatRuntime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val d = h / 24
    val hh = h % 24
    return "${d}d ${hh}h ${m}m"
}
