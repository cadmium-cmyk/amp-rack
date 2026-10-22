package com.shajikhan.ladspa.amprack.data.model

data class DrumInstrument(
    val id: String,
    val name: String,
    val soundAsset: String,
    val steps: List<Boolean> = List(8) { false }
)

data class DrumPattern(
    val name: String,
    val bpm: Int = 110,
    val tracks: Map<String, List<Boolean>>
)
