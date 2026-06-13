package com.example.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.example.state.Detection
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LizardMLDetector handles on-device lizard identification using MediaPipe Tasks Vision.
 * It manages the lifecycle of the underlying MediaPipe ObjectDetector and provides
 * synchronous and asynchronous detection methods for images, live streams, and video frames.
 */
class LizardMLDetector(private val context: Context) {

    private var mpObjectDetector: ObjectDetector? = null

    /**
     * Lazy initialization of the MediaPipe Object Detector.
     * Uses RunningMode.IMAGE to allow flexible synchronous calls from various threads.
     */
    private fun getOrCreateModel(threshold: Float): ObjectDetector? {
        synchronized(this) {
            if (mpObjectDetector != null) return mpObjectDetector

            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath("best_float32.tflite")
                .setDelegate(Delegate.GPU)

            val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setScoreThreshold(threshold)
                .setRunningMode(RunningMode.IMAGE)

            return try {
                mpObjectDetector = ObjectDetector.createFromOptions(context, optionsBuilder.build())
                mpObjectDetector
            } catch (e: Exception) {
                // Fallback to CPU if GPU delegate fails or is unsupported
                try {
                    baseOptionsBuilder.setDelegate(Delegate.CPU)
                    mpObjectDetector = ObjectDetector.createFromOptions(context, optionsBuilder.build())
                    mpObjectDetector
                } catch (e2: Exception) {
                    e2.printStackTrace()
                    null
                }
            }
        }
    }

    suspend fun warmUpModel(): Boolean = withContext(Dispatchers.IO) {
        getOrCreateModel(0.3f) != null
    }

    /**
     * Inference for static images.
     */
    suspend fun detectImage(bitmap: Bitmap, threshold: Float): List<Detection> = withContext(Dispatchers.Default) {
        val detector = getOrCreateModel(threshold) ?: return@withContext emptyList()
        
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = detector.detect(mpImage)
        
        return@withContext mapResults(result, mpImage.width, mpImage.height)
    }

    /**
     * Inference for real-time live stream or video frames.
     */
    fun detectFrame(
        bitmap: Bitmap?,
        threshold: Float
    ): List<Detection> {
        val detector = getOrCreateModel(threshold) ?: return emptyList()
        val frame = bitmap ?: return emptyList()

        val mpImage = BitmapImageBuilder(frame).build()
        val result = detector.detect(mpImage)
        
        return mapResults(result, mpImage.width, mpImage.height)
    }

    /**
     * Maps MediaPipe ObjectDetectorResult to domain Detection objects with normalized coordinates [0.0..1.0].
     */
    private fun mapResults(result: ObjectDetectorResult, width: Int, height: Int): List<Detection> {
        return result.detections().map { detection ->
            val box = detection.boundingBox()
            
            // Normalize coordinates for the UI Canvas
            val left = (box.left / width).coerceIn(0f, 1f)
            val top = (box.top / height).coerceIn(0f, 1f)
            val right = (box.right / width).coerceIn(0f, 1f)
            val bottom = (box.bottom / height).coerceIn(0f, 1f)

            Detection(
                boundingBox = RectF(left, top, right, bottom),
                label = detection.categories().firstOrNull()?.categoryName() ?: "Lizard",
                confidence = detection.categories().firstOrNull()?.score() ?: 0f
            )
        }
    }
    
    fun close() {
        synchronized(this) {
            mpObjectDetector?.close()
            mpObjectDetector = null
        }
    }
}
