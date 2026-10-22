package com.shajikhan.ladspa.amprack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.shajikhan.ladspa.amprack.ui.AmpRackViewModel

@Composable
fun DrumMachineScreen(
    viewModel: AmpRackViewModel,
    modifier: Modifier = Modifier
) {
    val drumInstruments by viewModel.drumInstruments.collectAsStateWithLifecycle()
    val isDrumPlaying by viewModel.audioEngine.isDrumPlaying.collectAsStateWithLifecycle()
    val currentStep by viewModel.audioEngine.currentStep.collectAsStateWithLifecycle()
    val drumBpm by viewModel.audioEngine.drumBpm.collectAsStateWithLifecycle()

    val isBackingPlaying by viewModel.audioEngine.isBackingTrackPlaying.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .testTag("drum_machine_screen")
    ) {
        // Top Drum Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D25)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DRUM MACHINE & SEQUENCER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "8-Step Multi-Track Rhythm Loop",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val pattern = drumInstruments.associate { it.id to it.steps }
                            viewModel.audioEngine.toggleDrumMachine(pattern)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isDrumPlaying) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("drum_play_button")
                    ) {
                        Icon(
                            if (isDrumPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isDrumPlaying) "STOP" else "START LOOP", fontWeight = FontWeight.Bold)
                    }
                }

                // BPM Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TEMPO:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.audioEngine.setDrumBpm(drumBpm - 5) },
                        modifier = Modifier.size(32.dp).testTag("bpm_minus")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease BPM", tint = Color.LightGray)
                    }
                    Text(
                        text = "$drumBpm BPM",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(65.dp)
                    )
                    IconButton(
                        onClick = { viewModel.audioEngine.setDrumBpm(drumBpm + 5) },
                        modifier = Modifier.size(32.dp).testTag("bpm_plus")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase BPM", tint = Color.LightGray)
                    }
                    Slider(
                        value = drumBpm.toFloat(),
                        onValueChange = { viewModel.audioEngine.setDrumBpm(it.toInt()) },
                        valueRange = 60f..220f,
                        modifier = Modifier.weight(1f).testTag("bpm_slider")
                    )
                }

                // Pattern Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rock Standard", "Funk Groove", "Blues Shuffle").forEach { preset ->
                        OutlinedButton(
                            onClick = { viewModel.applyDrumPreset(preset) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("preset_$preset")
                        ) {
                            Text(preset, fontSize = 11.sp)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.clearDrumPattern() },
                        modifier = Modifier.height(32.dp).testTag("clear_drum_pattern")
                    ) {
                        Text("Clear All", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Drum Matrix Grid
        Text(
            text = "Step Sequencer Matrix",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("drum_matrix_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(drumInstruments, key = { it.id }) { instrument ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF20232E))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = instrument.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(90.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (step in 0 until 8) {
                            val isActive = instrument.steps.getOrElse(step) { false }
                            val isCurrentPlayhead = isDrumPlaying && currentStep == step

                            val cellColor = when {
                                isActive && isCurrentPlayhead -> Color(0xFF00E676)
                                isActive -> MaterialTheme.colorScheme.primary
                                isCurrentPlayhead -> Color(0x44FFFFFF)
                                else -> Color(0xFF14161E)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellColor)
                                    .border(
                                        width = if (isCurrentPlayhead) 2.dp else 1.dp,
                                        color = if (isCurrentPlayhead) Color.White else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        viewModel.toggleDrumStep(instrument.id, step)
                                    }
                                    .testTag("step_${instrument.id}_$step")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Backing Track Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backing_track_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.audioEngine.toggleBackingTrack() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("toggle_backing_track_button")
                    ) {
                        Icon(
                            if (isBackingPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Backing Track",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Guitar Backing Track Loop",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBackingPlaying) "Playing high-fidelity demo track (Looping)" else "Practice chords, solos & scales",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
