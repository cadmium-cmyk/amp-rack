package com.shajikhan.ladspa.amprack.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.random.Random

data class TunerNote(
    val noteName: String,
    val targetFreq: Float,
    val detectedFreq: Float,
    val centsDeviation: Float,
    val isInTune: Boolean
)

data class RecordingSession(
    val id: String,
    val name: String,
    val durationSeconds: Int,
    val format: String,
    val timestamp: Long
)

class AudioEngineSimulator(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // VU Meters
    private val _inputLevel = MutableStateFlow(0.12f)
    val inputLevel: StateFlow<Float> = _inputLevel.asStateFlow()

    private val _outputLevel = MutableStateFlow(0.35f)
    val outputLevel: StateFlow<Float> = _outputLevel.asStateFlow()

    // Master Controls
    private val _isBypassed = MutableStateFlow(false)
    val isBypassed: StateFlow<Boolean> = _isBypassed.asStateFlow()

    private val _isLowLatency = MutableStateFlow(true)
    val isLowLatency: StateFlow<Boolean> = _isLowLatency.asStateFlow()

    private val _inputGain = MutableStateFlow(0.75f)
    val inputGain: StateFlow<Float> = _inputGain.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.80f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    // Tuner
    private val _isTunerActive = MutableStateFlow(false)
    val isTunerActive: StateFlow<Boolean> = _isTunerActive.asStateFlow()

    private val _currentTunerNote = MutableStateFlow(
        TunerNote("E", 82.41f, 82.50f, 2f, true)
    )
    val currentTunerNote: StateFlow<TunerNote> = _currentTunerNote.asStateFlow()

    // Backing Track Player
    private var exoPlayer: ExoPlayer? = null
    private val _isBackingTrackPlaying = MutableStateFlow(false)
    val isBackingTrackPlaying: StateFlow<Boolean> = _isBackingTrackPlaying.asStateFlow()

    private val _backingTrackProgress = MutableStateFlow(0f)
    val backingTrackProgress: StateFlow<Float> = _backingTrackProgress.asStateFlow()

    // Drum Machine
    private val _isDrumPlaying = MutableStateFlow(false)
    val isDrumPlaying: StateFlow<Boolean> = _isDrumPlaying.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _drumBpm = MutableStateFlow(110)
    val drumBpm: StateFlow<Int> = _drumBpm.asStateFlow()

    // Recording
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private val _recordings = MutableStateFlow<List<RecordingSession>>(emptyList())
    val recordings: StateFlow<List<RecordingSession>> = _recordings.asStateFlow()

    private var drumJob: Job? = null
    private var vuJob: Job? = null
    private var recordJob: Job? = null
    private var toneTrack: AudioTrack? = null

    // Audio Device Routing
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    
    private val _inputDevices = MutableStateFlow<List<android.media.AudioDeviceInfo>>(emptyList())
    val inputDevices: StateFlow<List<android.media.AudioDeviceInfo>> = _inputDevices.asStateFlow()
    
    private val _outputDevices = MutableStateFlow<List<android.media.AudioDeviceInfo>>(emptyList())
    val outputDevices: StateFlow<List<android.media.AudioDeviceInfo>> = _outputDevices.asStateFlow()

    private val _selectedInputDevice = MutableStateFlow<android.media.AudioDeviceInfo?>(null)
    val selectedInputDevice: StateFlow<android.media.AudioDeviceInfo?> = _selectedInputDevice.asStateFlow()
    
    private val _selectedOutputDevice = MutableStateFlow<android.media.AudioDeviceInfo?>(null)
    val selectedOutputDevice: StateFlow<android.media.AudioDeviceInfo?> = _selectedOutputDevice.asStateFlow()

    init {
        initBackingTrackPlayer()
        startVuMeters()
        refreshDevices()
    }

    fun refreshDevices() {
        _inputDevices.value = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_INPUTS).toList()
        _outputDevices.value = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS).toList()
        
        if (_selectedInputDevice.value == null) _selectedInputDevice.value = _inputDevices.value.firstOrNull()
        if (_selectedOutputDevice.value == null) _selectedOutputDevice.value = _outputDevices.value.firstOrNull()
    }

    fun setInputDevice(device: android.media.AudioDeviceInfo) {
        _selectedInputDevice.value = device
    }

    fun setOutputDevice(device: android.media.AudioDeviceInfo) {
        _selectedOutputDevice.value = device
    }

    private fun initBackingTrackPlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse("asset:///track.mp3"))
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isBackingTrackPlaying.value = isPlaying
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVuMeters() {
        vuJob = scope.launch {
            while (isActive) {
                if (!_isBypassed.value) {
                    val baseIn = _inputGain.value * 0.4f
                    val noiseIn = Random.nextFloat() * 0.15f
                    _inputLevel.value = (baseIn + noiseIn).coerceIn(0f, 1f)

                    val baseOut = _masterVolume.value * 0.5f
                    val noiseOut = Random.nextFloat() * 0.25f
                    _outputLevel.value = (baseOut + noiseOut).coerceIn(0f, 1f)
                } else {
                    _inputLevel.value = 0.05f
                    _outputLevel.value = 0.05f
                }
                delay(80)
            }
        }
    }

    fun toggleBypass() {
        _isBypassed.value = !_isBypassed.value
    }

    fun toggleLowLatency() {
        _isLowLatency.value = !_isLowLatency.value
    }

    fun setInputGain(gain: Float) {
        _inputGain.value = gain
    }

    fun setMasterVolume(vol: Float) {
        _masterVolume.value = vol
        exoPlayer?.volume = vol
    }

    fun toggleBackingTrack() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun setBackingTrackPosition(fraction: Float) {
        exoPlayer?.let { player ->
            val duration = player.duration
            if (duration > 0) {
                player.seekTo((duration * fraction).toLong())
            }
        }
    }

    fun setDrumBpm(bpm: Int) {
        _drumBpm.value = bpm.coerceIn(40, 260)
        if (_isDrumPlaying.value) {
            startDrumSequencer()
        }
    }

    fun toggleDrumMachine(activePattern: Map<String, List<Boolean>>? = null) {
        if (_isDrumPlaying.value) {
            _isDrumPlaying.value = false
            drumJob?.cancel()
            _currentStep.value = 0
        } else {
            _isDrumPlaying.value = true
            startDrumSequencer(activePattern)
        }
    }

    private fun startDrumSequencer(pattern: Map<String, List<Boolean>>? = null) {
        drumJob?.cancel()
        drumJob = scope.launch {
            var step = 0
            while (_isDrumPlaying.value && isActive) {
                _currentStep.value = step

                // Trigger click/tone for active drum steps
                pattern?.forEach { (_, steps) ->
                    if (step < steps.size && steps[step]) {
                        playClickSound(step == 0)
                    }
                }

                val stepDurationMs = ((60f / _drumBpm.value) * 1000f / 2f).toLong()
                delay(stepDurationMs.coerceAtLeast(30L))
                step = (step + 1) % 8
            }
        }
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            _isRecording.value = false
            recordJob?.cancel()
            val newSession = RecordingSession(
                id = "rec_${System.currentTimeMillis()}",
                name = "Amp Take ${_recordings.value.size + 1}",
                durationSeconds = _recordingDuration.value,
                format = "WAV 24-bit 48kHz",
                timestamp = System.currentTimeMillis()
            )
            _recordings.value = listOf(newSession) + _recordings.value
            _recordingDuration.value = 0
        } else {
            _isRecording.value = true
            _recordingDuration.value = 0
            recordJob = scope.launch {
                while (_isRecording.value && isActive) {
                    delay(1000)
                    _recordingDuration.value += 1
                }
            }
        }
    }

    fun toggleTuner() {
        _isTunerActive.value = !_isTunerActive.value
    }

    fun selectTunerString(note: String, targetHz: Float) {
        val dev = (Random.nextFloat() * 12f) - 6f
        _currentTunerNote.value = TunerNote(
            noteName = note,
            targetFreq = targetHz,
            detectedFreq = targetHz + (dev * 0.1f),
            centsDeviation = dev,
            isInTune = kotlin.math.abs(dev) < 2f
        )
    }

    fun playReferenceTone(freqHz: Float) {
        scope.launch(Dispatchers.IO) {
            try {
                toneTrack?.stop()
                toneTrack?.release()

                val sampleRate = 44100
                val numSamples = (sampleRate * 0.6).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freqHz)
                    buffer[i] = (sin(angle) * 12000.0).toInt().toShort()
                }

                toneTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                toneTrack?.write(buffer, 0, numSamples)
                toneTrack?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playClickSound(isDownbeat: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 22050
                val freq = if (isDownbeat) 880.0 else 440.0
                val samples = 300
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val fade = 1.0 - (i.toDouble() / samples)
                    buf[i] = (sin(2.0 * Math.PI * i / (sampleRate / freq)) * 8000.0 * fade).toInt().toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build())
                    .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                    .setBufferSizeInBytes(samples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(buf, 0, samples)
                track.play()
            } catch (e: Exception) {
                // Ignore transient audio error
            }
        }
    }

    fun release() {
        scope.cancel()
        exoPlayer?.release()
        toneTrack?.release()
    }
}
