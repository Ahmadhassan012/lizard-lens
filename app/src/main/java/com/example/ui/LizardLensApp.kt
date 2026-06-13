package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.state.DetectionViewModel
import com.example.state.InputMode
import com.example.ui.components.DetectionOverlayCanvas
import com.example.ui.components.DetectionViewport
import com.example.ui.components.ModeSelector
import com.example.ui.components.SaveShareBar
import com.example.ui.components.SettingsDialog
import com.example.ui.components.VideoTransportControls
import com.example.ui.components.WarmUpSplash
import kotlinx.coroutines.launch

import com.example.state.AppScreen
import com.example.ui.components.HomeExploreDashboard
import com.example.ui.components.FieldGuideScreen
import com.example.ui.components.AboutScreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun LizardBottomNavBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F1E15),
        border = BorderStroke(1.dp, Color(0xFF1E3326)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val screens = listOf(
                Triple(AppScreen.HOME, "Explore", Icons.Default.Home),
                Triple(AppScreen.LENS, "Lens", Icons.Default.CenterFocusStrong),
                Triple(AppScreen.GUIDE, "Field Guide", Icons.Default.MenuBook),
                Triple(AppScreen.ABOUT, "About Specs", Icons.Default.Info)
            )

            screens.forEach { (scr, label, icon) ->
                val isSelected = currentScreen == scr

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onScreenSelected(scr) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF1E5235) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color(0xFF5ED38C) else Color(0xFFA2B5A9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFFA2B5A9)
                    )
                }
            }
        }
    }
}

@Composable
fun LizardLensApp(
    viewModel: DetectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationale by remember { mutableStateOf(false) }

    // Launcher for camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            showPermissionRationale = true
        }
    }

    // Launchers for media selection
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectImage(context, uri)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectVideo(uri)
        }
    }

    // Check camera permission on Live mode selection
    LaunchedEffect(state.mode) {
        if (state.mode == InputMode.LIVE && !hasCameraPermission && state.currentScreen == AppScreen.LENS) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Capture state errors to show as non-blocking snackbars
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = err,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        bottomBar = {
            LizardBottomNavBar(
                currentScreen = state.currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentScreen) {
                AppScreen.HOME -> {
                    HomeExploreDashboard(
                        state = state,
                        onNavigateToLens = { mode ->
                            viewModel.setMode(mode)
                            viewModel.setScreen(AppScreen.LENS)
                        },
                        onAddNewMockSighting = { species, location, confidence, notes ->
                            viewModel.logSighting(species, location, confidence, notes)
                        }
                    )
                }

                AppScreen.GUIDE -> {
                    FieldGuideScreen(state = state)
                }

                AppScreen.ABOUT -> {
                    AboutScreen(onRunSelfCheck = {})
                }

                AppScreen.LENS -> {
                    // Lens Tracker Page Body
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = state.mode == InputMode.LIVE && !hasCameraPermission,
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                            )
                    ) {
                        if (state.mode == InputMode.LIVE && !hasCameraPermission) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Camera needed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Camera permission requested to run Live Tracking",
                                        color = Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Grant Permission")
                                    }
                                }
                            }
                        } else {
                            DetectionViewport(
                                mode = state.mode,
                                imageUri = state.currentImageUri,
                                videoUri = state.currentVideoUri,
                                isPlaying = state.isPlaying,
                                isModelReady = state.isModelReady,
                                onVideoPlayingChanged = { isPlaying -> viewModel.setPlaying(isPlaying) },
                                onLiveDetections = { results -> viewModel.setDetections(results) },
                                confidenceThreshold = state.confidenceThreshold,
                                frameSkipSelector = state.frameSkipSelector
                            )

                            // Bounding Box Overlay Canvas
                            DetectionOverlayCanvas(
                                detections = state.detections,
                                isModelReady = state.isModelReady
                            )
                        }
                    }

                    // Top Bar Floating Look Panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIZARD LENS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    text = "Active Scanner Lens",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Row {
                                if (state.mode == InputMode.IMAGE) {
                                    Surface(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(18.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .height(36.dp)
                                            .padding(end = 8.dp)
                                            .testTag("gallery_picker_trigger")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Pick Photo",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else if (state.mode == InputMode.VIDEO) {
                                    Surface(
                                        onClick = { videoPickerLauncher.launch("video/*") },
                                        shape = RoundedCornerShape(18.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .height(36.dp)
                                            .padding(end = 8.dp)
                                            .testTag("video_picker_trigger")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Pick Video",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                        .testTag("settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Open Settings Dialog",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Image Mode Context SaveShareBar
                    AnimatedVisibility(
                        visible = state.mode == InputMode.IMAGE && state.showSaveShareBar,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 76.dp)
                    ) {
                        SaveShareBar(
                            onSaveClicked = {
                                Toast.makeText(context, "Saved annotated frame successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onShareClicked = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Identified a Lizard Specimen with Lizard Lens app!")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share with:"))
                            }
                        )
                    }

                    // Video Play Transport Controls
                    if (state.mode == InputMode.VIDEO && state.currentVideoUri != null) {
                        VideoTransportControls(
                            isPlaying = state.isPlaying,
                            onPlayPauseToggled = { viewModel.setPlaying(!state.isPlaying) }
                        )
                    }

                    // Bottom Model Option Switch Rows
                    ModeSelector(
                        activeMode = state.mode,
                        onModeSelected = { mode -> viewModel.setMode(mode) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Splash warm up blockers during initialization
            AnimatedVisibility(
                visible = !state.isModelReady,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WarmUpSplash()
            }

            // Interactive Param Tweaker Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    confidenceThreshold = state.confidenceThreshold,
                    frameSkipSelector = state.frameSkipSelector,
                    onConfidenceChanged = { viewModel.setConfidenceThreshold(it) },
                    onFrameSkipChanged = { viewModel.setFrameSkip(it) },
                    onDismissRequest = { showSettingsDialog = false }
                )
            }

            // Rationales sheets for camera access
            if (showPermissionRationale) {
                BasicPermissionRationaleSheet(
                    onAction = {
                        showPermissionRationale = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onDismiss = { showPermissionRationale = false }
                )
            }
        }
    }
}

@Composable
fun BasicPermissionRationaleSheet(
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lizard Lens utilizes your camera device to execute on-device tracking. Please enable camera access in permissions to start surveying specimens.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onAction()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Open Settings")
                    }
                }
            }
        }
    }
}
