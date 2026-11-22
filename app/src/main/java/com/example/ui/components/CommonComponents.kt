package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.CyclePhase
import com.example.domain.CycleSummary
import com.example.domain.MoodRecord
import com.example.i18n.AppLanguage
import com.example.i18n.PinkoraText
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatEpochDay(epochDay: Long): String {
    if (epochDay <= 0L) return "—"
    return LocalDate.ofEpochDay(epochDay)
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
}

fun formatEpochDayShort(epochDay: Long): String {
    if (epochDay <= 0L) return "—"
    return LocalDate.ofEpochDay(epochDay)
        .format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
}

@Composable
fun PinkoraLogoMark(size: Dp = 36.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(PinkLight, LavenderSurface))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = "PINKORA logo",
            tint = PinkPrimary,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

@Composable
fun LanguageTogglePill(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag("language_toggle_pill"),
        shape = RoundedCornerShape(50),
        color = PureWhite,
        border = BorderStroke(1.dp, PinkSoftBorder),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLanguage.entries.forEach { lang ->
                val selected = lang == currentLanguage
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) PinkPrimary else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onLanguageChange(lang) }
                        .testTag("lang_option_${lang.code}"),
                ) {
                    Text(
                        text = if (lang == AppLanguage.EN) "EN" else "हिन्दी",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SoftGradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(PureWhite, PinkLight.copy(alpha = 0.45f)),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, PinkSoftBorder.copy(alpha = 0.7f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun CycleDayRing(
    summary: CycleSummary,
    strings: PinkoraText,
    modifier: Modifier = Modifier,
) {
    val cycleLen = summary.averageCycleLength.coerceAtLeast(1)
    val day = summary.currentCycleDay ?: 0
    val progress = if (day > 0) (day.toFloat() / cycleLen.toFloat()).coerceIn(0.04f, 1f) else 0f
    val phaseLabel = when (summary.currentPhase) {
        CyclePhase.MENSTRUAL -> strings.phaseMenstrual
        CyclePhase.FOLLICULAR -> strings.phaseFollicular
        CyclePhase.OVULATORY -> strings.phaseOvulatory
        CyclePhase.LUTEAL -> strings.phaseLuteal
        CyclePhase.UNKNOWN -> strings.phaseUnknown
    }

    Box(
        modifier = modifier.size(136.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = PinkLight,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(PinkPrimary, LavenderDeep, PinkPrimary)),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (day > 0) {
                Text(
                    text = strings.cycleDay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.displaySmall,
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = phaseLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = LavenderDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            } else {
                Text(
                    text = phaseLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = PinkPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
fun CycleTrendChart(
    cycleLengths: List<Int>,
    modifier: Modifier = Modifier,
) {
    if (cycleLengths.isEmpty()) return
    val maxVal = (cycleLengths.maxOrNull() ?: 35).coerceAtLeast(35)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 8.dp),
        ) {
            val count = cycleLengths.size
            val slotWidth = size.width / count.coerceAtLeast(1)
            val barWidth = (slotWidth * 0.52f).coerceAtMost(28.dp.toPx())
            cycleLengths.forEachIndexed { idx, len ->
                val ratio = (len.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1f)
                val barHeight = size.height * ratio
                val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                val y = size.height - barHeight
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(PinkPrimary, LavenderDeep)),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            cycleLengths.forEach { len ->
                Text(
                    text = "${len}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun MoodFrequencyChart(
    moods: List<MoodRecord>,
    strings: PinkoraText,
    modifier: Modifier = Modifier,
) {
    if (moods.isEmpty()) return
    val grouped = moods.groupingBy { it.mood }.eachCount()
        .entries.sortedByDescending { it.value }.take(5)
    val maxCount = (grouped.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        grouped.forEach { (moodKey, count) ->
            val label = strings.moodNames[moodKey] ?: moodKey
            val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.1f, 1f)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(86.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkLight),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(PinkPrimary, LavenderDeep))),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun RedFlagWarningCard(strings: PinkoraText, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("red_flag_warning_card"),
        colors = CardDefaults.cardColors(containerColor = AlertRedLight),
        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f)),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.MedicalServices,
                contentDescription = strings.seekMedicalHelpTitle,
                tint = AlertRed,
            )
            Column {
                Text(
                    text = strings.seekMedicalHelpTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlertRed,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.seekMedicalHelpBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun DraftReviewBadge(strings: PinkoraText, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = AmberLight,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AmberNotice.copy(alpha = 0.35f)),
    ) {
        Text(
            text = strings.draftPendingReviewBadge,
            style = MaterialTheme.typography.labelSmall,
            color = AmberNotice,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun PinkoraLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = PinkPrimary)
    }
}

@Composable
fun PinkoraEmptyState(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = LavenderSurface.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Rounded.Spa,
                contentDescription = null,
                tint = LavenderDeep,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PinkoraErrorState(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = AlertRedLight,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AlertRed)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AlertRed,
            )
        }
    }
}
