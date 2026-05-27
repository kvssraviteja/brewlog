package com.kvssrt.brewlog.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kvssrt.brewlog.data.BrewSegmentEntity
import com.kvssrt.brewlog.data.BrewSegmentSummary
import com.kvssrt.brewlog.data.BrewlogRepository
import com.kvssrt.brewlog.data.CoffeeBagDraft
import com.kvssrt.brewlog.data.CoffeeBagEntity
import com.kvssrt.brewlog.data.CoffeeBagImageStorage
import com.kvssrt.brewlog.data.CoffeeBagSummary
import com.kvssrt.brewlog.data.PourLogDraft
import com.kvssrt.brewlog.data.PourLogEntity
import com.kvssrt.brewlog.data.brewStyles
import com.kvssrt.brewlog.data.presetBrewers
import androidx.exifinterface.media.ExifInterface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private sealed interface BrewlogScreen {
    data object Home : BrewlogScreen
    data class CoffeeBagForm(val coffeeBagId: Long? = null) : BrewlogScreen
    data class CoffeeBagDetail(val coffeeBagId: Long) : BrewlogScreen
    data class PourLogForm(
        val coffeeBagId: Long,
        val logId: Long? = null,
        val initialSegmentId: Long? = null,
    ) : BrewlogScreen
    data class SegmentLogs(val coffeeBagId: Long, val segmentId: Long, val segmentLabel: String) : BrewlogScreen
}

