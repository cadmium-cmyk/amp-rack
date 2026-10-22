package com.shajikhan.ladspa.amprack.data.model

import androidx.compose.ui.graphics.Color
import com.shajikhan.ladspa.amprack.ui.theme.*

data class PluginInfo(
    val id: Int,
    val name: String,
    val description: String = "",
    val category: String = "Guitar Effects",
    val library: String = "",
    val defaultParameters: List<PluginParameter> = emptyList()
)

data class PluginParameter(
    val id: String,
    val name: String,
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val defaultValue: Float = 50f,
    val currentValue: Float = 50f,
    val unit: String = "%",
    val isToggle: Boolean = false,
    val options: List<String>? = null
)

data class ActivePlugin(
    val instanceId: String,
    val pluginInfo: PluginInfo,
    val isBypassed: Boolean = false,
    val parameters: List<PluginParameter>,
    val color: Color = PedalDistortion
)

fun getPluginColor(category: String): Color {
    return when (category.lowercase()) {
        "distortion", "fuzz" -> PedalDistortion
        "overdrive", "tube" -> PedalOverdrive
        "delay", "echo" -> PedalDelay
        "reverb", "ambience" -> PedalReverb
        "chorus" -> PedalChorus
        "flanger", "phaser", "vibrato" -> PedalFlanger
        "amplifier", "amp", "preamp" -> PedalAmp
        "cabinet", "cab" -> PedalCabinet
        "compressor", "limiter" -> PedalCompressor
        "equalizer", "eq" -> PedalEQ
        else -> PedalOverdrive
    }
}
