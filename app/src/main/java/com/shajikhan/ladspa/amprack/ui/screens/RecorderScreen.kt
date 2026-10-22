package com.shajikhan.ladspa.amprack.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shajikhan.ladspa.amprack.ui.AmpRackViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecorderScreen(
    viewModel: AmpRackViewModel,
    modifier: Modifier = Modifier
) {
    val isRecording by viewModel.audioEngine.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.audioEngine.recordingDuration.collectAsStateWithLifecycle()
    val recordings by viewModel.audioEngine.recordings.collectAsStateWithLifecycle()

    var selectedFormat by remember { mutableStateOf("WAV 24-bit") }

    // Waveform pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("recorder_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recording Studio Deck
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191B24)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color(0xFFFF1744) else Color.DarkGray)
                        )
                        Text(
                            text = if (isRecording) "RECORDING IN PROGRESS" else "STANDBY RECORDER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) Color(0xFFFF1744) else Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = selectedFormat,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                // Digital Timer
                val minutes = recordingDuration / 60
                val seconds = recordingDuration % 60
                val timerText = String.format("%02d:%02d", minutes, seconds)
                Text(
                    text = timerText,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                // Animated Waveform Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF13151D))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.5f, 0.8f, 0.2f, 0.95f, 0.6f, 0.4f, 0.85f, 0.35f, 0.75f, 0.5f, 0.9f)
                    heights.forEachIndexed { idx, h ->
                        val currentH = if (isRecording) (h * waveScale).coerceIn(0.15f, 1f) else 0.15f
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight(currentH)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isRecording) MaterialTheme.colorScheme.primary else Color(0x33FFFFFF))
                        )
                    }
                }

                // Record Button
                Button(
                    onClick = { viewModel.audioEngine.toggleRecording() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF5350) else Color(0xFFFF1744)
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(0.7f)
                        .testTag("toggle_record_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "STOP RECORDING" else "START RECORDING",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Format Options
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WAV 24-bit", "MP3 320k", "Opus HD").forEach { format ->
                        FilterChip(
                            selected = format == selectedFormat,
                            onClick = { selectedFormat = format },
                            label = { Text(format, fontSize = 11.sp) },
                            enabled = !isRecording
                        )
                    }
                }
            }
        }

        // Saved Takes List
        Text(
            text = "Recorded Takes (${recordings.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded tracks yet. Hit the record button above while jamming!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("recordings_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordings, key = { it.id }) { take ->
                    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(take.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("take_card_${take.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                        shape = RoundedCornerShape(10.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF282C3A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(take.name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("${take.durationSeconds}s • ${take.format} • $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
