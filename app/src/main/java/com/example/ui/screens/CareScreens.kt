package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.MoodRecord
import com.example.domain.MoodType
import com.example.domain.SeedContent
import com.example.domain.SymptomRecord
import com.example.domain.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.MoodFrequencyChart
import com.example.ui.components.PinkoraEmptyState
import com.example.ui.components.PinkoraErrorState
import com.example.ui.components.PinkoraLoadingState
import com.example.ui.components.RedFlagWarningCard
import com.example.ui.components.SoftGradientCard
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun CareHubScreen(
    initialSubTab: Int = 0,
    symptomsState: UiState<List<SymptomRecord>>,
    moodsState: UiState<List<MoodRecord>>,
    language: AppLanguage,
    onLogSymptoms: (Long, List<String>, Int, String) -> Unit,
    onDeleteSymptom: (String) -> Unit,
    onLogMood: (Long, MoodType, Int, String) -> Unit,
    onDeleteMood: (String) -> Unit,
    onOpenDiary: () -> Unit,
    onOpenAiAssistant: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab.coerceIn(0, 3)) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings.navCare, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenDiary,
                        modifier = Modifier.testTag("care_open_diary_btn"),
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.diaryTitle)
                    }
                    Button(
                        onClick = onOpenAiAssistant,
                        modifier = Modifier.testTag("care_open_ai_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI", color = PureWhite)
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = PinkPrimary,
                edgePadding = 12.dp,
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(strings.dailyCheckinTitle) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(strings.painSymptomTitle) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(strings.moodTrackerTitle) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text(strings.selfCareTitle) })
            }

            when (selectedTab) {
                0 -> DailyCheckinTab(
                    language = language,
                    onSaveCheckin = { symptoms, pain, mood, energy, notes ->
                        val today = LocalDate.now().toEpochDay()
                        if (symptoms.isNotEmpty() || pain > 0) {
                            onLogSymptoms(today, symptoms, pain, notes)
                        }
                        onLogMood(today, mood, energy, notes)
                    },
                )
                1 -> PainSymptomTab(
                    symptomsState = symptomsState,
                    language = language,
                    onLogSymptoms = onLogSymptoms,
                    onDeleteSymptom = onDeleteSymptom,
                )
                2 -> MoodTrackerTab(
                    moodsState = moodsState,
                    language = language,
                    onLogMood = onLogMood,
                    onDeleteMood = onDeleteMood,
                )
                3 -> SelfCareCenterTab(language = language)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyCheckinTab(
    language: AppLanguage,
    onSaveCheckin: (List<String>, Int, MoodType, Int, String) -> Unit,
) {
    val strings = AppStrings.get(language)
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var pain by remember { mutableIntStateOf(2) }
    var mood by remember { mutableStateOf(MoodType.RELAXED) }
    var energy by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SoftGradientCard {
                Text(strings.moodTrackerTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MoodType.entries.forEach { m ->
                        FilterChip(
                            selected = mood == m,
                            onClick = { mood = m },
                            label = { Text(strings.moodNames[m.wireValue] ?: m.wireValue) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("${strings.energyLevelLabel}: $energy / 5")
                Slider(
                    value = energy.toFloat(),
                    onValueChange = { energy = it.roundToInt().coerceIn(1, 5) },
                    valueRange = 1f..5f,
                )
            }
        }

        item {
            SoftGradientCard {
                Text(strings.painSymptomTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    strings.symptomNames.forEach { (key, label) ->
                        val active = key in selectedSymptoms
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedSymptoms = if (active) selectedSymptoms - key else (selectedSymptoms + key).take(10).toSet()
                            },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("${strings.painIntensityLabel}: $pain / 10")
                Slider(
                    value = pain.toFloat(),
                    onValueChange = { pain = it.roundToInt().coerceIn(0, 10) },
                    valueRange = 0f..10f,
                )
                if (pain >= 8 || "fainting_heavy" in selectedSymptoms) {
                    Spacer(Modifier.height(8.dp))
                    RedFlagWarningCard(strings = strings)
                }
            }
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it.take(500) },
                label = { Text(if (language == AppLanguage.HI) "दैनिक नोट (वैकल्पिक)" else "Daily note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    onSaveCheckin(selectedSymptoms.toList(), pain, mood, energy, notes)
                    notes = ""
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_daily_checkin_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, tint = PureWhite)
                Spacer(Modifier.width(8.dp))
                Text(strings.save, color = PureWhite)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PainSymptomTab(
    symptomsState: UiState<List<SymptomRecord>>,
    language: AppLanguage,
    onLogSymptoms: (Long, List<String>, Int, String) -> Unit,
    onDeleteSymptom: (String) -> Unit,
) {
    val strings = AppStrings.get(language)
    var selected by remember { mutableStateOf(setOf<String>()) }
    var pain by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SoftGradientCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Healing, contentDescription = null, tint = PinkPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.painSymptomTitle, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    strings.symptomNames.forEach { (key, label) ->
                        val active = key in selected
                        FilterChip(
                            selected = active,
                            onClick = {
                                selected = if (active) selected - key else (selected + key).take(10).toSet()
                            },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("${strings.painIntensityLabel}: $pain / 10")
                Slider(
                    value = pain.toFloat(),
                    onValueChange = { pain = it.roundToInt().coerceIn(0, 10) },
                    valueRange = 0f..10f,
                )
                if (pain >= 8 || "fainting_heavy" in selected) {
                    Spacer(Modifier.height(8.dp))
                    RedFlagWarningCard(strings = strings)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(500) },
                    label = { Text(if (language == AppLanguage.HI) "विवरण (वैकल्पिक)" else "Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onLogSymptoms(LocalDate.now().toEpochDay(), selected.toList(), pain, notes)
                        notes = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_symptom_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            }
        }

        when (symptomsState) {
            is UiState.Loading -> item { PinkoraLoadingState() }
            is UiState.Error -> item { PinkoraErrorState(symptomsState.message) }
            is UiState.Success -> {
                if (symptomsState.data.isEmpty()) {
                    item { PinkoraEmptyState(strings.noRecordsYet) }
                } else {
                    items(symptomsState.data, key = { it.id }) { rec ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, PinkSoftBorder),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(formatEpochDay(rec.dateEpochDay), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${strings.painIntensityLabel}: ${rec.painIntensity}/10 • " +
                                            rec.symptoms.joinToString(", ") { strings.symptomNames[it] ?: it },
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    if (rec.redFlagAlert) {
                                        Text(
                                            strings.seekMedicalHelpTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteSymptom(rec.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = PinkPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodTrackerTab(
    moodsState: UiState<List<MoodRecord>>,
    language: AppLanguage,
    onLogMood: (Long, MoodType, Int, String) -> Unit,
    onDeleteMood: (String) -> Unit,
) {
    val strings = AppStrings.get(language)
    var selectedMood by remember { mutableStateOf(MoodType.RELAXED) }
    var energy by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.55f), PureWhite)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Mood, contentDescription = null, tint = LavenderDeep)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.moodTrackerTitle, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MoodType.entries.forEach { m ->
                        FilterChip(
                            selected = selectedMood == m,
                            onClick = { selectedMood = m },
                            label = { Text(strings.moodNames[m.wireValue] ?: m.wireValue) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("${strings.energyLevelLabel}: $energy / 5")
                Slider(
                    value = energy.toFloat(),
                    onValueChange = { energy = it.roundToInt().coerceIn(1, 5) },
                    valueRange = 1f..5f,
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(500) },
                    label = { Text(if (language == AppLanguage.HI) "मूड नोट (वैकल्पिक)" else "Mood note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onLogMood(LocalDate.now().toEpochDay(), selectedMood, energy, notes)
                        notes = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_mood_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            }
        }

        when (moodsState) {
            is UiState.Loading -> item { PinkoraLoadingState() }
            is UiState.Error -> item { PinkoraErrorState(moodsState.message) }
            is UiState.Success -> {
                if (moodsState.data.isNotEmpty()) {
                    item {
                        SoftGradientCard {
                            Text(
                                if (language == AppLanguage.HI) "मूड पैटर्न" else "Mood Pattern",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                            MoodFrequencyChart(moods = moodsState.data, strings = strings)
                        }
                    }
                    items(moodsState.data, key = { it.id }) { rec ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, PinkSoftBorder),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${formatEpochDay(rec.dateEpochDay)} • ${strings.moodNames[rec.mood] ?: rec.mood}",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        "${strings.energyLevelLabel}: ${rec.energyLevel}/5" +
                                            if (rec.notes.isNotBlank()) " • ${rec.notes}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                IconButton(onClick = { onDeleteMood(rec.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = PinkPrimary)
                                }
                            }
                        }
                    }
                } else {
                    item { PinkoraEmptyState(strings.noRecordsYet) }
                }
            }
        }
    }
}

@Composable
private fun SelfCareCenterTab(language: AppLanguage) {
    val strings = AppStrings.get(language)
    var breathingRunning by remember { mutableStateOf(false) }
    var phaseIndex by remember { mutableIntStateOf(0) } // 0 = Inhale 4s, 1 = Hold 4s, 2 = Exhale 6s

    LaunchedEffect(breathingRunning) {
        while (breathingRunning) {
            phaseIndex = 0
            delay(4000)
            phaseIndex = 1
            delay(4000)
            phaseIndex = 2
            delay(6000)
        }
    }

    val targetScale = when {
        !breathingRunning -> 0.72f
        phaseIndex == 0 -> 1.0f
        phaseIndex == 1 -> 1.0f
        else -> 0.65f
    }
    val animScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = if (phaseIndex == 2) 5500 else 3500),
        label = "breath_scale",
    )

    val phaseText = when {
        !breathingRunning -> if (language == AppLanguage.HI) "तैयार" else "Ready"
        phaseIndex == 0 -> if (language == AppLanguage.HI) "सांस अंदर लें (4s)" else "Inhale (4s)"
        phaseIndex == 1 -> if (language == AppLanguage.HI) "रोकें (4s)" else "Hold (4s)"
        else -> if (language == AppLanguage.HI) "धीरे छोड़ें (6s)" else "Exhale (6s)"
    }

    val dailyPositive = remember(language) {
        val list = if (language == AppLanguage.HI) SeedContent.positiveMessagesHi else SeedContent.positiveMessagesEn
        list[(LocalDate.now().dayOfYear % list.size)]
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.img_selfcare_calm),
                contentDescription = strings.selfCareTitle,
                modifier = Modifier.fillMaxWidth().height(175.dp).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
        }

        item {
            SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.6f), PureWhite)) {
                Text(strings.breathingTitle, style = MaterialTheme.typography.titleLarge, color = LavenderDeep)
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(animScale)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(PinkLight, LavenderSurface))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(phaseText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LavenderDeep)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { breathingRunning = !breathingRunning },
                    modifier = Modifier.fillMaxWidth().testTag("breathing_toggle_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                ) {
                    Text(if (breathingRunning) strings.stopBreathing else strings.startBreathing, color = PureWhite)
                }
            }
        }

        item {
            SoftGradientCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null, tint = PinkPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (language == AppLanguage.HI) "आज का सकारात्मक संदेश" else "Daily Positive Message",
                        style = MaterialTheme.typography.titleMedium,
                        color = PinkPrimary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(dailyPositive, style = MaterialTheme.typography.bodyLarge)
            }
        }

        item {
            SoftGradientCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Spa, contentDescription = null, tint = LavenderDeep)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (language == AppLanguage.HI) "गर्म सिकाई और आराम सुझाव" else "Warm Compress & Relaxation Tips",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (language == AppLanguage.HI) {
                        "• पेट के निचले हिस्से पर 15-20 मिनट गुनगुना सेंक करें।\n• हल्की स्ट्रेचिंग और पर्याप्त नींद लें।\n• गुनगुना पानी या अदरक चाय पिएं।"
                    } else {
                        "• Apply a warm water bag on your lower abdomen for 15–20 minutes.\n• Try gentle knee-to-chest stretches and rest well.\n• Sip warm water or soothing herbal tea."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
