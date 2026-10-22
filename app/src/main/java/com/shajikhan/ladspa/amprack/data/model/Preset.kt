package com.shajikhan.ladspa.amprack.data.model

data class Preset(
    val id: String,
    val name: String,
    val description: String,
    val author: String = "Amp Rack",
    val isFactory: Boolean = true,
    val plugins: List<PresetPlugin>
)

data class PresetPlugin(
    val pluginId: Int,
    val pluginName: String,
    val category: String,
    val isBypassed: Boolean = false,
    val parameterValues: Map<String, Float> = emptyMap()
)
