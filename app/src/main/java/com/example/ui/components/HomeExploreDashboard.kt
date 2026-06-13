package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.state.AppScreen
import com.example.state.DetectionState
import com.example.state.InputMode
import com.example.state.SightingReport

@Composable
fun HomeExploreDashboard(
    state: DetectionState,
    onNavigateToLens: (InputMode) -> Unit,
    onAddNewMockSighting: (String, String, Float, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMockDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1B14), // Elegant Deep Forest Green gradient origin
                        Color(0xFF131F17),
                        Color(0xFF090D0B)
                    )
                )
            ),
        contentPadding = PaddingValues(bottom = 90.dp) // Leave clean space for bottom bar
    ) {
        // Hero Card
        item {
            HeroHeaderCard(
                state = state,
                onAddMockClick = { showAddMockDialog = true }
            )
        }

        // Quick Launch Channels
        item {
            QuickLaunchPanel(onNavigateToLens)
        }

        // Feed Heading
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPECIMEN SIGHTINGS FEED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA2B5A9),
                    letterSpacing = 1.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B3A27),
                    modifier = Modifier.clickable { showAddMockDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Sighting Log",
                            tint = Color(0xFF5ED38C),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log Sighting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5ED38C)
                        )
                    }
                }
            }
        }

        // Sightings Feed Elements
        if (state.sightingsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty list",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recorded spottings found.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(state.sightingsList) { report ->
                SightingReportCard(report)
            }
        }
    }

    // Modal to add client-side mockup sighting logs
    if (showAddMockDialog) {
        AddMockSightingDialog(
            onDismiss = { showAddMockDialog = false },
            onConfirm = { species, location, confidence, notes ->
                onAddNewMockSighting(species, location, confidence, notes)
                showAddMockDialog = false
            }
        )
    }
}

@Composable
fun HeroHeaderCard(
    state: DetectionState,
    onAddMockClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color(0xFF2C4A37), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16271D)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adaptive logo from previously modified assets
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B2A22))
                        .border(1.dp, Color(0xFF32523E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_lizard_logo),
                        contentDescription = "Lizard Lens Premium Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "LIZARD LENS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "The Scientific Herpetology Tool",
                        fontSize = 12.sp,
                        color = Color(0xFFA9C2B3),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Stats Grid Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCell(
                    title = "Journal Index",
                    value = "${state.sightingsList.size} Saved",
                    color = Color(0xFF5ED38C),
                    icon = Icons.Default.Bookmark
                )
                StatCell(
                    title = "Detections",
                    value = "${(state.confidenceThreshold * 100).toInt()}% Cutoff",
                    color = Color(0xFFF0B342),
                    icon = Icons.Default.BarChart
                )
                StatCell(
                    title = "Model State",
                    value = if (state.isModelReady) "Warm" else "Warming",
                    color = if (state.isModelReady) Color(0xFF5ED38C) else Color(0xFFE57373),
                    icon = Icons.Default.Bolt
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Streak Announcement
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF20362A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = Color(0xFFF9C846),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Daily Logging Streak: 4 Days Active!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onAddMockClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Log new item",
                            tint = Color(0xFF5ED38C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCell(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFFA9C2B3).copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun QuickLaunchPanel(
    onNavigateToLens: (InputMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "FAST INFERENCE CHANNELS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA2B5A9),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Channel 1: Live Lens
            QuickChannelCard(
                title = "Live Scanner",
                subtitle = "Camera Stream",
                icon = Icons.Default.Videocam,
                color = Color(0xFF5ED38C),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLens(InputMode.LIVE) }
            )

            // Channel 2: Image File
            QuickChannelCard(
                title = "Photo Lab",
                subtitle = "Upload File",
                icon = Icons.Default.Image,
                color = Color(0xFF4AC2F0),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLens(InputMode.IMAGE) }
            )

            // Channel 3: Video File
            QuickChannelCard(
                title = "Video Tracer",
                subtitle = "Frame Ticker",
                icon = Icons.Default.Movie,
                color = Color(0xFFF1B342),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLens(InputMode.VIDEO) }
            )
        }
    }
}

@Composable
fun QuickChannelCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .border(1.dp, Color(0xFF2C4A37), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFFA9C2B3).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SightingReportCard(report: SightingReport) {
    // Dynamic color coding depending on rarity
    val tagColor = when (report.rarity.lowercase()) {
        "rare", "legendary search" -> Color(0xFFF16E42)
        "endangered" -> Color(0xFFE53935)
        "vulnerable" -> Color(0xFFF5B041)
        else -> Color(0xFF5ED38C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFF22382C), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111E16)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.speciesName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = report.rarity.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = tagColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Location and Date Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFA2B5A9),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.location,
                    fontSize = 11.sp,
                    color = Color(0xFFA2B5A9),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFFA2B5A9),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.date,
                    fontSize = 11.sp,
                    color = Color(0xFFA2B5A9),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Note Text
            Text(
                text = report.note,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Accuracy & Temp metadata bar
            Divider(color = Color(0xFF1E3529), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFF5ED38C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Confidence Level: ${(report.confidence * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color(0xFF5ED38C),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = report.temperatureIndicator,
                    fontSize = 11.sp,
                    color = Color(0xFFA2B5A9).copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMockSightingDialog(
    onDismiss: () -> Unit,
    onConfirm: (species: String, location: String, confidence: Float, notes: String) -> Unit
) {
    var species by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(0.85f) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color(0xFF2C4A37), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Custom Sighting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species Name", color = Color(0xFFA2B5A9)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5ED38C),
                        unfocusedBorderColor = Color(0xFF2C4A37)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Search Region", color = Color(0xFFA2B5A9)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5ED38C),
                        unfocusedBorderColor = Color(0xFF2C4A37)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Field Notes / Behavior", color = Color(0xFFA2B5A9)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5ED38C),
                        unfocusedBorderColor = Color(0xFF2C4A37)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Inference Match Score", color = Color(0xFFA2B5A9), fontSize = 12.sp)
                    Text("${(confidence * 100).toInt()}%", color = Color(0xFF5ED38C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = confidence,
                    onValueChange = { confidence = it },
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF5ED38C),
                        activeTrackColor = Color(0xFF5ED38C)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFA2B5A9))
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (species.isNotBlank() && location.isNotBlank()) {
                                onConfirm(species, location, confidence, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5235), contentColor = Color.White),
                        enabled = species.isNotBlank() && location.isNotBlank()
                    ) {
                        Text("Register Sighting")
                    }
                }
            }
        }
    }
}
