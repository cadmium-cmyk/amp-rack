package com.shajikhan.ladspa.amprack.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shajikhan.ladspa.amprack.ui.AmpRackViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GuitarString(val name: String, val note: String, val freqHz: Float)

val STANDARD_STRINGS = listOf(
    GuitarString("6th", "E", 82.41f),
    GuitarString("5th", "A", 110.00f),
    GuitarString("4th", "D", 146.83f),
    GuitarString("3rd", "G", 196.00f),
    GuitarString("2nd", "B", 246.94f),
    GuitarString("1st", "e", 329.63f)
)

@Composable
fun TunerScreen(
    viewModel: AmpRackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTunerNote by viewModel.audioEngine.currentTunerNote.collectAsStateWithLifecycle()
    var selectedString by remember { mutableStateOf(STANDARD_STRINGS[0]) }

    val statusColor by animateColorAsState(
        targetValue = when {
            currentTunerNote.isInTune -> Color(0xFF00E676)
            currentTunerNote.centsDeviation < 0 -> Color(0xFFFF9100)
            else -> Color(0xFFFF5252)
        },
        label = "tuner_status_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("tuner_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CHROMATIC GUITAR TUNER",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Standard Tuning (A4 = 440 Hz)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Center Gauge & Note Dial
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191B24)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Note Display
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222533))
                        .border(3.dp, statusColor, CircleShape)
                        .testTag("tuner_note_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentTunerNote.noteName,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                        Text(
                            text = String.format("%.1f Hz", currentTunerNote.detectedFreq),
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Cents Status Label
                Text(
                    text = when {
                        currentTunerNote.isInTune -> "IN TUNE"
                        currentTunerNote.centsDeviation < 0 -> "FLAT (${currentTunerNote.centsDeviation.toInt()} cents)"
                        else -> "SHARP (+${currentTunerNote.centsDeviation.toInt()} cents)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Needle Gauge Arc
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val centerY = height

                    // Center tick (0 cents)
                    drawLine(
                        color = Color(0xFF00E676),
                        start = Offset(centerX, centerY - 45.dp.toPx()),
                        end = Offset(centerX, centerY - 15.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Side markers (-50 to +50)
                    for (c in -50..50 step 10) {
                        if (c == 0) continue
                        val fraction = (c + 50) / 100f
                        val x = width * fraction
                        drawLine(
                            color = Color(0x55FFFFFF),
                            start = Offset(x, centerY - 30.dp.toPx()),
                            end = Offset(x, centerY - 15.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Indicator needle
                    val needleFraction = ((currentTunerNote.centsDeviation.coerceIn(-50f, 50f) + 50f) / 100f)
                    val needleX = width * needleFraction
                    drawLine(
                        color = statusColor,
                        start = Offset(needleX, centerY - 50.dp.toPx()),
                        end = Offset(centerX, centerY),
                        strokeWidth = 4.dp.toPx()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-50b", fontSize = 11.sp, color = Color.Gray)
                    Text("0", fontSize = 11.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    Text("+50#", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // String Selectors & Reference Tone
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select String to Tune",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                STANDARD_STRINGS.forEach { guitarString ->
                    val isSelected = selectedString.name == guitarString.name
                    OutlinedButton(
                        onClick = {
                            selectedString = guitarString
                            viewModel.audioEngine.selectTunerString(guitarString.note, guitarString.freqHz)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color(0xFF222530),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33FFFFFF)
                            )
                        ),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("string_btn_${guitarString.note}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(guitarString.note, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(guitarString.name, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Play reference tone button
            FilledTonalButton(
                onClick = {
                    viewModel.audioEngine.playReferenceTone(selectedString.freqHz)
                },
                modifier = Modifier.testTag("play_ref_tone_button")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Reference Tone (${selectedString.note}: ${selectedString.freqHz.toInt()}Hz)")
            }
        }
    }
}
