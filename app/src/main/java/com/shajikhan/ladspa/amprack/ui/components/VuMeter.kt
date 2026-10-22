package com.shajikhan.ladspa.amprack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shajikhan.ladspa.amprack.ui.theme.LedOff
import com.shajikhan.ladspa.amprack.ui.theme.LedOnAmber
import com.shajikhan.ladspa.amprack.ui.theme.LedOnGreen
import com.shajikhan.ladspa.amprack.ui.theme.LedOnRed

@Composable
fun VuMeter(
    label: String,
    level: Float,
    modifier: Modifier = Modifier,
    segments: Int = 12
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .height(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF14151B))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeSegments = (level.coerceIn(0f, 1f) * segments).toInt()
            for (i in 0 until segments) {
                val isActive = i < activeSegments
                val segmentColor = when {
                    i >= segments - 2 -> if (isActive) LedOnRed else LedOff
                    i >= segments - 4 -> if (isActive) LedOnAmber else LedOff
                    else -> if (isActive) LedOnGreen else LedOff
                }
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(segmentColor)
                )
            }
        }
    }
}
