package com.shajikhan.ladspa.amprack.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shajikhan.ladspa.amprack.data.model.PluginInfo
import com.shajikhan.ladspa.amprack.data.model.PluginParameter
import java.io.InputStreamReader

class PluginRepository(private val context: Context) {
    private val gson = Gson()
    private var cachedPlugins: List<PluginInfo>? = null

    fun getAllPlugins(): List<PluginInfo> {
        cachedPlugins?.let { return it }

        val pluginsList = mutableListOf<PluginInfo>()

        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("all_plugins.json")
            val reader = InputStreamReader(inputStream)
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)

            for (key in jsonObject.keySet()) {
                val item = jsonObject.getAsJsonObject(key)
                val id = if (item.has("id")) item.get("id").asInt else key.toIntOrNull() ?: 1000
                val name = if (item.has("name")) item.get("name").asString else "Plugin $key"
                val desc = if (item.has("desc")) item.get("desc").asString else ""
                val library = if (item.has("library")) item.get("library").asString else ""

                val category = deduceCategory(name, desc, library)
                val params = generateDefaultParameters(name, category)

                pluginsList.add(
                    PluginInfo(
                        id = id,
                        name = name,
                        description = desc,
                        category = category,
                        library = library,
                        defaultParameters = params
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (pluginsList.isEmpty()) {
            pluginsList.addAll(getFallbackPlugins())
        }

        val sorted = pluginsList.sortedBy { it.name }
        cachedPlugins = sorted
        return sorted
    }

    private fun deduceCategory(name: String, desc: String, lib: String): String {
        val text = "$name $desc $lib".lowercase()
        return when {
            text.contains("distortion") || text.contains("fuzz") || text.contains("muff") || text.contains("ds1") -> "Distortion"
            text.contains("overdrive") || text.contains("screamer") || text.contains("ts9") || text.contains("drive") -> "Overdrive"
            text.contains("amp") || text.contains("plexi") || text.contains("jcm") || text.contains("tubedriver") -> "Amplifier"
            text.contains("cab") || text.contains("speaker") || text.contains("impulse") -> "Cabinet"
            text.contains("delay") || text.contains("echo") || text.contains("dub") -> "Delay"
            text.contains("reverb") || text.contains("ambience") || text.contains("zita") || text.contains("plate") -> "Reverb"
            text.contains("chorus") -> "Chorus"
            text.contains("flange") -> "Flanger"
            text.contains("phase") -> "Phaser"
            text.contains("eq") || text.contains("equalizer") || text.contains("tone") -> "Equalizer"
            text.contains("comp") || text.contains("compressor") -> "Compressor"
            text.contains("limit") || text.contains("limiter") -> "Limiter"
            text.contains("gate") || text.contains("denoiser") -> "Noise Gate"
            text.contains("wah") || text.contains("quack") -> "Wah"
            text.contains("pitch") || text.contains("shifter") || text.contains("harm") -> "Pitch"
            text.contains("tremolo") || text.contains("vibe") -> "Modulation"
            else -> "Effects"
        }
    }

    fun generateDefaultParameters(name: String, category: String): List<PluginParameter> {
        val params = mutableListOf<PluginParameter>()
        when (category) {
            "Distortion", "Overdrive" -> {
                params.add(PluginParameter("gain", "Gain / Drive", 0f, 100f, 65f, 65f, "%"))
                params.add(PluginParameter("tone", "Tone", 0f, 100f, 50f, 50f, "%"))
                params.add(PluginParameter("level", "Level / Vol", 0f, 100f, 75f, 75f, "%"))
                params.add(PluginParameter("fat", "Fat Boost", 0f, 1f, 0f, 0f, "", isToggle = true))
            }
            "Amplifier" -> {
                params.add(PluginParameter("gain", "Gain", 0f, 100f, 60f, 60f, "%"))
                params.add(PluginParameter("bass", "Bass", 0f, 100f, 50f, 50f, "%"))
                params.add(PluginParameter("mid", "Middle", 0f, 100f, 55f, 55f, "%"))
                params.add(PluginParameter("treble", "Treble", 0f, 100f, 60f, 60f, "%"))
                params.add(PluginParameter("presence", "Presence", 0f, 100f, 40f, 40f, "%"))
                params.add(PluginParameter("master", "Master Vol", 0f, 100f, 70f, 70f, "%"))
            }
            "Cabinet" -> {
                params.add(PluginParameter("model", "Cabinet Model", 0f, 5f, 1f, 1f, "", options = listOf("Marshall 4x12", "Celestion G12M", "Twin Reverb", "Mesa Boogie", "Vox AC30", "Bassman 4x10")))
                params.add(PluginParameter("mic_distance", "Mic Distance", 0f, 100f, 30f, 30f, "%"))
                params.add(PluginParameter("air", "Air / Room", 0f, 100f, 45f, 45f, "%"))
                params.add(PluginParameter("output", "Cab Level", 0f, 100f, 80f, 80f, "%"))
            }
            "Delay" -> {
                params.add(PluginParameter("time", "Time", 20f, 1200f, 360f, 360f, "ms"))
                params.add(PluginParameter("feedback", "Feedback", 0f, 100f, 45f, 45f, "%"))
                params.add(PluginParameter("mix", "Mix", 0f, 100f, 35f, 35f, "%"))
                params.add(PluginParameter("highcut", "Dampening", 0f, 100f, 30f, 30f, "%"))
            }
            "Reverb" -> {
                params.add(PluginParameter("decay", "Decay Time", 0.1f, 10f, 2.5f, 2.5f, "s"))
                params.add(PluginParameter("predelay", "Pre-Delay", 0f, 200f, 25f, 25f, "ms"))
                params.add(PluginParameter("mix", "Wet / Dry", 0f, 100f, 30f, 30f, "%"))
                params.add(PluginParameter("room_size", "Room Size", 0f, 100f, 60f, 60f, "%"))
                params.add(PluginParameter("damping", "Damping", 0f, 100f, 40f, 40f, "%"))
            }
            "Chorus", "Flanger", "Phaser" -> {
                params.add(PluginParameter("rate", "Rate / Speed", 0.1f, 10f, 1.8f, 1.8f, "Hz"))
                params.add(PluginParameter("depth", "Depth", 0f, 100f, 65f, 65f, "%"))
                params.add(PluginParameter("feedback", "Regen", 0f, 100f, 30f, 30f, "%"))
                params.add(PluginParameter("mix", "Mix", 0f, 100f, 50f, 50f, "%"))
            }
            "Equalizer" -> {
                params.add(PluginParameter("low", "Low (100Hz)", -12f, 12f, 0f, 0f, "dB"))
                params.add(PluginParameter("low_mid", "Low Mid (400Hz)", -12f, 12f, 2f, 2f, "dB"))
                params.add(PluginParameter("mid", "Mid (1kHz)", -12f, 12f, -1f, -1f, "dB"))
                params.add(PluginParameter("high_mid", "High Mid (3kHz)", -12f, 12f, 3f, 3f, "dB"))
                params.add(PluginParameter("high", "High (8kHz)", -12f, 12f, 1f, 1f, "dB"))
            }
            "Compressor", "Limiter" -> {
                params.add(PluginParameter("threshold", "Threshold", -40f, 0f, -18f, -18f, "dB"))
                params.add(PluginParameter("ratio", "Ratio", 1f, 20f, 4f, 4f, ":1"))
                params.add(PluginParameter("attack", "Attack", 1f, 100f, 15f, 15f, "ms"))
                params.add(PluginParameter("release", "Release", 10f, 500f, 120f, 120f, "ms"))
                params.add(PluginParameter("makeup", "Make-Up", 0f, 24f, 4f, 4f, "dB"))
            }
            "Noise Gate" -> {
                params.add(PluginParameter("thresh", "Threshold", -80f, 0f, -50f, -50f, "dB"))
                params.add(PluginParameter("decay", "Decay", 10f, 500f, 60f, 60f, "ms"))
            }
            else -> {
                params.add(PluginParameter("input", "Input", 0f, 100f, 50f, 50f, "%"))
                params.add(PluginParameter("param", "Parameter", 0f, 100f, 50f, 50f, "%"))
                params.add(PluginParameter("output", "Output", 0f, 100f, 50f, 50f, "%"))
            }
        }
        return params
    }

    private fun getFallbackPlugins(): List<PluginInfo> {
        return listOf(
            PluginInfo(8095, "GxAmplifier-JCM800", "Iconic high gain British valve amp emulation", "Amplifier", "libgx_amp.lv2.so"),
            PluginInfo(8020, "GxTubeDistortion", "Saturated valve overdrive & distortion pedal", "Distortion", "libGxTubeDistortion.lv2.so"),
            PluginInfo(8094, "GxSuperFuzz", "Thick octave fuzz pedal recreation", "Distortion", "libGxSuperFuzz.lv2.so"),
            PluginInfo(8018, "GxCabinet-4x12", "Loudspeaker 4x12 impulse cabinet simulation", "Cabinet", "libgx_cabinet.lv2.so"),
            PluginInfo(8025, "GxDigitalDelay", "Pristine digital stereo delay line", "Delay", "libgx_digital_delay.lv2.so"),
            PluginInfo(8050, "GxZitaReverb", "Studio algorithmic plate and hall reverb", "Reverb", "libgx_zita_rev1.lv2.so"),
            PluginInfo(8076, "GxChorusStereo", "Lush analog bucket-brigade stereo chorus", "Chorus", "libgx_chorus.lv2.so"),
            PluginInfo(8048, "GxPhaser", "Classic 4-stage optical phaser unit", "Phaser", "libgx_phaser.lv2.so"),
            PluginInfo(8037, "GxGraphicEQ", "10-band studio frequency equalizer", "Equalizer", "libgx_graphiceq.lv2.so"),
            PluginInfo(8078, "GxCompressor", "Dynamic punch sustain compressor", "Compressor", "libgx_compressor.lv2.so"),
            PluginInfo(8019, "GxDenoiserGate", "Intelligent downward expander noise gate", "Noise Gate", "libGxDenoiser2.lv2.so")
        )
    }
}
