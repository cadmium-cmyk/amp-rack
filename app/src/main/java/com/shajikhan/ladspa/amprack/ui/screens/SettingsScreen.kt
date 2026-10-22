package com.shajikhan.ladspa.amprack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shajikhan.ladspa.amprack.ui.AmpRackViewModel
import com.shajikhan.ladspa.amprack.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AmpRackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isLowLatency by viewModel.audioEngine.isLowLatency.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var accountDeletedMessage by remember { mutableStateOf(false) }
    
    var showDeviceDialog by remember { mutableStateOf(false) }
    var isSelectingInput by remember { mutableStateOf(true) }
    
    val selectedInput by viewModel.audioEngine.selectedInputDevice.collectAsStateWithLifecycle()
    val selectedOutput by viewModel.audioEngine.selectedOutputDevice.collectAsStateWithLifecycle()
    val inputDevices by viewModel.audioEngine.inputDevices.collectAsStateWithLifecycle()
    val outputDevices by viewModel.audioEngine.outputDevices.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Theme Skinning
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("theme_selector_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Visual Theme & Skin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Switch between classic analog amp and modern studio skins",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTheme.values().take(3).forEach { theme ->
                            ThemeItem(
                                theme = theme,
                                isSelected = currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTheme.values().drop(3).forEach { theme ->
                            ThemeItem(
                                theme = theme,
                                isSelected = currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Audio System Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Audio Engine Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Low Latency AAudio Stream", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("Exclusive hardware audio buffer mode", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isLowLatency,
                            onCheckedChange = { viewModel.audioEngine.toggleLowLatency() },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("settings_low_latency_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Input Device", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("Select guitar input", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        TextButton(onClick = { 
                            viewModel.audioEngine.refreshDevices()
                            showDeviceDialog = true
                            isSelectingInput = true
                        }) {
                            Text(
                                text = selectedInput?.productName?.toString() ?: "Phone Mic",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Output Device", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("Select monitoring output", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        TextButton(onClick = { 
                            viewModel.audioEngine.refreshDevices()
                            showDeviceDialog = true
                            isSelectingInput = false
                        }) {
                            Text(
                                text = selectedOutput?.productName?.toString() ?: "Speaker",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Account & Privacy Data Management (Porting DeleteAccount / DeleteData)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Data & Privacy Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Manage your saved presets, recorded takes and app privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDataDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB74D))
                            ),
                            modifier = Modifier.weight(1f).testTag("delete_data_btn")
                        ) {
                            Text("Clear Data", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF5350))
                            ),
                            modifier = Modifier.weight(1f).testTag("delete_account_btn")
                        ) {
                            Text("Delete Account", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // About & Links
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About Amp Rack",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Version 5.95-Raptor • Audio Effects Processor & LADSPA Host",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Open Source Guitar & Audio Effects Processor for Android with 200+ effects, drum sequencer, real-time tuner and zero latency monitoring.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/djshaji"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.shaji.in"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Music", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Clear Data Dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Clear All User Data?") },
            text = { Text("This will reset your custom presets and clear local recordings.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearRack()
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("Clear Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { Text("The following data will be permanently deleted:\n• Custom presets and tones\n• User profile information\n• Saved session files") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearRack()
                        showDeleteAccountDialog = false
                        accountDeletedMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    modifier = Modifier.testTag("confirm_delete_account_btn")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Device Selection Bottom Sheet
    if (showDeviceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDeviceDialog = false },
            containerColor = Color(0xFF191B22)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isSelectingInput) "Select Input Device" else "Select Output Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    val devices = if (isSelectingInput) inputDevices else outputDevices
                    items(devices) { device ->
                        ListItem(
                            headlineContent = { Text(device.productName.toString(), color = Color.White) },
                            modifier = Modifier
                                .clickable {
                                    if (isSelectingInput) viewModel.audioEngine.setInputDevice(device)
                                    else viewModel.audioEngine.setOutputDevice(device)
                                    showDeviceDialog = false
                                }
                                .background(Color(0xFF222530))
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ThemeItem(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14161E))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.accentColor else Color(0x22FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(theme.accentColor)
            )
            Text(
                text = theme.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.LightGray
            )
        }
    }
}
