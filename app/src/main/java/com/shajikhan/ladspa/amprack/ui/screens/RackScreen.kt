package com.shajikhan.ladspa.amprack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shajikhan.ladspa.amprack.data.model.PluginInfo
import com.shajikhan.ladspa.amprack.ui.AmpRackViewModel
import com.shajikhan.ladspa.amprack.ui.components.PedalCard
import com.shajikhan.ladspa.amprack.ui.components.VuMeter
import com.shajikhan.ladspa.amprack.ui.theme.LedOff
import com.shajikhan.ladspa.amprack.ui.theme.LedOnGreen

@Composable
fun RackScreen(
    viewModel: AmpRackViewModel,
    onNavigateToTuner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlugins by viewModel.activePlugins.collectAsStateWithLifecycle()
    val availablePlugins by viewModel.availablePlugins.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val selectedPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()

    val inputLevel by viewModel.audioEngine.inputLevel.collectAsStateWithLifecycle()
    val outputLevel by viewModel.audioEngine.outputLevel.collectAsStateWithLifecycle()
    val isBypassed by viewModel.audioEngine.isBypassed.collectAsStateWithLifecycle()
    val isLowLatency by viewModel.audioEngine.isLowLatency.collectAsStateWithLifecycle()
    val inputGain by viewModel.audioEngine.inputGain.collectAsStateWithLifecycle()
    val masterVolume by viewModel.audioEngine.masterVolume.collectAsStateWithLifecycle()

    var showPluginSheet by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetDescInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("rack_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPluginSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Effect") },
                text = { Text("Add Effect") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_effect_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Master Rack Console
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("master_rack_console"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A22)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Top Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (!isBypassed) LedOnGreen else LedOff)
                            )
                            Text(
                                text = "MASTER CONSOLE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isBypassed) Color.White else Color.Gray,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isLowLatency,
                                onClick = { viewModel.audioEngine.toggleLowLatency() },
                                label = { Text("Low Latency", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("low_latency_toggle")
                            )

                            Button(
                                onClick = { viewModel.audioEngine.toggleBypass() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBypassed) Color(0xFFEF5350) else Color(0xFF2E7D32)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("master_bypass_button")
                            ) {
                                Text(
                                    text = if (isBypassed) "BYPASSED" else "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // VU Meters and Master Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VuMeter(label = "INPUT", level = inputLevel, modifier = Modifier.testTag("input_vu"))
                            VuMeter(label = "OUTPUT", level = outputLevel, modifier = Modifier.testTag("output_vu"))
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("In Gain", fontSize = 10.sp, color = Color.Gray)
                                Text("${(inputGain * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Master Vol", fontSize = 10.sp, color = Color.Gray)
                                Text("${(masterVolume * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                            }
                            IconButton(
                                onClick = onNavigateToTuner,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF252834))
                                    .testTag("open_tuner_button")
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Tuner",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Presets Horizontal Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tone Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = { showSavePresetDialog = true },
                        modifier = Modifier.testTag("save_preset_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Preset", fontSize = 12.sp)
                    }
                    if (activePlugins.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearRack() },
                            modifier = Modifier.testTag("clear_rack_button")
                        ) {
                            Text("Clear", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Presets Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = selectedPreset?.id == preset.id
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.loadPreset(preset) },
                        label = { Text(preset.name, fontSize = 12.sp) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E212B),
                            labelColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("preset_chip_${preset.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Pedal Chain
            if (activePlugins.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                        .testTag("empty_rack_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Effect Rack is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Add pedals, tube amps, reverbs or load a tone preset above to start shaping your sound.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { showPluginSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("empty_add_effect_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse 200+ Effects")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .testTag("active_rack_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                ) {
                    itemsIndexed(activePlugins, key = { _, plugin -> plugin.instanceId }) { index, plugin ->
                        PedalCard(
                            activePlugin = plugin,
                            canMoveUp = index > 0,
                            canMoveDown = index < activePlugins.size - 1,
                            onMoveUp = { viewModel.movePluginUp(index) },
                            onMoveDown = { viewModel.movePluginDown(index) },
                            onToggleBypass = { viewModel.togglePluginBypass(plugin.instanceId) },
                            onDelete = { viewModel.removePluginFromRack(plugin.instanceId) },
                            onUpdateParam = { paramId, value ->
                                viewModel.updatePluginParameter(plugin.instanceId, paramId, value)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Plugin Bottom Sheet
    if (showPluginSheet) {
        PluginSelectorSheet(
            plugins = availablePlugins,
            onSelectPlugin = { plugin ->
                viewModel.addPluginToRack(plugin)
            },
            onDismiss = { showPluginSheet = false }
        )
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Tone Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Save current chain (${activePlugins.size} effects) as a custom preset.")
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                    OutlinedTextField(
                        value = presetDescInput,
                        onValueChange = { presetDescInput = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth().testTag("preset_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            viewModel.saveCurrentAsPreset(presetNameInput.trim(), presetDescInput.trim())
                            presetNameInput = ""
                            presetDescInput = ""
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetNameInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_save_preset_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
