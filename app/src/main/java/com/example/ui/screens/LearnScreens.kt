package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EducationalArticle
import com.example.domain.SeedContent
import com.example.domain.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.DraftReviewBadge
import com.example.ui.components.SoftGradientCard
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintWellness
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueLight
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LearnHubScreen(
    articles: List<EducationalArticle>,
    profile: UserProfile?,
    waterGlassesToday: Int,
    waterTarget: Int,
    language: AppLanguage,
    onUpdateWater: (Int) -> Unit,
    onUpdateWaterTarget: (Int) -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("all") }
    var activeArticle by remember { mutableStateOf<EducationalArticle?>(null) }

    val filteredArticles = remember(articles, selectedCategory, profile) {
        articles.filter { art ->
            val catOk = selectedCategory == "all" || art.category == selectedCategory
            val ageOk = if (profile?.isTeen() == true) {
                art.ageAppropriateFor != "adult_18_plus"
            } else {
                true
            }
            catOk && ageOk
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Text(
                text = strings.learnTitle,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = PinkPrimary,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(strings.hygieneCategory, maxLines = 1) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(strings.mythsCategory, maxLines = 1) },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(strings.nutritionCategory, maxLines = 1) },
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(strings.waterTitle, maxLines = 1) },
                )
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    "all" to (if (language == AppLanguage.HI) "सभी" else "All"),
                                    "first_period" to strings.firstPeriodCategory,
                                    "hygiene" to strings.hygieneCategory,
                                    "self_care" to strings.selfCareCategory,
                                    "when_to_see_doctor" to strings.doctorCategory,
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = selectedCategory == key,
                                        onClick = { selectedCategory = key },
                                        label = { Text(label) },
                                    )
                                }
                            }
                        }

                        items(filteredArticles, key = { it.id }) { article ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeArticle = article }
                                    .testTag("learn_article_${article.id}"),
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
                    }
                }

                1 -> {
                    // Myths vs Facts
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(SeedContent.mythsVsFacts, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, PinkSoftBorder),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Surface(color = PinkLight, shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = if (language == AppLanguage.HI) item.mythHi else item.mythEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = PinkPrimary,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Surface(color = MintLight, shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = if (language == AppLanguage.HI) item.factHi else item.factEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MintWellness,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Food & Nutrition Center
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.img_nutrition_hero),
                                contentDescription = strings.nutritionCategory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item {
                            Surface(color = LavenderSurface, shape = MaterialTheme.shapes.small) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = LavenderDeep)
                                    Text(strings.nutritionDisclaimer, style = MaterialTheme.typography.bodySmall, color = LavenderDeep)
                                }
                            }
                        }
                        items(SeedContent.nutritionGuides, key = { it.id }) { guide ->
                            SoftGradientCard {
                                Text(
                                    text = if (language == AppLanguage.HI) guide.titleHi else guide.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PinkPrimary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) guide.foodsHi else guide.foodsEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) guide.benefitHi else guide.benefitEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // Water Reminder & Tracker
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WaterBlueLight),
                                border = BorderStroke(1.dp, WaterBlue.copy(alpha = 0.3f)),
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.LocalDrink, contentDescription = null, tint = WaterBlue)
                                        Spacer(Modifier.width(8.dp))
                                        Text(strings.waterTitle, style = MaterialTheme.typography.titleLarge)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "$waterGlassesToday / $waterTarget ${strings.glassesToday}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = WaterBlue,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (waterGlassesToday.toFloat() / waterTarget.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(MaterialTheme.shapes.small),
                                        color = WaterBlue,
                                        trackColor = PureWhite,
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { onUpdateWater(-1) }) {
                                            Text("-1")
                                        }
                                        Button(
                                            onClick = { onUpdateWater(1) },
                                            modifier = Modifier.testTag("learn_add_water_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                                        ) {
                                            Text(strings.addGlass, color = PureWhite)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            SoftGradientCard {
                                var sliderTarget by remember(waterTarget) { mutableIntStateOf(waterTarget) }
                                Text(
                                    "${if (language == AppLanguage.HI) "दैनिक लक्ष्य" else "Daily Target"}: $sliderTarget",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Slider(
                                    value = sliderTarget.toFloat(),
                                    onValueChange = { sliderTarget = it.roundToInt().coerceIn(4, 16) },
                                    onValueChangeFinished = { onUpdateWaterTarget(sliderTarget) },
                                    valueRange = 4f..16f,
                                )
                                OutlinedButton(onClick = onOpenNotifications) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.notificationsTitle)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeArticle != null) {
        val art = activeArticle!!
        AlertDialog(
            onDismissRequest = { activeArticle = null },
            title = {
                Column {
                    DraftReviewBadge(strings = strings)
                    Spacer(Modifier.height(6.dp))
                    Text(if (language == AppLanguage.HI) art.titleHi else art.titleEn)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (language == AppLanguage.HI) art.bodyHi else art.bodyEn,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeArticle = null }) {
                    Text(strings.close)
                }
            },
        )
    }
}

@Composable
fun ArticleDetailDialog(
    article: EducationalArticle,
    language: AppLanguage,
    onDismiss: () -> Unit,
) {
    val strings = AppStrings.get(language)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                DraftReviewBadge(strings = strings)
                Spacer(Modifier.height(6.dp))
                Text(if (language == AppLanguage.HI) article.titleHi else article.titleEn)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = if (language == AppLanguage.HI) article.bodyHi else article.bodyEn,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        },
    )
}
