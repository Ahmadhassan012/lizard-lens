package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.DetectionState
import com.example.state.LizardSpecies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldGuideScreen(
    state: DetectionState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedSpeciesId by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(searchQuery, state.speciesGuide) {
        if (searchQuery.isBlank()) {
            state.speciesGuide
        } else {
            state.speciesGuide.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.scientificName.contains(searchQuery, ignoreCase = true) ||
                it.familyName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1610),
                        Color(0xFF102118),
                        Color(0xFF060A08)
                    )
                )
            )
    ) {
        // Floating header panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "TAXONOMIC FIELD GUIDE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA2B5A9),
                letterSpacing = 1.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reptilia: Squamata",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Discover on-field diagnostic criteria and habitat parameters for registered families.",
                fontSize = 12.sp,
                color = Color(0xFFA9C2B3).copy(alpha = 0.8f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter species, family, or scientific name...", color = Color(0xFFA2B5A9).copy(alpha = 0.6f), fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = Color(0xFFA2B5A9)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color(0xFFA2B5A9)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF5ED38C),
                    unfocusedBorderColor = Color(0xFF223C2C),
                    focusedContainerColor = Color(0xFF14241B),
                    unfocusedContainerColor = Color(0xFF14241B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Divider(color = Color(0xFF1A3324), thickness = 1.dp)

        // Guide entries
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching lizard families listed in database.",
                            color = Color(0xFFA2B5A9).copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredList) { species ->
                    val isExpanded = expandedSpeciesId == species.id
                    FieldGuideCard(
                        species = species,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedSpeciesId = if (isExpanded) null else species.id
                        }
                    )
                }
            }

            // Bottom Spacing context
            item {
                Spacer(modifier = Modifier.height(82.dp))
            }
        }
    }
}

@Composable
fun FieldGuideCard(
    species: LizardSpecies,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isExpanded) Color(0xFF5ED38C).copy(alpha = 0.5f) else Color(0xFF1A3324),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color(0xFF162A1F) else Color(0xFF101E15)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Index Label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpanded) Color(0xFF5ED38C) else Color(0xFF1D3528),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = species.id,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) Color(0xFF0F1B14) else Color(0xFF5ED38C)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Titles block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = species.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = species.scientificName,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFA9C2B3).copy(alpha = 0.8f)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFFA2B5A9)
                )
            }

            // Smooth expansion block
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider(color = Color(0xFF2C4A37), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Spec fields
                    GuideSpecRow(label = "Family Group", value = species.familyName)
                    GuideSpecRow(label = "Average Length", value = species.avgLength)
                    GuideSpecRow(label = "Primary Biome", value = species.habitat)
                    GuideSpecRow(label = "Basking Cycle", value = species.activityPeriod)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Behavior & Notes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5ED38C),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = species.behaviorNote,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Key Diagnostic Markers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5ED38C),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = species.identificationKey,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GuideSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFA2B5A9).copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.widthIn(max = 220.dp)
        )
    }
}
