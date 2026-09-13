package com.aeroalga.app.ui.screens.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.aeroalga.app.ui.components.DotMatrixBar
import com.aeroalga.app.ui.components.DotMatrixDisplay
import com.aeroalga.app.ui.components.DotMatrixGauge
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
        // TOP HARDWARE MARQUEE TICKER BANNER
        DotMatrixTickerBanner(activeNode = activeNode, isConnected = isConnected)

        // HERO HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AEROALGA v2.0",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = BioTextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "BDPA-v2 · MATRIX TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioTextMuted
                )
            }

            // Runtime Display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SYSTEM RUNTIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioTextMutedDim
                )
                DotMatrixDisplay(
                    text = formatRuntime(telemetry.runtimeSec),
                    activeColor = BioCyan,
                    dotRadius = 1.1.dp,
                    dotSpacing = 0.8.dp,
                    showInactiveDots = false
                )
            }
        }

        // AERATION FLOW & PUMP STATS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MatrixHeaderStatBox(
                icon = Icons.Default.Air,
                label = "Aeration Flow",
                valueText = String.format("%.1f", telemetry.flowRate),
                unit = "L/min",
                color = BioCyan,
                modifier = Modifier.weight(1f)
            )
            MatrixHeaderStatBox(
                icon = Icons.Default.Speed,
                label = "Pump PWM",
                valueText = "${telemetry.pumpSpeedPct}%",
                unit = "5kHz",
                color = BioLime,
                modifier = Modifier.weight(1f)
            )
        }

        // PARTICULATE FILTRATION (PMS5003 DUAL UART)
        MatrixFiltrationCard(telemetry)

        // CO2 FIXATION (MH-Z19B NDIR)
        MatrixCo2Card(telemetry)

        // BIOLOGICAL HOMEOSTASIS (DOT MATRIX LED METERS)
        Text(
            text = "HOMEOSTASIS MATRIX · LIVE CULTURE",
            style = MaterialTheme.typography.labelSmall,
            color = BioTextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MatrixHomeostasisCard(
                icon = Icons.Default.Science,
                iconColor = BioLime,
                title = "pH Level",
                valueString = String.format("%.2f", telemetry.ph),
                safeRangeText = "safe 7.2–8.5",
                current = telemetry.ph,
                min = 4f, max = 12f, safeMin = 7.2f, safeMax = 8.5f,
                defaultColor = BioLime,
                modifier = Modifier.weight(1f)
            )
            MatrixHomeostasisCard(
                icon = Icons.Default.Thermostat,
                iconColor = BioCyan,
                title = "Water Temp",
                valueString = "${String.format("%.1f", telemetry.waterTemp)}C",
                safeRangeText = "safe 20–28°C",
                current = telemetry.waterTemp,
                min = 10f, max = 38f, safeMin = 20f, safeMax = 28f,
                defaultColor = BioCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MatrixHomeostasisCard(
                icon = Icons.Default.WaterDrop,
                iconColor = BioCyan,
                title = "Turbidity",
                valueString = "${telemetry.turbidityNtu.toInt()}",
                safeRangeText = "biomass NTU",
                current = telemetry.turbidityNtu,
                min = 0f, max = 600f, safeMin = 150f, safeMax = 450f,
                defaultColor = BioLime,
                modifier = Modifier.weight(1f)
            )
            MatrixHomeostasisCard(
                icon = Icons.Default.Waves,
                iconColor = BioLime,
                title = "Dissolved O₂",
                valueString = "${String.format("%.1f", telemetry.dissolvedOxygen)}",
                safeRangeText = "safe 6–10 mg/L",
                current = telemetry.dissolvedOxygen,
                min = 0f, max = 14f, safeMin = 6f, safeMax = 10f,
                defaultColor = BioLime,
                modifier = Modifier.weight(1f)
            )
        }

        // ACTUATION HARDWARE CONTROLS
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
                            text = "ACTUATION MATRIX",
                            style = MaterialTheme.typography.labelSmall,
                            color = BioTextMuted
                        )
                        Text(
                            text = "Bioreactor Controls",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = BioTextMuted)
                }

                // LED Assist Switch
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
                            Text("660nm/450nm PAR stabilized", fontSize = 11.sp, color = BioTextMuted)
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
                        DotMatrixDisplay(
                            text = "${pumpSliderVal.toInt()}%",
                            activeColor = BioCyan,
                            dotRadius = 1.3.dp,
                            dotSpacing = 0.8.dp,
                            showInactiveDots = false
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
                        Text("10% · IDLE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
                        Text("100% · MAX INTAKE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
                    }
                }
            }
        }
    }
}