@Composable
fun BrewlogApp(
    repository: BrewlogRepository,
    imageStorage: CoffeeBagImageStorage,
) {
    var screen by remember { mutableStateOf<BrewlogScreen>(BrewlogScreen.Home) }

    when (val current = screen) {
        BrewlogScreen.Home -> HomeScreen(
            repository = repository,
            onAddCoffeeBag = { screen = BrewlogScreen.CoffeeBagForm() },
            onOpenCoffeeBag = { screen = BrewlogScreen.CoffeeBagDetail(it) },
        )

        is BrewlogScreen.CoffeeBagForm -> CoffeeBagFormScreen(
            repository = repository,
            imageStorage = imageStorage,
            coffeeBagId = current.coffeeBagId,
            onBack = {
                screen = current.coffeeBagId?.let { BrewlogScreen.CoffeeBagDetail(it) } ?: BrewlogScreen.Home
            },
            onSaved = { screen = BrewlogScreen.CoffeeBagDetail(it) },
        )

        is BrewlogScreen.CoffeeBagDetail -> CoffeeBagDetailScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            onBack = { screen = BrewlogScreen.Home },
            onEditBag = { screen = BrewlogScreen.CoffeeBagForm(current.coffeeBagId) },
            onAddLog = { screen = BrewlogScreen.PourLogForm(current.coffeeBagId) },
            onOpenSegment = { segment ->
                screen = BrewlogScreen.SegmentLogs(
                    coffeeBagId = current.coffeeBagId,
                    segmentId = segment.id,
                    segmentLabel = segment.label,
                )
            },
        )

        is BrewlogScreen.PourLogForm -> PourLogFormScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            logId = current.logId,
            initialSegmentId = current.initialSegmentId,
            onBack = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
            onSaved = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
        )

        is BrewlogScreen.SegmentLogs -> SegmentLogsScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            segmentId = current.segmentId,
            segmentLabel = current.segmentLabel,
            onBack = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
            onAddLog = {
                screen = BrewlogScreen.PourLogForm(
                    coffeeBagId = current.coffeeBagId,
                    initialSegmentId = current.segmentId,
                )
            },
            onEditLog = { log ->
                screen = BrewlogScreen.PourLogForm(
                    coffeeBagId = current.coffeeBagId,
                    logId = log.id,
                    initialSegmentId = current.segmentId,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        ),
                ) {
                    Text(
                        text = "<",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        actions = {
            if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(text = actionText)
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    repository: BrewlogRepository,
    onAddCoffeeBag: () -> Unit,
    onOpenCoffeeBag: (Long) -> Unit,
) {
    val coffeeBags by repository.observeCoffeeBags().collectAsState(initial = emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Brewlog") },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCoffeeBag) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        BrewlogList(
            modifier = Modifier.padding(innerPadding),
        ) {
            item {
                Text(
                    text = "Coffee bags",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (coffeeBags.isEmpty()) {
                item {
                    Text(
                        text = "Add a coffee bag to start logging brews.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(coffeeBags, key = { it.id }) { coffeeBag ->
                CoffeeBagCard(coffeeBag = coffeeBag, onClick = { onOpenCoffeeBag(coffeeBag.id) })
            }
        }
    }
}

@Composable
private fun CoffeeBagCard(
    coffeeBag: CoffeeBagSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoffeeImage(
                imagePath = coffeeBag.imagePath,
                modifier = Modifier.size(72.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = coffeeBag.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (coffeeBag.roaster.isNotBlank()) {
                    Text(text = coffeeBag.roaster, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (coffeeBag.roastDate.isNotBlank()) {
                    Text(
                        text = "Roasted ${coffeeBag.roastDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = buildCoffeeBagSummary(coffeeBag),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoffeeBagFormScreen(
    repository: BrewlogRepository,
    imageStorage: CoffeeBagImageStorage,
    coffeeBagId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val existingBag by coffeeBagId
        ?.let { repository.observeCoffeeBag(it).collectAsState(initial = null) }
        ?: remember { mutableStateOf<CoffeeBagEntity?>(null) }

    var initialized by remember(coffeeBagId) { mutableStateOf(false) }
    var name by remember(coffeeBagId) { mutableStateOf("") }
    var roaster by remember(coffeeBagId) { mutableStateOf("") }
    var roastDate by remember(coffeeBagId) { mutableStateOf("") }
    var beanDetails by remember(coffeeBagId) { mutableStateOf("") }
    var imagePath by remember(coffeeBagId) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    if (!initialized && (coffeeBagId == null || existingBag != null)) {
        existingBag?.let {
            name = it.name
            roaster = it.roaster
            roastDate = it.roastDate
            beanDetails = it.beanDetails
            imagePath = it.imagePath
        }
        initialized = true
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            runCatching { imageStorage.copyToAppStorage(uri) }
                .onSuccess {
                    imagePath = it
                    error = null
                }
                .onFailure { error = it.message ?: "Could not save selected image." }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (coffeeBagId == null) "Add coffee bag" else "Edit coffee bag",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        BrewlogList(
            modifier = Modifier.padding(innerPadding),
        ) {
            item {
                CoffeeImage(
                    imagePath = imagePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.2f),
                )
            }
            item {
                Button(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (imagePath.isBlank()) "Add bag image" else "Change bag image")
                }
            }
            item {
                CoffeeTextField(value = name, onValueChange = { name = it }, label = "Coffee name")
            }
            item {
                CoffeeTextField(value = roaster, onValueChange = { roaster = it }, label = "Roaster")
            }
            item {
                CoffeeTextField(value = roastDate, onValueChange = { roastDate = it }, label = "Roast date YYYY-MM-DD")
            }
            item {
                CoffeeTextField(
                    value = beanDetails,
                    onValueChange = { beanDetails = it },
                    label = "Bean details",
                    minLines = 5,
                )
            }
            item {
                error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        scope.launch {
                            val draft = CoffeeBagDraft(
                                id = coffeeBagId ?: 0,
                                name = name,
                                roaster = roaster,
                                roastDate = roastDate,
                                beanDetails = beanDetails,
                                imagePath = imagePath,
                            )
                            runCatching {
                                val existing = existingBag
                                if (existing == null) {
                                    repository.addCoffeeBag(draft)
                                } else {
                                    repository.updateCoffeeBag(existing, draft)
                                    existing.id
                                }
                            }.onSuccess(onSaved)
                                .onFailure { error = it.message ?: "Could not save coffee bag." }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Save coffee bag")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoffeeBagDetailScreen(
    repository: BrewlogRepository,
    coffeeBagId: Long,
    onBack: () -> Unit,
    onEditBag: () -> Unit,
    onAddLog: () -> Unit,
    onOpenSegment: (BrewSegmentSummary) -> Unit,
) {
    val coffeeBag by repository.observeCoffeeBag(coffeeBagId).collectAsState(initial = null)
    val segments by repository.observeSegmentSummaries(coffeeBagId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                title = coffeeBag?.name ?: "Coffee bag",
                onBack = onBack,
                actionText = "Edit",
                onAction = onEditBag,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLog) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        BrewlogList(
            modifier = Modifier.padding(innerPadding),
        ) {
            coffeeBag?.let {
                item { CoffeeBagDetails(coffeeBag = it) }
            }
            item {
                Text(
                    text = "Brew segments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (segments.isEmpty()) {
                item {
                    Text(
                        text = "Add a brew log to create the first segment for this bag.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(segments, key = { it.id }) { segment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSegment(segment) },
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when (segment.logCount) {
                                1L -> "1 log"
                                else -> "${segment.logCount} logs"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoffeeBagDetails(coffeeBag: CoffeeBagEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CoffeeImage(
                imagePath = coffeeBag.imagePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.2f),
            )
            Text(
                text = coffeeBag.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (coffeeBag.roaster.isNotBlank()) {
                Text(text = coffeeBag.roaster, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (coffeeBag.roastDate.isNotBlank()) {
                Text(text = "Roasted ${coffeeBag.roastDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (coffeeBag.beanDetails.isNotBlank()) {
                Text(text = coffeeBag.beanDetails)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PourLogFormScreen(
    repository: BrewlogRepository,
    coffeeBagId: Long,
    logId: Long?,
    initialSegmentId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val existingLog by logId
        ?.let { repository.observeLog(it).collectAsState(initial = null) }
        ?: remember { mutableStateOf<PourLogEntity?>(null) }
    val segments by repository.observeAllSegments(coffeeBagId).collectAsState(initial = emptyList())
    val selectedInitialSegment = remember(segments, initialSegmentId, existingLog) {
        segments.firstOrNull { it.id == (existingLog?.segmentId ?: initialSegmentId) }
    }

    var initialized by remember(logId, selectedInitialSegment?.id) { mutableStateOf(false) }
    var brewedOn by remember(logId) { mutableStateOf(LocalDate.now().toString()) }
    var brewStyle by remember(logId) { mutableStateOf(brewStyles.first()) }
    var brewer by remember(logId) { mutableStateOf("") }
    var doseGrams by remember(logId) { mutableStateOf("") }
    var waterGrams by remember(logId) { mutableStateOf("") }
    var equipmentDetails by remember(logId) { mutableStateOf("") }
    var grinderDetails by remember(logId) { mutableStateOf("") }
    var grindSize by remember(logId) { mutableStateOf("") }
    var recipe by remember(logId) { mutableStateOf("") }
    var waterDetails by remember(logId) { mutableStateOf("") }
    var tastingNotes by remember(logId) { mutableStateOf("") }
    var nextImprovements by remember(logId) { mutableStateOf("") }
    var rating by remember(logId) { mutableStateOf("") }
    var brewTimeSeconds by remember(logId) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (!initialized && (logId == null || existingLog != null)) {
        selectedInitialSegment?.let {
            brewStyle = it.brewStyle
            brewer = it.brewer
        }
        existingLog?.let {
            brewedOn = it.brewedOn.toString()
            doseGrams = it.doseGrams.clean()
            waterGrams = it.waterGrams.clean()
            equipmentDetails = it.equipmentDetails
            grinderDetails = it.grinderDetails
            grindSize = it.grindSize
            recipe = it.recipe
            waterDetails = it.waterDetails
            tastingNotes = it.tastingNotes
            nextImprovements = it.nextImprovements
            rating = it.rating?.toString().orEmpty()
            brewTimeSeconds = it.brewTimeSeconds?.toString().orEmpty()
        }
        initialized = true
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (logId == null) "Add brew log" else "Edit brew log",
                onBack = onBack,
                actionText = if (logId == null) null else "Delete",
                onAction = if (logId == null) null else ({ showDeleteConfirmation = true }),
            )
        },
    ) { innerPadding ->
        BrewlogList(
            modifier = Modifier.padding(innerPadding),
        ) {
            item {
                CoffeeTextField(value = brewedOn, onValueChange = { brewedOn = it }, label = "Date")
            }
            item {
                Text(text = "Brew style", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                ChipRows(items = brewStyles, selected = brewStyle, onSelected = { brewStyle = it })
            }
            item {
                Text(text = "Brewer", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                ChipRows(items = presetBrewers, selected = brewer, onSelected = { brewer = it })
                CoffeeTextField(value = brewer, onValueChange = { brewer = it }, label = "Brewer")
            }
            item {
                CoffeeTextField(
                    value = doseGrams,
                    onValueChange = { doseGrams = it },
                    label = "Coffee dose grams",
                    keyboardType = KeyboardType.Decimal,
                )
            }
            item {
                CoffeeTextField(
                    value = waterGrams,
                    onValueChange = { waterGrams = it },
                    label = "Water grams",
                    keyboardType = KeyboardType.Decimal,
                )
            }
            item {
                CoffeeTextField(value = grindSize, onValueChange = { grindSize = it }, label = "Grind size")
            }
            item {
                CoffeeTextField(
                    value = equipmentDetails,
                    onValueChange = { equipmentDetails = it },
                    label = "Equipment details",
                    minLines = 2,
                )
            }
            item {
                CoffeeTextField(
                    value = grinderDetails,
                    onValueChange = { grinderDetails = it },
                    label = "Grinder details",
                    minLines = 2,
                )
            }
            item {
                CoffeeTextField(value = recipe, onValueChange = { recipe = it }, label = "Recipe", minLines = 4)
            }
            item {
                CoffeeTextField(
                    value = waterDetails,
                    onValueChange = { waterDetails = it },
                    label = "Water details",
                    minLines = 2,
                )
            }
            item {
                CoffeeTextField(
                    value = tastingNotes,
                    onValueChange = { tastingNotes = it },
                    label = "Tasting notes",
                    minLines = 3,
                )
            }
            item {
                CoffeeTextField(
                    value = nextImprovements,
                    onValueChange = { nextImprovements = it },
                    label = "Next improvements",
                    minLines = 3,
                )
            }
            item {
                CoffeeTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = "Rating 1-5",
                    keyboardType = KeyboardType.Number,
                )
            }
            item {
                CoffeeTextField(
                    value = brewTimeSeconds,
                    onValueChange = { brewTimeSeconds = it },
                    label = "Brew time seconds",
                    keyboardType = KeyboardType.Number,
                )
            }
            item {
                error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        runCatching {
                            buildPourLogDraft(
                                coffeeBagId = coffeeBagId,
                                logId = logId ?: 0,
                                brewedOn = brewedOn,
                                brewStyle = brewStyle,
                                brewer = brewer,
                                doseGrams = doseGrams,
                                waterGrams = waterGrams,
                                equipmentDetails = equipmentDetails,
                                grinderDetails = grinderDetails,
                                grindSize = grindSize,
                                recipe = recipe,
                                waterDetails = waterDetails,
                                tastingNotes = tastingNotes,
                                nextImprovements = nextImprovements,
                                rating = rating,
                                brewTimeSeconds = brewTimeSeconds,
                            )
                        }.onSuccess { draft ->
                            scope.launch {
                                runCatching {
                                    val existing = existingLog
                                    if (existing == null) {
                                        repository.addPourLog(draft)
                                    } else {
                                        repository.updatePourLog(existing, draft)
                                        existing.id
                                    }
                                }.onSuccess { onSaved() }
                                    .onFailure { error = it.message ?: "Could not save brew log." }
                            }
                        }.onFailure {
                            error = it.message ?: "Check the brew log details."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Save brew log")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "Delete brew log?") },
            text = { Text(text = "This removes this log from the segment.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val log = existingLog
                        if (log != null) {
                            scope.launch {
                                repository.deletePourLog(log)
                                showDeleteConfirmation = false
                                onSaved()
                            }
                        } else {
                            showDeleteConfirmation = false
                        }
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentLogsScreen(
    repository: BrewlogRepository,
    coffeeBagId: Long,
    segmentId: Long,
    segmentLabel: String,
    onBack: () -> Unit,
    onAddLog: () -> Unit,
    onEditLog: (PourLogEntity) -> Unit,
) {
    val logs by repository.observeLogs(coffeeBagId, segmentId).collectAsState(initial = emptyList())
    val segments by repository.observeAllSegments(coffeeBagId).collectAsState(initial = emptyList())
    val segment = segments.firstOrNull { it.id == segmentId }
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = segmentLabel,
                onBack = onBack,
                actionText = "Delete",
                onAction = { showDeleteConfirmation = true },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLog) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        BrewlogList(
            modifier = Modifier.padding(innerPadding),
        ) {
            item {
                Text(
                    text = "Brew logs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (logs.isEmpty()) {
                item {
                    Text(text = "No logs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(logs, key = { it.id }) { log ->
                PourLogCard(log = log, onClick = { onEditLog(log) })
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "Delete brew segment?") },
            text = { Text(text = "This deletes the segment and all logs inside it.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedSegment = segment
                        if (selectedSegment != null) {
                            scope.launch {
                                repository.deleteBrewSegment(selectedSegment)
                                showDeleteConfirmation = false
                                onBack()
                            }
                        } else {
                            showDeleteConfirmation = false
                            onBack()
                        }
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun PourLogCard(
    log: PourLogEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = log.brewedOn.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                log.rating?.let { Text(text = "$it/5", color = MaterialTheme.colorScheme.primary) }
            }
            Text(text = "${log.doseGrams.clean()} g coffee | ${log.waterGrams.clean()} g water | 1:${log.ratio().roundToInt()}")
            if (log.grindSize.isNotBlank()) {
                Text(text = "Grind: ${log.grindSize}")
            }
            if (log.recipe.isNotBlank()) {
                Text(text = log.recipe)
            }
            if (log.tastingNotes.isNotBlank()) {
                Text(text = "Tasting: ${log.tastingNotes}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (log.nextImprovements.isNotBlank()) {
                Text(text = "Next: ${log.nextImprovements}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChipRows(
    items: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    val isSelected = item == selected
                    AssistChip(
                        onClick = { onSelected(item) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            labelColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                        label = {
                            Text(text = item)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CoffeeImage(
    imagePath: String,
    modifier: Modifier = Modifier,
) {
    val image = remember(imagePath) {
        imagePath.takeIf { it.isNotBlank() }?.let {
            val bitmap = BitmapFactory.decodeFile(it) ?: return@let null
            val rotation = runCatching { ExifInterface(it).rotationDegrees }.getOrDefault(0)
            bitmap to rotation
        }
    }

    if (image == null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No image",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    } else {
        val (bitmap, rotation) = image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Coffee bag image",
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .rotate(rotation.toFloat()),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun BrewlogList(
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun CoffeeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun buildCoffeeBagSummary(coffeeBag: CoffeeBagSummary): String {
    val latest = coffeeBag.latestBrewedOn?.let { "Latest: $it" } ?: "No brews yet"
    val count = when (coffeeBag.logCount) {
        1L -> "1 log"
        else -> "${coffeeBag.logCount} logs"
    }
    return "$latest | $count"
}

private fun buildPourLogDraft(
    coffeeBagId: Long,
    logId: Long,
    brewedOn: String,
    brewStyle: String,
    brewer: String,
    doseGrams: String,
    waterGrams: String,
    equipmentDetails: String,
    grinderDetails: String,
    grindSize: String,
    recipe: String,
    waterDetails: String,
    tastingNotes: String,
    nextImprovements: String,
    rating: String,
    brewTimeSeconds: String,
) = PourLogDraft(
    id = logId,
    coffeeBagId = coffeeBagId,
    brewStyle = brewStyle,
    brewer = brewer,
    brewedOn = runCatching { LocalDate.parse(brewedOn.trim()) }
        .getOrElse { error("Date must use YYYY-MM-DD.") },
    doseGrams = doseGrams.toRequiredDouble("Coffee dose"),
    waterGrams = waterGrams.toRequiredDouble("Water amount"),
    equipmentDetails = equipmentDetails,
    grinderDetails = grinderDetails,
    grindSize = grindSize,
    recipe = recipe,
    waterDetails = waterDetails,
    tastingNotes = tastingNotes,
    nextImprovements = nextImprovements,
    rating = rating.toOptionalInt("Rating")?.also {
        require(it in 1..5) { "Rating must be from 1 to 5." }
    },
    brewTimeSeconds = brewTimeSeconds.toOptionalInt("Brew time"),
)

private fun String.toRequiredDouble(label: String): Double =
    toDoubleOrNull() ?: error("$label must be a number.")

private fun String.toOptionalInt(label: String): Int? {
    if (isBlank()) return null
    return toIntOrNull() ?: error("$label must be a whole number.")
}

private fun Double.clean(): String =
    if (this % 1.0 == 0.0) roundToInt().toString() else toString()
