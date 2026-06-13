package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import android.view.TextureView
import android.view.Surface
import com.example.ml.LizardMLDetector
import com.example.state.Detection
import com.example.state.InputMode
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@SuppressLint("UnrememberedMutableState")
@Composable
fun DetectionViewport(
    mode: InputMode,
    imageUri: Uri?,
    videoUri: Uri?,
    isPlaying: Boolean,
    isModelReady: Boolean,
    onVideoPlayingChanged: (Boolean) -> Unit,
    confidenceThreshold: Float,
    frameSkipSelector: Int,
    onLiveDetections: (List<Detection>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when (mode) {
            InputMode.LIVE -> {
                LiveCameraViewport(
                    isModelReady = isModelReady,
                    confidenceThreshold = confidenceThreshold,
                    onLiveDetections = onLiveDetections
                )
            }
            InputMode.IMAGE -> {
                if (imageUri != null) {
                    ImageGalleryViewport(imageUri = imageUri)
                } else {
                    EmptyViewportPlaceholder(
                        text = "No image selected.\nTap the selector below to analyze a photo."
                    )
                }
            }
            InputMode.VIDEO -> {
                if (videoUri != null) {
                    VideoExoPlayerViewport(
                        videoUri = videoUri,
                        isPlaying = isPlaying,
                        confidenceThreshold = confidenceThreshold,
                        frameSkipSelector = frameSkipSelector,
                        onVideoPlayingChanged = onVideoPlayingChanged,
                        onVideoDetections = onLiveDetections
                    )
                } else {
                    EmptyViewportPlaceholder(
                        text = "No video selected.\nTap the selector below to load recorded assets."
                    )
                }
            }
        }
    }
}

@Composable
fun LiveCameraViewport(
    isModelReady: Boolean,
    confidenceThreshold: Float,
    onLiveDetections: (List<Detection>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val objectDetector = remember { ObjectDetector(context) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Clean up Executor on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            try {
                cameraProviderRef?.unbindAll()
            } catch (e: Exception) {
                Log.e("LiveCameraViewport", "Error unbinding camera: ${e.localizedMessage}")
            }
        }
    }

    if (!isModelReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProviderRef = cameraProvider

                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // Feed camera frames live into the analyzer
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            try {
                                val bitmap = imageProxy.toBitmap()
                                val results = objectDetector.detectLiveStream(
                                    bitmap = bitmap,
                                    threshold = confidenceThreshold
                                )
                                onLiveDetections(results)
                            } catch (e: Exception) {
                                Log.e("CameraViewport", "Inference error: ${e.localizedMessage}")
                            } finally {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraViewport", "Binding failed: ${e.localizedMessage}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ImageGalleryViewport(imageUri: Uri) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = "Analyzed specimen from gallery",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoExoPlayerViewport(
    videoUri: Uri,
    isPlaying: Boolean,
    confidenceThreshold: Float,
    frameSkipSelector: Int,
    onVideoPlayingChanged: (Boolean) -> Unit,
    onVideoDetections: (List<Detection>) -> Unit
) {
    val context = LocalContext.current
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Load Uri into Player
    LaunchedEffect(videoUri) {
        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    // Sync Playback states
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }

    // Build frame analyze ticker depending on playback position and frame skipping factor
    LaunchedEffect(isPlaying, confidenceThreshold, frameSkipSelector, textureViewRef) {
        val objectDetector = LizardMLDetector(context)
        var tickCount = 0
        while (isPlaying) {
            tickCount++
            // Only execute TFLite detection on eligible ticks to match frame skipping factor
            if (tickCount % frameSkipSelector == 0) {
                val bitmap = textureViewRef?.bitmap
                if (bitmap != null) {
                    val results = objectDetector.detectFrame(
                        bitmap = bitmap,
                        threshold = confidenceThreshold
                    )
                    onVideoDetections(results)
                }
            }
            delay(150L) // 150ms intervals
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                onVideoPlayingChanged(playing)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            try {
                exoPlayer.removeListener(listener)
                exoPlayer.stop()
                exoPlayer.release()
            } catch (e: Exception) {
                Log.e("VideoViewport", "Error releasing ExoPlayer: ${e.localizedMessage}")
            }
        }
    }

    // Video display frame using TextureView for bitmap capture capability
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                textureViewRef = this
                exoPlayer.setVideoTextureView(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun EmptyViewportPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
    }
}
 }
}
