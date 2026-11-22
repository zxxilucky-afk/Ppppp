package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.CycleCalculator
import com.example.domain.CycleSummary
import com.example.domain.FlowIntensity
import com.example.domain.PeriodRecord
import com.example.domain.UiState
import com.example.domain.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.CycleTrendChart
import com.example.ui.components.PinkoraEmptyState
import com.example.ui.components.PinkoraErrorState
import com.example.ui.components.PinkoraLoadingState
import com.example.ui.components.SoftGradientCard
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleHubScreen(
    initialSubTab: Int = 0,
    profile: UserProfile?,
    periodsState: UiState<List<PeriodRecord>>,
    cycleSummary: CycleSummary,
    language: AppLanguage,
    onSavePeriod: (recordId: String?, startEpoch: Long, endEpoch: Long, flow: FlowIntensity, notes: String) -> Unit,
    onDeletePeriod: (String) -> Unit,
) {
    val strings = AppStrings.get(language)
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab.coerceIn(0, 2)) }
    var showLogDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<PeriodRecord?>(null) }
    var dialogInitialStartEpoch by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }

    val periods = (periodsState as? UiState.Success)?.data ?: emptyList()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings.navCycle, style = MaterialTheme.typography.headlineMedium)
                Button(
                    onClick = {
                        editingRecord = null
                        dialogInitialStartEpoch = LocalDate.now().toEpochDay()
                        showLogDialog = true
                    },
                    modifier = Modifier.testTag("log_period_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.logPeriodBtn, color = PureWhite)
                }
            }

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = PinkPrimary,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(strings.calendarTitle) },
                    modifier = Modifier.testTag("cycle_tab_calendar"),
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(strings.cycleStatsTitle) },
                    modifier = Modifier.testTag("cycle_tab_stats"),
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(strings.periodHistoryTitle) },
                    modifier = Modifier.testTag("cycle_tab_history"),
                )
            }

            when (periodsState) {
                is UiState.Loading -> PinkoraLoadingState()
                is UiState.Error -> Box(Modifier.padding(16.dp)) { PinkoraErrorState(periodsState.message) }
                is UiState.Success -> {
                    when (selectedTab) {
                        0 -> PeriodCalendarTab(
                            records = periods,
                            summary = cycleSummary,
                            profile = profile,
                            language = language,
                            onDayClicked = { epochDay ->
                                editingRecord = null
                                dialogInitialStartEpoch = epochDay
                                showLogDialog = true
                            },
                        )
                        1 -> CycleStatsTab(
                            records = periods,
                            summary = cycleSummary,
                            profile = profile,
                            language = language,
                        )
                        2 -> PeriodHistoryTab(
                            records = periods,
                            language = language,
                            onEdit = { rec ->
                                editingRecord = rec
                                dialogInitialStartEpoch = rec.startDateEpochDay
                                showLogDialog = true
                            },
                            onDelete = onDeletePeriod,
                        )
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        PeriodLogDialog(
            existing = editingRecord,
            initialStartEpoch = dialogInitialStartEpoch,
            defaultLength = (profile?.averagePeriodLength ?: 5L).toInt(),
            language = language,
            onDismiss = { showLogDialog = false },
            onSave = { start, end, flow, notes ->
                onSavePeriod(editingRecord?.id, start, end, flow, notes)
                showLogDialog = false
            },
        )
    }
}

@Composable
private fun PeriodCalendarTab(
    records: List<PeriodRecord>,
    summary: CycleSummary,
    profile: UserProfile?,
    language: AppLanguage,
    onDayClicked: (Long) -> Unit,
) {
    val strings = AppStrings.get(language)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val todayEpoch = LocalDate.now().toEpochDay()
    val showOvulation = profile?.isTeen() == false && profile.ovulationTrackingEnabled

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, PinkSoftBorder),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                        }
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentMonth.year}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                            Text(
                                text = d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    val firstOfMonth = currentMonth.atDay(1)
                    val leadingEmpty = firstOfMonth.dayOfWeek.value - 1
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val totalCells = leadingEmpty + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - leadingEmpty + 1
                                if (dayNum in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNum)
                                    val epoch = date.toEpochDay()
                                    val isLogged = CycleCalculator.isLoggedPeriodDay(epoch, records, summary.averagePeriodLength)
                                    val isPredicted = !isLogged && CycleCalculator.isPredictedPeriodDay(epoch, summary)
                                    val isFertile = showOvulation && summary.fertileWindowRange?.contains(epoch) == true
                                    val isToday = epoch == todayEpoch

                                    val bgColor = when {
                                        isLogged -> PinkPrimary
                                        isPredicted -> PinkLight
                                        isFertile -> LavenderSurface
                                        else -> Color.Transparent
                                    }
                                    val textColor = when {
                                        isLogged -> PureWhite
                                        isPredicted -> PinkPrimary
                                        isFertile -> LavenderDeep
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .then(
                                                if (isToday) Modifier.border(2.dp, LavenderDeep, CircleShape) else Modifier
                                            )
                                            .clickable { onDayClicked(epoch) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textColor,
                                            fontWeight = if (isToday || isLogged) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LegendChip(PinkPrimary, if (language == AppLanguage.HI) "दर्ज पीरियड" else "Logged")
                        LegendChip(PinkLight, if (language == AppLanguage.HI) "अनुमानित" else "Estimated")
                        if (showOvulation) {
                            LegendChip(LavenderSurface, if (language == AppLanguage.HI) "फर्टाइल विंडो" else "Fertile (18+)")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = strings.estimateDisclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CycleStatsTab(
    records: List<PeriodRecord>,
    summary: CycleSummary,
    profile: UserProfile?,
    language: AppLanguage,
) {
    val strings = AppStrings.get(language)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (summary.isIrregular) {
            item {
                Surface(color = AmberLight, shape = MaterialTheme.shapes.medium) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = AmberNotice)
                        Text(strings.irregularWarning, style = MaterialTheme.typography.bodyMedium, color = AmberNotice)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftGradientCard(modifier = Modifier.weight(1f)) {
                    Text(
                        if (language == AppLanguage.HI) "औसत चक्र" else "Avg Cycle",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "${summary.averageCycleLength}d",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PinkPrimary,
                    )
                }
                SoftGradientCard(modifier = Modifier.weight(1f)) {
                    Text(
                        if (language == AppLanguage.HI) "औसत अवधि" else "Avg Period",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "${summary.averagePeriodLength}d",
                        style = MaterialTheme.typography.headlineMedium,
                        color = LavenderDeep,
                    )
                }
            }
        }

        item {
            SoftGradientCard {
                Text(
                    if (language == AppLanguage.HI) "पिछले चक्रों की लंबाई" else "Cycle Length Trend",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(10.dp))
                if (summary.observedCycleLengths.isEmpty()) {
                    Text(
                        if (language == AppLanguage.HI) {
                            "रुझान चार्ट देखने के लिए कम से कम 2 पीरियड दर्ज करें।"
                        } else {
                            "Log at least 2 periods to visualize cycle length trends."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    CycleTrendChart(cycleLengths = summary.observedCycleLengths)
                }
            }
        }

        if (profile?.isTeen() == false && profile.ovulationTrackingEnabled && summary.fertileWindowRange != null) {
            item {
                SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.6f), PureWhite)) {
                    Text(
                        if (language == AppLanguage.HI) "वैकल्पिक ओव्यूलेशन अनुमान (18+)" else "Optional Ovulation Estimate (18+)",
                        style = MaterialTheme.typography.titleMedium,
                        color = LavenderDeep,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${formatEpochDay(summary.fertileWindowRange.first)} – ${formatEpochDay(summary.fertileWindowRange.last)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(strings.estimateDisclaimer, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Text(
                "${if (language == AppLanguage.HI) "कुल दर्ज पीरियड्स" else "Total logged periods"}: ${records.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PeriodHistoryTab(
    records: List<PeriodRecord>,
    language: AppLanguage,
    onEdit: (PeriodRecord) -> Unit,
    onDelete: (String) -> Unit,
) {
    val strings = AppStrings.get(language)
    if (records.isEmpty()) {
        Box(Modifier.padding(16.dp)) {
            PinkoraEmptyState(strings.noRecordsYet)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(records, key = { it.id }) { rec ->
            val flowLabel = when (FlowIntensity.fromWire(rec.flowIntensity)) {
                FlowIntensity.SPOTTING -> strings.flowSpotting
                FlowIntensity.LIGHT -> strings.flowLight
                FlowIntensity.MEDIUM -> strings.flowMedium
                FlowIntensity.HEAVY -> strings.flowHeavy
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, PinkSoftBorder),
                modifier = Modifier.fillMaxWidth().testTag("period_history_card_${rec.id}"),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${formatEpochDay(rec.startDateEpochDay)} → ${if (rec.endDateEpochDay > 0) formatEpochDay(rec.endDateEpochDay) else "..."}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${strings.flowLabel}: $flowLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = PinkPrimary,
                        )
                        if (rec.notes.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(rec.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row {
                        IconButton(onClick = { onEdit(rec) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit, tint = LavenderDeep)
                        }
                        IconButton(onClick = { onDelete(rec.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = PinkPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodLogDialog(
    existing: PeriodRecord?,
    initialStartEpoch: Long,
    defaultLength: Int,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (startEpoch: Long, endEpoch: Long, flow: FlowIntensity, notes: String) -> Unit,
) {
    val strings = AppStrings.get(language)
    val todayEpoch = LocalDate.now().toEpochDay()
    var startEpoch by remember { mutableLongStateOf(existing?.startDateEpochDay ?: initialStartEpoch) }
    var durationDays by remember {
        mutableIntStateOf(
            if (existing != null && existing.endDateEpochDay >= existing.startDateEpochDay) {
                (existing.endDateEpochDay - existing.startDateEpochDay + 1).toInt().coerceIn(1, 14)
            } else {
                defaultLength.coerceIn(1, 14)
            }
        )
    }
    var flow by remember { mutableStateOf(FlowIntensity.fromWire(existing?.flowIntensity)) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) strings.logPeriodBtn else strings.edit) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${if (language == AppLanguage.HI) "प्रारंभ तिथि" else "Start date"}: ${formatEpochDay(startEpoch)}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { startEpoch = (startEpoch - 1).coerceAtLeast(todayEpoch - 365) }) { Text("-1d") }
                    TextButton(onClick = { startEpoch = todayEpoch }) { Text(if (language == AppLanguage.HI) "आज" else "Today") }
                    TextButton(onClick = { startEpoch = (startEpoch + 1).coerceAtMost(todayEpoch + 14) }) { Text("+1d") }
                }

                Text("${if (language == AppLanguage.HI) "अवधि" else "Duration"}: $durationDays ${if (language == AppLanguage.HI) "दिन" else "days"}")
                Slider(
                    value = durationDays.toFloat(),
                    onValueChange = { durationDays = it.roundToInt().coerceIn(1, 12) },
                    valueRange = 1f..12f,
                )

                Text(strings.flowLabel, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FlowIntensity.entries.forEach { f ->
                        val label = when (f) {
                            FlowIntensity.SPOTTING -> strings.flowSpotting
                            FlowIntensity.LIGHT -> strings.flowLight
                            FlowIntensity.MEDIUM -> strings.flowMedium
                            FlowIntensity.HEAVY -> strings.flowHeavy
                        }
                        FilterChip(
                            selected = flow == f,
                            onClick = { flow = f },
                            label = { Text(label, maxLines = 1) },
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(500) },
                    label = { Text(if (language == AppLanguage.HI) "नोट्स (वैकल्पिक)" else "Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val endEpoch = startEpoch + (durationDays - 1)
                    onSave(startEpoch, endEpoch, flow, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
            ) {
                Text(strings.save, color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        },
    )
}
