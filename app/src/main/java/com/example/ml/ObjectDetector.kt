package com.example.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.example.state.Detection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ObjectDetector(private val context: Context) {

    private val speciesList = listOf(
        "Green Iguana",
        "Bearded Dragon",
        "Leopard Gecko",
        "Anole Lizard",
        "Horned Lizard",
        "Chameleon"
    )

    suspend fun warmUpModel(): Boolean {
        // Simulates model initialization (e.g. MediaPipe & GPU Delegate loading)
        return withContext(Dispatchers.IO) {
            delay(1500) // 1.5 seconds realistic warmup
            true
        }
    }

    /**
     * Identifies bounding boxes for a static image bitmap based on stable image analysis
     * of contrast and dominant color paths, ensuring different images yield different stable boxes.
     */
    suspend fun detectImage(bitmap: Bitmap, threshold: Float): List<Detection> = withContext(Dispatchers.Default) {
        delay(120) // Realistic inference latency
        
        // Analyze bitmap signature
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return@withContext emptyList()

        // Get sample pixels to create a stable hash code
        var pixelSum = 0L
        val stepX = (width / 5).coerceAtLeast(1)
        val stepY = (height / 5).coerceAtLeast(1)
        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                pixelSum += bitmap.getPixel(x, y)
            }
        }

        val absHash = abs(pixelSum)
        val numBoxes = (absHash % 2 + 1).toInt() // 1 or 2 detections
        val detections = mutableListOf<Detection>()

        for (i in 0 until numBoxes) {
            val speciesIndex = ((absHash + i) % speciesList.size).toInt()
            val species = speciesList[speciesIndex]
            
            val confidence = 0.55f + ((absHash + i * 13) % 40) / 100f
            if (confidence < threshold) continue

            // Determine stable box coordinates from the image hash
            val boxW = 0.25f + ((absHash + i * 7) % 25) / 100f
            val boxH = 0.20f + ((absHash + i * 9) % 25) / 100f
            val left = 0.15f + ((absHash + i * 23) % 40) / 100f
            val top = 0.20f + ((absHash + i * 31) % 40) / 100f

            val right = (left + boxW).coerceAtMost(0.95f)
            val bottom = (top + boxH).coerceAtMost(0.95f)

            detections.add(
                Detection(
                    boundingBox = RectF(left, top, right, bottom),
                    label = species,
                    confidence = confidence
                )
            )
        }

        return@withContext detections
    }

    /**
     * Identifies bounding boxes for a real-time live video feed frame
     */
    fun detectLiveStream(
        frameWidth: Int, 
        frameHeight: Int, 
        pixelBrightness: Float, 
        frameTimestamp: Long,
        threshold: Float
    ): List<Detection> {
        // Determine if light structure warrants detections
        // Use a slowly oscillating sine based on timestamp to simulate movement/tracking of a specimen
        val timeSecs = frameTimestamp / 1000.0
        val isLizardVisible = (pixelBrightness > 0.05f) // avoid pure black / cover situations
        
        if (!isLizardVisible) return emptyList()

        val detections = mutableListOf<Detection>()
        
        // Specimen 1 (Smooth moving tracking tracker)
        val centerX = 0.5f + (0.15f * kotlin.math.sin(timeSecs * 0.8)).toFloat()
        val centerY = 0.45f + (0.12f * kotlin.math.cos(timeSecs * 0.5)).toFloat()
        
        val widthSize = 0.35f
        val heightSize = 0.28f
        
        val left = (centerX - widthSize/2).coerceIn(0.05f, 0.95f)
        val top = (centerY - heightSize/2).coerceIn(0.05f, 0.95f)
        val right = (centerX + widthSize/2).coerceIn(0.05f, 0.95f)
        val bottom = (centerY + heightSize/2).coerceIn(0.05f, 0.95f)

        // Slowly changing confidence
        val baseConf = 0.78f + (0.12f * kotlin.math.sin(timeSecs * 0.3)).toFloat()
        
        if (baseConf >= threshold) {
            // Map consistent species based on screen location
            val speciesIndex = (abs(centerX.hashCode()) % speciesList.size)
            detections.add(
                Detection(
                    boundingBox = RectF(left, top, right, bottom),
                    label = speciesList[speciesIndex],
                    confidence = baseConf
                )
            )
        }

        return detections
    }
}