@Composable
fun DotMatrixTickerBanner(activeNode: String, isConnected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BioSurface)
            .border(1.dp, BioBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) BioLime else BioWarn)
            )
            Text(
                text = activeNode.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BioLime,
                letterSpacing = 1.sp
            )
            Text(
                text = "· 2.4GHz MESH",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BioTextMutedDim
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BioSurfaceVariant)
                .border(1.dp, BioBorderSoft, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isConnected) "WS LINKED" else "OFFLINE",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isConnected) BioCyan else BioWarn
            )
        }
    }
}

@Composable
fun MatrixFiltrationCard(telemetry: TelemetryData) {
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
                    Text("PARTICULATE DYNAMICS", style = MaterialTheme.typography.labelSmall)
                    Text("PM2.5 / PM10 Removal", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Icon(Icons.Default.FilterList, contentDescription = null, tint = BioTextMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatrixMetricPill(
                    title = "Inlet",
                    valueString = String.format("%.1f", telemetry.pm25In),
                    unit = "ug/m3 PM2.5",
                    activeColor = BioWarn
                )

                DotMatrixGauge(
                    percentage = telemetry.pmEfficiency,
                    label = "n_PM",
                    activeColor = BioLime
                )

                MatrixMetricPill(
                    title = "Outlet",
                    valueString = String.format("%.1f", telemetry.pm25Out),
                    unit = "ug/m3 PM2.5",
                    activeColor = BioLime
                )
            }

            AnimatedFlowTrack(color = BioCyan)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MatrixSecondaryStat("PM10 IN", "${String.format("%.1f", telemetry.pm10In)} ug/m3", Modifier.weight(1f))
                MatrixSecondaryStat("PM10 OUT", "${String.format("%.1f", telemetry.pm10Out)} ug/m3", Modifier.weight(1f))
            }

            Text(
                text = "n_PM = ((PM_in - PM_out) / PM_in) * 100 = ${String.format("%.1f", telemetry.pmEfficiency)}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BioTextMutedDim
            )
        }
    }
}

@Composable
fun MatrixCo2Card(telemetry: TelemetryData) {
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
                    Text("CARBON FIXATION MATRIX", style = MaterialTheme.typography.labelSmall)
                    Text("Biological CO2 Fixation", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Icon(Icons.Default.Eco, contentDescription = null, tint = BioTextMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatrixMetricPill(
                    title = "Inlet",
                    valueString = "${telemetry.co2In.toInt()}",
                    unit = "PPM CO2",
                    activeColor = BioWarn
                )

                DotMatrixGauge(
                    percentage = telemetry.co2Efficiency,
                    label = "n_CO2",
                    activeColor = BioCyan
                )

                MatrixMetricPill(
                    title = "Outlet",
                    valueString = "${telemetry.co2Out.toInt()}",
                    unit = "PPM CO2",
                    activeColor = BioCyan
                )
            }

            AnimatedFlowTrack(color = BioLime, durationMs = 3200)

            Text(
                text = "Delta = ${telemetry.co2In.toInt() - telemetry.co2Out.toInt()} PPM NET CAPTURE",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = BioTextMutedDim
            )
        }
    }
}

@Composable
fun MatrixMetricPill(
    title: String,
    valueString: String,
    unit: String,
    activeColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BioSurfaceVariant)
            .border(1.dp, BioBorderSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        DotMatrixDisplay(
            text = valueString,
            activeColor = activeColor,
            dotRadius = 1.3.dp,
            dotSpacing = 0.9.dp,
            showInactiveDots = false
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(unit, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
    }
}

@Composable
fun MatrixHomeostasisCard(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    valueString: String,
    safeRangeText: String,
    current: Float,
    min: Float,
    max: Float,
    safeMin: Float,
    safeMax: Float,
    defaultColor: androidx.compose.ui.graphics.Color,
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

            DotMatrixDisplay(
                text = valueString,
                activeColor = defaultColor,
                dotRadius = 1.3.dp,
                dotSpacing = 0.9.dp,
                showInactiveDots = false
            )

            DotMatrixBar(
                value = current,
                min = min,
                max = max,
                safeMin = safeMin,
                safeMax = safeMax,
                defaultColor = defaultColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(safeRangeText, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = BioTextMutedDim)
        }
    }
}

@Composable
fun MatrixHeaderStatBox(
    icon: ImageVector,
    label: String,
    valueText: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BioSurface)
            .border(1.dp, BioBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Column {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DotMatrixDisplay(
                    text = valueText,
                    activeColor = color,
                    dotRadius = 1.3.dp,
                    dotSpacing = 0.9.dp,
                    showInactiveDots = false
                )
                Text(unit, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = BioTextMutedDim)
            }
        }
    }
}

@Composable
fun MatrixSecondaryStat(label: String, value: String, modifier: Modifier = Modifier) {
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
    val s = sec % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}
