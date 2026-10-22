package com.shajikhan.ladspa.amprack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shajikhan.ladspa.amprack.data.model.PluginParameter

@Composable
fun PedalKnob(
    parameter: PluginParameter,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("param_${parameter.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x22FFFFFF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parameter.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 12.sp
                )
                if (parameter.options != null && parameter.options.isNotEmpty()) {
                    val optIndex = parameter.currentValue.toInt().coerceIn(0, parameter.options.size - 1)
                    Text(
                        text = parameter.options[optIndex],
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontSize = 11.sp
                    )
                } else if (parameter.isToggle) {
                    Text(
                        text = if (parameter.currentValue > 0.5f) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (parameter.currentValue > 0.5f) accentColor else Color.Gray,
                        fontSize = 11.sp
                    )
                } else {
                    val formatted = when {
                        parameter.maxValue - parameter.minValue <= 10f -> String.format("%.1f", parameter.currentValue)
                        else -> parameter.currentValue.toInt().toString()
                    }
                    Text(
                        text = "$formatted ${parameter.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontSize = 11.sp
                    )
                }
            }

            if (parameter.isToggle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Switch(
                        checked = parameter.currentValue > 0.5f,
                        onCheckedChange = { checked ->
                            onValueChange(if (checked) 1f else 0f)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        ),
                        modifier = Modifier.testTag("switch_${parameter.id}")
                    )
                }
            } else if (parameter.options != null && parameter.options.isNotEmpty()) {
                Slider(
                    value = parameter.currentValue,
                    onValueChange = onValueChange,
                    valueRange = 0f..(parameter.options.size - 1).toFloat(),
                    steps = parameter.options.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.testTag("slider_${parameter.id}")
                )
            } else {
                Slider(
                    value = parameter.currentValue,
                    onValueChange = onValueChange,
                    valueRange = parameter.minValue..parameter.maxValue,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.testTag("slider_${parameter.id}")
                )
            }
        }
    }
}
