package com.shajikhan.ladspa.amprack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shajikhan.ladspa.amprack.data.model.ActivePlugin
import com.shajikhan.ladspa.amprack.ui.theme.LedOff
import com.shajikhan.ladspa.amprack.ui.theme.LedOnGreen

@Composable
fun PedalCard(
    activePlugin: ActivePlugin,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleBypass: () -> Unit,
    onDelete: () -> Unit,
    onUpdateParam: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pedal_${activePlugin.instanceId}")
            .border(
                width = 1.dp,
                color = if (!activePlugin.isBypassed) activePlugin.color.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1D25)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                activePlugin.color.copy(alpha = if (activePlugin.isBypassed) 0.15f else 0.4f),
                                Color(0xFF1E212B)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // LED
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (!activePlugin.isBypassed) LedOnGreen else LedOff)
                    )
                    Column {
                        Text(
                            text = activePlugin.pluginInfo.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!activePlugin.isBypassed) Color.White else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activePlugin.pluginInfo.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = activePlugin.color,
                            fontSize = 10.sp
                        )
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(32.dp).testTag("move_up_${activePlugin.instanceId}")
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (canMoveUp) Color.LightGray else Color(0x33FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(32.dp).testTag("move_down_${activePlugin.instanceId}")
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (canMoveDown) Color.LightGray else Color(0x33FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleBypass,
                        modifier = Modifier.size(32.dp).testTag("bypass_${activePlugin.instanceId}")
                    ) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = "Bypass Pedal",
                            tint = if (!activePlugin.isBypassed) LedOnGreen else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_${activePlugin.instanceId}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Remove Pedal",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("expand_${activePlugin.instanceId}")
                    ) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle controls",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Controls Body
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activePlugin.parameters.isEmpty()) {
                        Text(
                            text = "No user-adjustable controls for this unit",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        activePlugin.parameters.forEach { param ->
                            PedalKnob(
                                parameter = param,
                                onValueChange = { newVal ->
                                    onUpdateParam(param.id, newVal)
                                },
                                accentColor = activePlugin.color
                            )
                        }
                    }
                }
            }
        }
    }
}
