package com.shajikhan.ladspa.amprack.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shajikhan.ladspa.amprack.data.model.ActivePlugin
import com.shajikhan.ladspa.amprack.data.model.Preset
import com.shajikhan.ladspa.amprack.data.model.PresetPlugin

class PresetRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("amp_rack_presets", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getFactoryPresets(): List<Preset> {
        return listOf(
            Preset(
                id = "factory_crunch",
                name = "British Crunch",
                description = "Classic rock high-output tube breakup with tight cabinet punch",
                plugins = listOf(
                    PresetPlugin(8019, "GxDenoiserGate", "Noise Gate"),
                    PresetPlugin(8020, "GxTubeDistortion", "Overdrive"),
                    PresetPlugin(8095, "GxAmplifier-JCM800", "Amplifier"),
                    PresetPlugin(8018, "GxCabinet-4x12", "Cabinet"),
                    PresetPlugin(8050, "GxZitaReverb", "Reverb")
                )
            ),
            Preset(
                id = "factory_lead",
                name = "Heavy Metal Lead",
                description = "Screaming harmonics, mid boost, tight delay and infinite sustain",
                plugins = listOf(
                    PresetPlugin(8019, "GxDenoiserGate", "Noise Gate"),
                    PresetPlugin(8078, "GxCompressor", "Compressor"),
                    PresetPlugin(8094, "GxSuperFuzz", "Distortion"),
                    PresetPlugin(8095, "GxAmplifier-JCM800", "Amplifier"),
                    PresetPlugin(8025, "GxDigitalDelay", "Delay"),
                    PresetPlugin(8050, "GxZitaReverb", "Reverb")
                )
            ),
            Preset(
                id = "factory_blues",
                name = "Texas Blues Clean",
                description = "Warm tube saturation with snappy compressor and touch dynamics",
                plugins = listOf(
                    PresetPlugin(8078, "GxCompressor", "Compressor"),
                    PresetPlugin(8020, "GxTubeDistortion", "Overdrive"),
                    PresetPlugin(8095, "GxAmplifier-JCM800", "Amplifier"),
                    PresetPlugin(8018, "GxCabinet-4x12", "Cabinet"),
                    PresetPlugin(8076, "GxChorusStereo", "Chorus")
                )
            ),
            Preset(
                id = "factory_ambient",
                name = "Dreamy Ambient",
                description = "Shoegaze shimmer with lush stereo modulation and endless delay trails",
                plugins = listOf(
                    PresetPlugin(8078, "GxCompressor", "Compressor"),
                    PresetPlugin(8076, "GxChorusStereo", "Chorus"),
                    PresetPlugin(8048, "GxPhaser", "Phaser"),
                    PresetPlugin(8025, "GxDigitalDelay", "Delay"),
                    PresetPlugin(8050, "GxZitaReverb", "Reverb")
                )
            ),
            Preset(
                id = "factory_funk",
                name = "Funk Groove",
                description = "Punchy snappy compressor, bright EQ boost and rhythmic phase",
                plugins = listOf(
                    PresetPlugin(8078, "GxCompressor", "Compressor"),
                    PresetPlugin(8037, "GxGraphicEQ", "Equalizer"),
                    PresetPlugin(8048, "GxPhaser", "Phaser"),
                    PresetPlugin(8018, "GxCabinet-4x12", "Cabinet")
                )
            )
        )
    }

    fun getUserPresets(): List<Preset> {
        val json = prefs.getString("user_presets_json", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Preset>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePreset(name: String, description: String, activePlugins: List<ActivePlugin>): Preset {
        val current = getUserPresets().toMutableList()
        val newPreset = Preset(
            id = "user_${System.currentTimeMillis()}",
            name = name,
            description = description,
            isFactory = false,
            plugins = activePlugins.map { active ->
                PresetPlugin(
                    pluginId = active.pluginInfo.id,
                    pluginName = active.pluginInfo.name,
                    category = active.pluginInfo.category,
                    isBypassed = active.isBypassed,
                    parameterValues = active.parameters.associate { it.id to it.currentValue }
                )
            }
        )
        current.add(0, newPreset)
        prefs.edit().putString("user_presets_json", gson.toJson(current)).apply()
        return newPreset
    }

    fun deletePreset(id: String) {
        val current = getUserPresets().toMutableList()
        current.removeAll { it.id == id }
        prefs.edit().putString("user_presets_json", gson.toJson(current)).apply()
    }
}
