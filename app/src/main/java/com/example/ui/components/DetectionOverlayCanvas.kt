package com.example.ui.components

import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.Detection
import com.example.ui.theme.ConfidenceBg
import com.example.ui.theme.ConfidenceText
import com.example.ui.theme.DetectionBoxFill
import com.example.ui.theme.DetectionBoxStroke
import kotlinx.coroutines.delay

@Composable
fun BoxScope.DetectionOverlayCanvas(
    detections: List<Detection>,
    isModelReady: Boolean,
    modifier: Modifier = Modifier
) {
    var showNoDetectionWarning by remember { mutableStateOf(false) }

    // Start a timer whenever detections are empty, resetting when they are occupied
    LaunchedEffect(detections, isModelReady) {
        if (detections.isEmpty() && isModelReady) {
            delay(3000) // 3 seconds of no detection as specified in §6.5
            showNoDetectionWarning = true
        } else {
            showNoDetectionWarning = false
        }
    }

    // Bounding Box Overlay Canvas
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                val desc = if (detections.isNotEmpty()) {
                    detections.joinToString(", ") { "${it.label} with ${(it.confidence * 100).toInt()}% confidence" }
                } else {
                    "No lizards identified."
                }
                contentDescription = desc
            }
    ) {
        val width = size.width
        val height = size.height

        detections.forEach { detection ->
            val box = detection.boundingBox
            // Map normalized coordinates [0.0..1.0] to active design sizes
            val rectLeft = box.left * width
            val rectTop = box.top * height
            val rectRight = box.right * width
            val rectBottom = box.bottom * height

            val rectW = rectRight - rectLeft
            val rectH = rectBottom - rectTop

            // 1. Draw solid rounded bounding box backing
            drawRoundRect(
                color = DetectionBoxFill,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 2. Draw thick amber boundary lines
            drawRoundRect(
                color = DetectionBoxStroke,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }

    // Overlay Tag Labels as positioned composables to support crisp typography, wrapping, and rich shapes
    val density = LocalDensity.current
    detections.forEachIndexed { index, detection ->
        val box = detection.boundingBox
        val confidencePercent = (detection.confidence * 100).toInt()

        // Place the tag pill above the top-left of the bounding box
        // To prevent collision overlaps, stagger offset if they are near each other
        val staggerOffset = (index * 24).dp

        BoxWithConstraintsParentSize { parentWidth, parentHeight ->
            val xDp = with(density) { (box.left * parentWidth).toDp() }
            val yDp = with(density) { (box.top * parentHeight).toDp() - 36.dp - staggerOffset }

            // Ensure label stays inside view boundaries
            val clampedXDp = xDp.coerceIn(8.dp, (parentWidth.dp - 150.dp).coerceAtLeast(8.dp))
            val clampedYDp = yDp.coerceIn(8.dp, (parentHeight.dp - 40.dp).coerceAtLeast(8.dp))

            Box(
                modifier = Modifier
                    .offset(x = clampedXDp, y = clampedYDp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ConfidenceBg)
                    .border(1.dp, DetectionBoxStroke, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${detection.label} $confidencePercent%",
                    color = ConfidenceText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }

    // 3s Idle Warning Element (Subtle Amber Pulsing Ring && Descriptive Text)
    AnimatedVisibility(
        visible = showNoDetectionWarning,
        enter = fadeIn(animationSpec = tween(600)),
        exit = fadeOut(animationSpec = tween(400)),
        modifier = Modifier.align(Alignment.Center)
    ) {
        val pulseScale = remember { androidx.compose.animation.core.Animatable(0.9f) }
        LaunchedEffect(Unit) {
            while (true) {
                pulseScale.animateTo(1.2f, animationSpec = tween(1500, easing = androidx.compose.animation.core.LinearEasing))
                pulseScale.animateTo(0.9f, animationSpec = tween(1500, easing = androidx.compose.animation.core.LinearEasing))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp * pulseScale.value)
                    .border(2.dp, DetectionBoxStroke.copy(alpha = 0.7f), CircleShape)
                    .background(DetectionBoxStroke.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(DetectionBoxStroke, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No detection in sight",
                color = DetectionBoxStroke,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Tiny layout helper to capture parent container size at compose runtime
 */
@Composable
fun BoxWithConstraintsParentSize(
    content: @Composable (widthPx: Float, heightPx: Float) -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        content(widthPx, heightPx)
    }
}
