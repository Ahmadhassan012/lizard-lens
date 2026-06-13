package com.example.state

import android.graphics.RectF
import android.net.Uri

enum class InputMode {
    LIVE, IMAGE, VIDEO
}

enum class AppScreen {
    HOME, LENS, GUIDE, ABOUT
}

data class SightingReport(
    val id: String,
    val speciesName: String,
    val date: String,
    val location: String,
    val confidence: Float,
    val note: String,
    val rarity: String, // e.g. "Rare", "Common", "Endangered"
    val temperatureIndicator: String // e.g. "Basking (32°C)"
)

data class LizardSpecies(
    val id: String,
    val name: String,
    val scientificName: String,
    val familyName: String,
    val avgLength: String,
    val habitat: String,
    val activityPeriod: String,
    val behaviorNote: String,
    val identificationKey: String
)

data class Detection(
    val boundingBox: RectF, // normalized coordinates [0.0..1.0]
    val label: String,
    val confidence: Float
)

data class DetectionState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val mode: InputMode = InputMode.LIVE,
    val detections: List<Detection> = emptyList(),
    val isModelReady: Boolean = false,
    val isProcessing: Boolean = false,
    val currentImageUri: Uri? = null,
    val currentVideoUri: Uri? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
    val showSaveShareBar: Boolean = false,
    val confidenceThreshold: Float = 0.5f,
    val frameSkipSelector: Int = 2, // runs inference on every N-th frame

    // Aesthetic simulated sightings list for the Home feed
    val sightingsList: List<SightingReport> = listOf(
        SightingReport("1", "Texas Horned Lizard", "06/13 09:12 AM", "Sonoran Dry Hills", 0.94f, "Adult specimen basking on dry sandstone gravel. Observed distinctive defensive posturing with flattened torso.", "Rare", "Basking (36°C)"),
        SightingReport("2", "Green Iguana", "06/12 04:30 PM", "Everglades Freshwater Slew", 0.88f, "Sub-adult located at the fork of a bald cypress tree. High camouflage index. Calm temperament.", "Common", "Canopy Sun (31°C)"),
        SightingReport("3", "Leopard Gecko", "06/10 11:15 PM", "Dry Scrub Savanna", 0.91f, "Nocturnal search. Found nesting under limestone shelf. Eyes fully dilated; healthy pattern spotted.", "Vulnerable", "Sheltered Cool (24°C)"),
        SightingReport("4", "Gila Monster", "05/29 08:05 AM", "Mojave Saguaro Foothills", 0.97f, "Heavy adult specimen active after high-humidity flash shower. Red and black toxic beads fully polished.", "Endangered", "Ground Basking (28°C)")
    ),

    // Encyclopedic on-device herpetology guide
    val speciesGuide: List<LizardSpecies> = listOf(
        LizardSpecies("1", "Horned Lizard", "Phrynosoma cornutum", "Phrynosomatidae", "8 - 15 cm", "Dry, sandy arid plains and deserts", "Diurnal (mid-morning basking)", "Known for its flat body and ability to squirt defensive fluid from its eye sinus.", "Crowned with sharp occipital horns, flat round profile, spiked lateral scales."),
        LizardSpecies("2", "Green Iguana", "Iguana iguana", "Iguanidae", "1.2 - 2.0 m", "Tropical forest canopy near water bodies", "Diurnal (high canopy basking)", "Highly arboreal and excellent swimmer. Primarily herbivorous.", "Spiny crest along mid-back, large round scale below tympanum drum."),
        LizardSpecies("3", "Leopard Gecko", "Eublepharis macularius", "Eublepharidae", "20 - 28 cm", "Rocky desert outcrops and clay soil", "Crepuscular / Nocturnal", "Lacks adhesive toe pads; instead has movable eyelids.", "Splattered dark brown rosettes on pale yellow ground color, thick fat-storing tail."),
        LizardSpecies("4", "Gila Monster", "Heloderma suspectum", "Helodermatidae", "35 - 55 cm", "Dry gravel foothills and canyon beds", "Crepuscular", "One of the world's few venomous lizards, hunting by olfaction.", "Tessellated patterns of pink, orange, and charcoal-yellow beaded osteoderms."),
        LizardSpecies("5", "Jackson's Chameleon", "Trioceros jacksonii", "Chamaeleonidae", "25 - 30 cm", "Humid mountain slope forests", "Diurnal (slow canopy stalking)", "Famous for color-changing physiology and rapid spring tongue capture.", "Three large forward-facing horns on adult males reminding of Triceratops skulls.")
    )
)

