package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.CycleSummary
import com.example.domain.EducationalArticle
import com.example.domain.UserProfile
import com.example.domain.WellnessTip
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.CycleDayRing
import com.example.ui.components.DraftReviewBadge
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.PinkoraLogoMark
import com.example.ui.components.SoftGradientCard
import com.example.ui.components.formatEpochDayShort
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueLight

@Composable
fun HomeDashboardScreen(
    profile: UserProfile?,
    cycleSummary: CycleSummary,
    waterGlassesToday: Int,
    waterTarget: Int,
    tips: List<WellnessTip>,
    articles: List<EducationalArticle>,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onIncrementWater: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDailyCheckin: () -> Unit,
    onOpenSymptoms: () -> Unit,
    onOpenMood: () -> Unit,
    onOpenCareAssistant: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenArticle: (EducationalArticle) -> Unit,
) {
    val strings = AppStrings.get(language)
    val preferredName = profile?.preferredName?.ifBlank { "Friend" } ?: "Friend"
    val dailyTip = tips.firstOrNull()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PinkoraLogoMark(size = 42.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${strings.greetingPrefix}, $preferredName",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = strings.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    LanguageTogglePill(
                        currentLanguage = language,
                        onLanguageChange = onLanguageChange,
                    )
                }
            }

            item {
                SoftGradientCard(
                    colors = listOf(PureWhite, PinkLight.copy(alpha = 0.65f), LavenderSurface.copy(alpha = 0.55f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CycleDayRing(summary = cycleSummary, strings = strings)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.nextPeriodEstimate,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            if (cycleSummary.estimatedNextPeriodStartEpochDay != null && cycleSummary.daysUntilNextPeriod != null) {
                                Text(
                                    text = formatEpochDayShort(cycleSummary.estimatedNextPeriodStartEpochDay),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PinkPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${cycleSummary.daysUntilNextPeriod} ${strings.daysRemaining}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LavenderDeep,
                                )
                            } else {
                                Text(
                                    text = strings.logFirstPeriodPrompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = onOpenCalendar,
                                modifier = Modifier.testTag("home_calendar_shortcut"),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = PureWhite),
                            ) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.calendarTitle, color = PinkPrimary)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = strings.estimateDisclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (cycleSummary.isIrregular) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = AmberLight,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = AmberNotice, modifier = Modifier.size(18.dp))
                                Text(strings.irregularWarning, style = MaterialTheme.typography.bodySmall, color = AmberNotice)
                            }
                        }
                    }
                }
            }

            item {
                // Quick Actions 2x3 Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        QuickTile(
                            title = strings.dailyCheckinTitle,
                            icon = Icons.Rounded.EditNote,
                            bg = PinkLight,
                            tint = PinkPrimary,
                            modifier = Modifier.weight(1f).testTag("home_daily_checkin_btn"),
                            onClick = onOpenDailyCheckin,
                        )
                        QuickTile(
                            title = strings.painSymptomTitle,
                            icon = Icons.Rounded.Healing,
                            bg = LavenderSurface,
                            tint = LavenderDeep,
                            modifier = Modifier.weight(1f).testTag("home_symptom_btn"),
                            onClick = onOpenSymptoms,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        QuickTile(
                            title = strings.moodTrackerTitle,
                            icon = Icons.Rounded.Mood,
                            bg = LavenderSurface,
                            tint = LavenderDeep,
                            modifier = Modifier.weight(1f).testTag("home_mood_btn"),
                            onClick = onOpenMood,
                        )
                        QuickTile(
                            title = strings.diaryTitle,
                            icon = Icons.Rounded.Lock,
                            bg = PinkLight,
                            tint = PinkPrimary,
                            modifier = Modifier.weight(1f).testTag("home_diary_btn"),
                            onClick = onOpenDiary,
                        )
                    }
                }
            }

            item {
                // Water & AI Care Assistant Row
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WaterBlueLight),
                    border = BorderStroke(1.dp, WaterBlue.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.LocalDrink, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(strings.waterTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$waterGlassesToday / $waterTarget ${strings.glassesToday}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WaterBlue,
                                )
                            }
                        }
                        Button(
                            onClick = onIncrementWater,
                            modifier = Modifier.testTag("home_add_water_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                        ) {
                            Text(strings.addGlass, color = PureWhite)
                        }
                    }
                }
            }

            item {
                SoftGradientCard(
                    colors = listOf(LavenderSurface.copy(alpha = 0.7f), PureWhite),
                    modifier = Modifier.clickable { onOpenCareAssistant() }.testTag("home_ai_shortcut"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = LavenderDeep, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(strings.aiAssistantTitle, style = MaterialTheme.typography.titleMedium, color = LavenderDeep)
                                Text(
                                    if (language == AppLanguage.HI) {
                                        "स्वच्छता, आराम और देखभाल से जुड़े प्रश्न पूछें"
                                    } else {
                                        "Ask gentle questions on hygiene, comfort & wellness"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = strings.notificationsTitle, tint = PinkPrimary,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onOpenNotifications() }
                                .testTag("home_notifications_shortcut"))
                    }
                }
            }

            if (dailyTip != null) {
                item {
                    SoftGradientCard {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Spa, contentDescription = null, tint = PinkPrimary)
                            Column {
                                Text(
                                    if (language == AppLanguage.HI) "आज का स्वास्थ्य सुझाव" else "Today's Wellness Tip",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PinkPrimary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) dailyTip.textHi else dailyTip.textEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (language == AppLanguage.HI) "शैक्षिक लेख" else "Featured Care Guides",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            items(articles.take(3), key = { it.id }) { article ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenArticle(article) }
                        .testTag("home_article_${article.id}"),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, PinkSoftBorder),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DraftReviewBadge(strings = strings)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.HI) article.titleHi else article.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.HI) article.summaryHi else article.summaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    icon: ImageVector,
    bg: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = bg.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
            )
        }
    }
}
