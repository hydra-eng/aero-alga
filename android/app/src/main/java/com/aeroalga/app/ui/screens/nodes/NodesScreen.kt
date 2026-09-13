package com.aeroalga.app.ui.screens.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeroalga.app.data.model.NodeDevice
import com.aeroalga.app.data.repository.NodeRepository
import com.aeroalga.app.ui.theme.*

@Composable
fun NodesScreen(
    repository: NodeRepository,
    modifier: Modifier = Modifier
) {
    val nodes by repository.nodes.collectAsState()
    val activeNodeId by repository.activeNodeId.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BioBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BioLime,
                contentColor = BioBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Node")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "AeroAlga Fleet",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BioTextPrimary
                )
                Text(
                    text = "REGISTERED HARDWARE NODES · ACTIVE MESH",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioTextMuted
                )
            }

            if (nodes.isEmpty()) {
                // Fallback display if network has not discovered nodes yet
                val defaultNodes = listOf(
                    NodeDevice("node-01", "Bioreactor Alpha", "Main Atrium", "ONLINE", "192.168.1.101", "2.0.0-esp32"),
                    NodeDevice("node-02", "Bioreactor Beta", "Research Lab 3", "ONLINE", "192.168.1.102", "2.0.0-esp32")
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(defaultNodes) { node ->
                        NodeCardItem(node, activeNodeId == node.id) {
                            repository.switchNode(node.id)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(nodes) { node ->
                        NodeCardItem(node, activeNodeId == node.id) {
                            repository.switchNode(node.id)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newId by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }
        var newLoc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Connect New AeroAlga Node", style = MaterialTheme.typography.headlineMedium, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newId,
                        onValueChange = { newId = it },
                        label = { Text("Node ID (e.g. node-03)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BioLime,
                            unfocusedBorderColor = BioBorderSoft
                        )
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Friendly Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BioLime,
                            unfocusedBorderColor = BioBorderSoft
                        )
                    )
                    OutlinedTextField(
                        value = newLoc,
                        onValueChange = { newLoc = it },
                        label = { Text("Location") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BioLime,
                            unfocusedBorderColor = BioBorderSoft
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newId.isNotBlank()) {
                            repository.switchNode(newId.trim())
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Connect", color = BioLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = BioTextMuted)
                }
            },
            containerColor = BioSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NodeCardItem(node: NodeDevice, isActive: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) BioSurfaceVariant else BioSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) BioLime else BioBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BioBackground)
                        .border(1.dp, BioBorderSoft, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isActive) BioLime else BioCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(node.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("${node.id} · ${node.location}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = BioTextMuted)
                    Text("IP: ${node.ipAddress} · FW: ${node.firmwareVersion}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = BioTextMutedDim)
                }
            }

            if (isActive) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = BioLime,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (node.status == "ONLINE") BioLime else BioWarn)
                )
            }
        }
    }
}
