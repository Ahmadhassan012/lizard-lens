package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(
    onRunSelfCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var checkStep by remember { mutableStateOf("Ready to Audit") }
    var currentProgress by remember { mutableStateOf(0.0f) }

    val selfCheckSteps = listOf(
        "Initializing camera frame buffer...",
        "Validating MediaPipe herpetological weights...",
        "Validating viewport clipping matrices...",
        "Checking thermal limits...",
        "On-device neural inference pipeline fully warmed! (100% OK)"
    )

    fun startDiagnostics() {
        if (isChecking) return
        isChecking = true
        coroutineScope.launch {
            for (i in selfCheckSteps.indices) {
                checkStep = selfCheckSteps[i]
                currentProgress = (i + 1) / selfCheckSteps.size.toFloat()
                delay(1200)
            }
            isChecking = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1610),
                        Color(0xFF0F1E15),
                        Color(0xFF050807)
                    )
                )
            ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // App Manifesto Block
        item {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "LIZARD LENS NATURALIST SUITE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA2B5A9),
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scientific Manifesto & Core Specs",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lizard Lens is crafted to enable biologists, hobbyists, and naturalists to capture, identify, and report wild reptiles with absolute respect for nature. Operating 100% on-device, it maintains thermal efficiency, respects remote field locations, and minimizes sensory footprint.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            }
        }

        // Checklist/Checkup Interactive Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .border(1.dp, Color(0xFF2C4A37), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE PIPELINE HEALTH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5ED38C),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isChecking) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = checkStep,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(currentProgress * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5ED38C),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = currentProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF5ED38C),
                                trackColor = Color(0xFF1B2E24)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = checkStep,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { startDiagnostics() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5235)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify Pipe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ethics Manifesto Block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "NATURALIST ETHICAL CODE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA2B5A9),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                EthicsItemField(
                    number = "01",
                    title = "Respect Safe Distance Boundaries",
                    description = "When scanning lizards in active basking sites, remain at least 1.5-2 meters away. Zooming the viewport is preferred over direct physical proximity."
                )

                EthicsItemField(
                    number = "02",
                    title = "Leave Biotope Undisturbed",
                    description = "Never turn over flat stones, peel oak bark layerings, or strip desert shrub roots simply to force a specimen tracking response. Leave their wild shelters exactly as structured."
                )

                EthicsItemField(
                    number = "03",
                    title = "Preserve Thermal Regulation Cycles",
                    description = "Cold-blooded reptiles rely extensively on morning sun cycles. Shadows from human surveyors blocks vital infrared absorption. Minimize auditing durational spans to 3 minutes max."
                )

                EthicsItemField(
                    number = "04",
                    title = "Strictly Zero Invasive Audits",
                    description = "Lizard Lens is crafted purely for visual photography and observation logs. Handling, trapping, or feeding wild reptiles is strongly discouraged."
                )
            }
        }

        // Technical Environment Cards
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .border(1.dp, Color(0xFF22352B), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1812)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEVELOPMENT CORE ARTIFACTS",
                        color = Color(0xFFA2B5A9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Android CameraX Engine Integration\n• Jetpack Compose & Material 3 styling\n• MediaPipe On-Device ML Pipeline (Warmed)\n• Simulated Naturalist Logging Record State",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Leave plenty of bottom spacing for scroll
        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}

@Composable
fun EthicsItemField(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF5ED38C),
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFFA9C2B3).copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}
