package com.shajikhan.ladspa.amprack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shajikhan.ladspa.amprack.audio.AudioEngineSimulator
import com.shajikhan.ladspa.amprack.data.model.*
import com.shajikhan.ladspa.amprack.data.repository.ConnectRepository
import com.shajikhan.ladspa.amprack.data.repository.PluginRepository
import com.shajikhan.ladspa.amprack.data.repository.PresetRepository
import com.shajikhan.ladspa.amprack.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AmpRackViewModel(application: Application) : AndroidViewModel(application) {
    val pluginRepo = PluginRepository(application)
    val presetRepo = PresetRepository(application)
    val connectRepo = ConnectRepository(application)
    val audioEngine = AudioEngineSimulator(application)

    // Current Theme
    private val _currentTheme = MutableStateFlow(AppTheme.MODERN)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    // Available Plugins Catalog
    private val _availablePlugins = MutableStateFlow<List<PluginInfo>>(emptyList())
    val availablePlugins: StateFlow<List<PluginInfo>> = _availablePlugins.asStateFlow()

    // Active Chain in Rack
    private val _activePlugins = MutableStateFlow<List<ActivePlugin>>(emptyList())
    val activePlugins: StateFlow<List<ActivePlugin>> = _activePlugins.asStateFlow()

    // Presets
    private val _presets = MutableStateFlow<List<Preset>>(emptyList())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()

    private val _selectedPreset = MutableStateFlow<Preset?>(null)
    val selectedPreset: StateFlow<Preset?> = _selectedPreset.asStateFlow()

    // Connect Guide Tips
    private val _connectTips = MutableStateFlow<List<ConnectTip>>(emptyList())
    val connectTips: StateFlow<List<ConnectTip>> = _connectTips.asStateFlow()

    // Drum Machine Tracks
    private val _drumInstruments = MutableStateFlow<List<DrumInstrument>>(emptyList())
    val drumInstruments: StateFlow<List<DrumInstrument>> = _drumInstruments.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val allPlugins = pluginRepo.getAllPlugins()
        _availablePlugins.value = allPlugins

        val factoryPresets = presetRepo.getFactoryPresets()
        val userPresets = presetRepo.getUserPresets()
        _presets.value = factoryPresets + userPresets

        _connectTips.value = connectRepo.getConnectTips()

        initDefaultDrumTracks()

        // Load default preset (British Crunch)
        if (factoryPresets.isNotEmpty()) {
            loadPreset(factoryPresets.first())
        }
    }

    private fun initDefaultDrumTracks() {
        _drumInstruments.value = listOf(
            DrumInstrument("kick", "Kick Bass", "Kick_Rock.ogg", listOf(true, false, false, false, true, false, false, false)),
            DrumInstrument("snare", "Snare Drum", "Snare_Rock.ogg", listOf(false, false, true, false, false, false, true, false)),
            DrumInstrument("hihat_c", "Closed Hi-Hat", "HH_1_closed_a.ogg", listOf(true, true, true, true, true, true, true, true)),
            DrumInstrument("hihat_o", "Open Hi-Hat", "HH_1_open.ogg", listOf(false, false, false, false, false, false, false, true)),
            DrumInstrument("tom_h", "High Tom", "Tom_Hi.ogg", listOf(false, false, false, true, false, false, false, false)),
            DrumInstrument("tom_l", "Low Tom", "Tom_Low.ogg", listOf(false, false, false, false, false, true, false, false)),
            DrumInstrument("cowbell", "Cowbell", "CowBell.ogg", listOf(false, true, false, false, false, true, false, false)),
            DrumInstrument("crash", "Crash Cymbal", "Crash_1.ogg", listOf(true, false, false, false, false, false, false, false))
        )
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    fun addPluginToRack(pluginInfo: PluginInfo) {
        val newInstance = ActivePlugin(
            instanceId = UUID.randomUUID().toString(),
            pluginInfo = pluginInfo,
            isBypassed = false,
            parameters = pluginInfo.defaultParameters.map { it.copy() },
            color = getPluginColor(pluginInfo.category)
        )
        _activePlugins.value = _activePlugins.value + newInstance
    }

    fun removePluginFromRack(instanceId: String) {
        _activePlugins.value = _activePlugins.value.filter { it.instanceId != instanceId }
    }

    fun togglePluginBypass(instanceId: String) {
        _activePlugins.value = _activePlugins.value.map { plugin ->
            if (plugin.instanceId == instanceId) {
                plugin.copy(isBypassed = !plugin.isBypassed)
            } else {
                plugin
            }
        }
    }

    fun updatePluginParameter(instanceId: String, parameterId: String, newValue: Float) {
        _activePlugins.value = _activePlugins.value.map { plugin ->
            if (plugin.instanceId == instanceId) {
                val updatedParams = plugin.parameters.map { param ->
                    if (param.id == parameterId) {
                        param.copy(currentValue = newValue)
                    } else {
                        param
                    }
                }
                plugin.copy(parameters = updatedParams)
            } else {
                plugin
            }
        }
    }

    fun movePluginUp(index: Int) {
        if (index > 0 && index < _activePlugins.value.size) {
            val list = _activePlugins.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _activePlugins.value = list
        }
    }

    fun movePluginDown(index: Int) {
        if (index >= 0 && index < _activePlugins.value.size - 1) {
            val list = _activePlugins.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _activePlugins.value = list
        }
    }

    fun clearRack() {
        _activePlugins.value = emptyList()
    }

    fun loadPreset(preset: Preset) {
        _selectedPreset.value = preset
        val newChain = mutableListOf<ActivePlugin>()
        val all = _availablePlugins.value

        preset.plugins.forEach { p ->
            val match = all.find { it.id == p.pluginId } ?: all.find { it.name.equals(p.pluginName, ignoreCase = true) }
            val pluginInfo = match ?: PluginInfo(
                id = p.pluginId,
                name = p.pluginName,
                category = p.category,
                defaultParameters = pluginRepo.generateDefaultParameters(p.pluginName, p.category)
            )

            val params = pluginInfo.defaultParameters.map { param ->
                val presetVal = p.parameterValues[param.id]
                if (presetVal != null) param.copy(currentValue = presetVal) else param.copy()
            }

            newChain.add(
                ActivePlugin(
                    instanceId = UUID.randomUUID().toString(),
                    pluginInfo = pluginInfo,
                    isBypassed = p.isBypassed,
                    parameters = params,
                    color = getPluginColor(pluginInfo.category)
                )
            )
        }
        _activePlugins.value = newChain
    }

    fun saveCurrentAsPreset(name: String, description: String) {
        val saved = presetRepo.savePreset(name, description, _activePlugins.value)
        _presets.value = presetRepo.getFactoryPresets() + presetRepo.getUserPresets()
        _selectedPreset.value = saved
    }

    fun deleteUserPreset(presetId: String) {
        presetRepo.deletePreset(presetId)
        _presets.value = presetRepo.getFactoryPresets() + presetRepo.getUserPresets()
        if (_selectedPreset.value?.id == presetId) {
            _selectedPreset.value = _presets.value.firstOrNull()
        }
    }

    // Drum Machine Step Toggle
    fun toggleDrumStep(instrumentId: String, stepIndex: Int) {
        _drumInstruments.value = _drumInstruments.value.map { inst ->
            if (inst.id == instrumentId) {
                val newSteps = inst.steps.toMutableList()
                newSteps[stepIndex] = !newSteps[stepIndex]
                inst.copy(steps = newSteps)
            } else {
                inst
            }
        }
    }

    fun clearDrumPattern() {
        _drumInstruments.value = _drumInstruments.value.map { inst ->
            inst.copy(steps = List(8) { false })
        }
    }

    fun applyDrumPreset(presetName: String) {
        when (presetName) {
            "Rock Standard" -> initDefaultDrumTracks()
            "Funk Groove" -> {
                _drumInstruments.value = _drumInstruments.value.map { inst ->
                    when (inst.id) {
                        "kick" -> inst.copy(steps = listOf(true, false, false, true, false, false, true, false))
                        "snare" -> inst.copy(steps = listOf(false, false, true, false, false, true, false, true))
                        "hihat_c" -> inst.copy(steps = listOf(true, true, true, true, true, true, true, true))
                        "cowbell" -> inst.copy(steps = listOf(false, true, false, false, false, true, false, false))
                        else -> inst.copy(steps = List(8) { false })
                    }
                }
            }
            "Blues Shuffle" -> {
                _drumInstruments.value = _drumInstruments.value.map { inst ->
                    when (inst.id) {
                        "kick" -> inst.copy(steps = listOf(true, false, false, false, true, false, false, false))
                        "snare" -> inst.copy(steps = listOf(false, false, true, false, false, false, true, false))
                        "hihat_c" -> inst.copy(steps = listOf(true, false, true, false, true, false, true, false))
                        else -> inst.copy(steps = List(8) { false })
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
