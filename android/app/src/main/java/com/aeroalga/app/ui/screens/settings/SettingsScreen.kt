package com.aeroalga.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeroalga.app.data.repository.NodeRepository
import com.aeroalga.app.ui.theme.*

@Composable
fun SettingsScreen(
    repository: NodeRepository,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember { mutableStateOf(repository.backendBaseUrl) }
    var alertOnHypoxia by remember { mutableStateOf(true) }
    var alertOnPhDrift by remember { mutableStateOf(true) }
    var alertOnOverheat by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BioBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "CALIBRATION & MESH CONFIG",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = BioTextPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "ELECTROCHEMICAL PROBES · TELEMETRY INGESTION HUB",
                style = MaterialTheme.typography.labelSmall,
                color = BioTextMuted
            )
        }

        // BACKEND CONNECTION CONFIG
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = BioLime)
                    Text("TELEMETRY INGESTION HUB", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Base URL", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    placeholder = { Text("http://192.168.1.100:8000/", fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BioLime,
                        unfocusedBorderColor = BioBorderSoft
                    )
                )

                Button(
                    onClick = {
                        repository.updateBaseUrl(serverUrl.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BioLime, contentColor = BioBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("UPDATE LINK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SENSOR CALIBRATION WIZARDS
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = BioCyan)
                    Text("ELECTROCHEMICAL SENSOR CALIBRATION", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                MatrixCalibrationActionRow(
                    title = "pH Probe 2-Point Calibration",
                    desc = "Zero offset with standard pH 7.00 buffer",
                    buttonText = "ZERO pH"
                )

                MatrixCalibrationActionRow(
                    title = "MH-Z19B NDIR Zero Reference",
                    desc = "Expose sensors to 400ppm fresh atmosphere",
                    buttonText = "ZERO NDIR"
                )

                MatrixCalibrationActionRow(
                    title = "Turbidity Optical Zeroing",
                    desc = "Zero calibration in pure distilled water (0 NTU)",
                    buttonText = "ZERO NTU"
                )
            }
        }

        // ALARM THRESHOLDS
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BioWarn)
                    Text("HOMEOSTASIS ALERTS & GUARDS", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                MatrixSettingToggleRow("ALARM ON HYPOXIA (DO < 5.0 mg/L)", alertOnHypoxia) { alertOnHypoxia = it }
                MatrixSettingToggleRow("ALARM ON pH DRIFT (OUTSIDE 7.2–8.5)", alertOnPhDrift) { alertOnPhDrift = it }
                MatrixSettingToggleRow("ALARM ON CULTURE OVERHEAT (> 28°C)", alertOnOverheat) { alertOnOverheat = it }
            }
        }
    }
}

@Composable
fun MatrixCalibrationActionRow(title: String, desc: String, buttonText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(desc, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = BioTextMutedDim)
        }
        OutlinedButton(
            onClick = {},
            border = androidx.compose.foundation.BorderStroke(1.dp, BioBorderSoft),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(buttonText, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioCyan)
        }
    }
}

@Composable
fun MatrixSettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = BioTextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BioBackground,
                checkedTrackColor = BioLime,
                uncheckedTrackColor = BioSurfaceVariant
            )
        )
    }
}
