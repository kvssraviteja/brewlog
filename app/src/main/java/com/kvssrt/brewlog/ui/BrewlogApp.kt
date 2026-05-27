package com.kvssrt.brewlog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kvssrt.brewlog.data.BrewlogRepository
import com.kvssrt.brewlog.data.CoffeeBagDraft
import com.kvssrt.brewlog.data.CoffeeBagEntity
import com.kvssrt.brewlog.data.CoffeeBagSummary
import com.kvssrt.brewlog.data.PourLogDraft
import com.kvssrt.brewlog.data.PourLogEntity
import com.kvssrt.brewlog.data.PourMethodEntity
import com.kvssrt.brewlog.data.presetPourMethods
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private sealed interface BrewlogScreen {
    data object Home : BrewlogScreen
    data object AddCoffeeBag : BrewlogScreen
    data class CoffeeBagDetail(val coffeeBagId: Long) : BrewlogScreen
    data class AddPourLog(val coffeeBagId: Long) : BrewlogScreen
    data class MethodLogs(val coffeeBagId: Long, val methodId: Long, val methodName: String) : BrewlogScreen
}

@Composable
fun BrewlogApp(
    repository: BrewlogRepository,
) {
    var screen by remember { mutableStateOf<BrewlogScreen>(BrewlogScreen.Home) }

    when (val current = screen) {
        BrewlogScreen.Home -> HomeScreen(
            repository = repository,
            onAddCoffeeBag = { screen = BrewlogScreen.AddCoffeeBag },
            onOpenCoffeeBag = { screen = BrewlogScreen.CoffeeBagDetail(it) },
        )

        BrewlogScreen.AddCoffeeBag -> AddCoffeeBagScreen(
            repository = repository,
            onBack = { screen = BrewlogScreen.Home },
            onCreated = { screen = BrewlogScreen.CoffeeBagDetail(it) },
        )

        is BrewlogScreen.CoffeeBagDetail -> CoffeeBagDetailScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            onBack = { screen = BrewlogScreen.Home },
            onAddLog = { screen = BrewlogScreen.AddPourLog(current.coffeeBagId) },
            onOpenMethod = { method ->
                screen = BrewlogScreen.MethodLogs(
                    coffeeBagId = current.coffeeBagId,
                    methodId = method.id,
                    methodName = method.name,
                )
            },
        )

        is BrewlogScreen.AddPourLog -> AddPourLogScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            onBack = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
            onCreated = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
        )

        is BrewlogScreen.MethodLogs -> MethodLogsScreen(
            repository = repository,
            coffeeBagId = current.coffeeBagId,
            methodId = current.methodId,
            methodName = current.methodName,
            onBack = { screen = BrewlogScreen.CoffeeBagDetail(current.coffeeBagId) },
        )
    }
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
        topBar = {
            TopAppBar(title = { Text(text = "Brewlog") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCoffeeBag) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        text = "Add a coffee bag to start logging daily pour overs.",
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
        Column(
            modifier = Modifier.padding(16.dp),
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
            Text(
                text = buildCoffeeBagSummary(coffeeBag),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCoffeeBagScreen(
    repository: BrewlogRepository,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var roaster by remember { mutableStateOf("") }
    var beanDetails by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add coffee bag") },
                navigationIcon = { TextButton(onClick = onBack) { Text(text = "Back") } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CoffeeTextField(value = name, onValueChange = { name = it }, label = "Coffee name")
            }
            item {
                CoffeeTextField(value = roaster, onValueChange = { roaster = it }, label = "Roaster")
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
                            runCatching {
                                repository.addCoffeeBag(
                                    CoffeeBagDraft(
                                        name = name,
                                        roaster = roaster,
                                        beanDetails = beanDetails,
                                    ),
                                )
                            }.onSuccess(onCreated)
                                .onFailure { error = it.message ?: "Could not add coffee bag." }
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
    onAddLog: () -> Unit,
    onOpenMethod: (PourMethodEntity) -> Unit,
) {
    val coffeeBag by repository.observeCoffeeBag(coffeeBagId).collectAsState(initial = null)
    val methods by repository.observeMethods(coffeeBagId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = coffeeBag?.name ?: "Coffee bag") },
                navigationIcon = { TextButton(onClick = onBack) { Text(text = "Back") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLog) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            coffeeBag?.let {
                item { CoffeeBagDetails(coffeeBag = it) }
            }
            item {
                Text(
                    text = "Pour methods",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (methods.isEmpty()) {
                item {
                    Text(
                        text = "Add a log to create this bag's first pour method.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(methods, key = { it.id }) { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMethod(method) },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = method.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "View logs for this method",
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = coffeeBag.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (coffeeBag.roaster.isNotBlank()) {
                Text(text = coffeeBag.roaster, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (coffeeBag.beanDetails.isNotBlank()) {
                Text(text = coffeeBag.beanDetails)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPourLogScreen(
    repository: BrewlogRepository,
    coffeeBagId: Long,
    onBack: () -> Unit,
    onCreated: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var brewedOn by remember { mutableStateOf(LocalDate.now().toString()) }
    var methodName by remember { mutableStateOf(presetPourMethods.first()) }
    var doseGrams by remember { mutableStateOf("") }
    var waterGrams by remember { mutableStateOf("") }
    var equipmentDetails by remember { mutableStateOf("") }
    var grinderDetails by remember { mutableStateOf("") }
    var grindSize by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var waterDetails by remember { mutableStateOf("") }
    var tastingNotes by remember { mutableStateOf("") }
    var nextImprovements by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var brewTimeSeconds by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add pour log") },
                navigationIcon = { TextButton(onClick = onBack) { Text(text = "Back") } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CoffeeTextField(value = brewedOn, onValueChange = { brewedOn = it }, label = "Date")
            }
            item {
                Text(text = "Pour method", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                PresetMethodChips(selected = methodName, onSelected = { methodName = it })
                CoffeeTextField(
                    value = methodName,
                    onValueChange = { methodName = it },
                    label = "Method name",
                )
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
                                brewedOn = brewedOn,
                                methodName = methodName,
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
                                runCatching { repository.addPourLog(draft) }
                                    .onSuccess { onCreated() }
                                    .onFailure { error = it.message ?: "Could not add pour log." }
                            }
                        }.onFailure {
                            error = it.message ?: "Check the log details."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Save pour log")
                }
            }
        }
    }
}

@Composable
private fun PresetMethodChips(
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presetPourMethods.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { method ->
                    AssistChip(
                        onClick = { onSelected(method) },
                        label = {
                            Text(
                                text = if (method == selected) "$method selected" else method,
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodLogsScreen(
    repository: BrewlogRepository,
    coffeeBagId: Long,
    methodId: Long,
    methodName: String,
    onBack: () -> Unit,
) {
    val logs by repository.observeLogs(coffeeBagId, methodId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = methodName) },
                navigationIcon = { TextButton(onClick = onBack) { Text(text = "Back") } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Pour logs",
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
                PourLogCard(log = log)
            }
        }
    }
}

@Composable
private fun PourLogCard(log: PourLogEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
    brewedOn: String,
    methodName: String,
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
    coffeeBagId = coffeeBagId,
    methodName = methodName,
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
