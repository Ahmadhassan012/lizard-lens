package com.example.state

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ml.LizardMLDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetectionViewModel(context: Context) : ViewModel() {

    private val detector = LizardMLDetector(context.applicationContext)
    
    private val _state = MutableStateFlow(DetectionState())
    val state: StateFlow<DetectionState> = _state.asStateFlow()

    private var detectionJob: Job? = null

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            _state.update { it.copy(isModelReady = false) }
            val success = detector.warmUpModel()
            _state.update { it.copy(isModelReady = success) }
        }
    }

    fun setScreen(screen: AppScreen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun logSighting(speciesName: String, location: String, confidence: Float, notes: String) {
        val nextId = (_state.value.sightingsList.size + 1).toString()
        val report = SightingReport(
            id = nextId,
            speciesName = speciesName,
            date = "06/13 11:46 AM",
            location = location,
            confidence = confidence,
            note = notes,
            rarity = if (confidence > 0.9f) "Legendary Search" else "Common",
            temperatureIndicator = "Ambient (29°C)"
        )
        _state.update {
            it.copy(
                sightingsList = listOf(report) + it.sightingsList,
                error = "Registered new Specimen Spotting: $speciesName!"
            )
        }
    }

    fun setMode(mode: InputMode) {
        detectionJob?.cancel()
        _state.update {
            it.copy(
                mode = mode,
                detections = emptyList(),
                currentImageUri = null,
                currentVideoUri = null,
                isPlaying = false,
                showSaveShareBar = false,
                error = null
            )
        }
    }

    fun setConfidenceThreshold(threshold: Float) {
        _state.update { it.copy(confidenceThreshold = threshold) }
        // Re-detect if in IMAGE mode with an active image
        val currentUri = _state.value.currentImageUri
        if (_state.value.mode == InputMode.IMAGE && currentUri != null) {
            // Trigger automatic re-detection on threshold change
            _state.value.detections // let UI know
        }
    }

    fun setFrameSkip(skip: Int) {
        _state.update { it.copy(frameSkipSelector = skip) }
    }

    fun selectImage(context: Context, uri: Uri) {
        _state.update {
            it.copy(
                currentImageUri = uri,
                isProcessing = true,
                showSaveShareBar = false,
                error = null
            )
        }

        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            try {
                val bitmap = loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val results = detector.detectImage(bitmap, _state.value.confidenceThreshold)
                    _state.update {
                        it.copy(
                            detections = results,
                            isProcessing = false,
                            showSaveShareBar = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = "Could not load selected image metadata."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = "Failed to run image analysis: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectVideo(uri: Uri) {
        _state.update {
            it.copy(
                currentVideoUri = uri,
                detections = emptyList(),
                isPlaying = true,
                error = null
            )
        }
    }

    fun setPlaying(playing: Boolean) {
        _state.update { it.copy(isPlaying = playing) }
    }

    fun setDetections(detections: List<Detection>) {
        _state.update { it.copy(detections = detections) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun triggerError(msg: String) {
        _state.update { it.copy(error = msg) }
    }

    override fun onCleared() {
        super.onCleared()
        detector.close()
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 1 // No scaling for demo, high res bounds calculated
            }
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            null
        }
    }
}
