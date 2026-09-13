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
    var showSavedSnackbar by remember { mutableStateOf(false) }

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
                text = "System Settings & Calibration",
                style = MaterialTheme.typography.headlineMedium,
                color = BioTextPrimary
            )
            Text(
                text = "COMMUNICATION · SENSOR CALIBRATION · ALERTS",
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
                    Text("Backend Hub Host", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Base URL") },
                    placeholder = { Text("http://192.168.1.100:8000/") },
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
                        showSavedSnackbar = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BioLime, contentColor = BioBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update Connection", fontWeight = FontWeight.Bold)
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
                    Text("Electrochemical Calibration", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                CalibrationActionRow(
                    title = "pH Probe 2-Point Calibration",
                    desc = "Zero offset with standard pH 7.00 buffer",
                    buttonText = "Calibrate pH"
                )

                CalibrationActionRow(
                    title = "MH-Z19B NDIR Zero Point",
                    desc = "Expose inlet/outlet sensors to 400ppm fresh air",
                    buttonText = "Zero NDIR"
                )

                CalibrationActionRow(
                    title = "Turbidity Optical Zero",
                    desc = "Place sensor in pure distilled water (0 NTU reference)",
                    buttonText = "Zero NTU"
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
                    Text("Biological Threshold Alarms", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                SettingToggleRow("Alarm on Hypoxia (DO < 5.0 mg/L)", alertOnHypoxia) { alertOnHypoxia = it }
                SettingToggleRow("Alarm on pH Drift (outside 7.2–8.5)", alertOnPhDrift) { alertOnPhDrift = it }
                SettingToggleRow("Alarm on Overheat (Temp > 28°C)", alertOnOverheat) { alertOnOverheat = it }
            }
        }
    }
}

@Composable
fun CalibrationActionRow(title: String, desc: String, buttonText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(desc, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = BioTextMutedDim)
        }
        OutlinedButton(
            onClick = {},
            border = androidx.compose.foundation.BorderStroke(1.dp, BioBorderSoft),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonText, fontSize = 11.sp, color = BioTextPrimary)
        }
    }
}

@Composable
fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = BioTextPrimary)
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
