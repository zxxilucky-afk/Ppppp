package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.domain.AgeGroup
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.onGoogleSignInClicked
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.PinkoraErrorState
import com.example.ui.components.PinkoraLogoMark
import com.example.ui.components.SoftGradientCard
import com.example.ui.theme.BlushBackground
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun SplashScreen(language: AppLanguage) {
    val strings = AppStrings.get(language)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PinkLight, BlushBackground, LavenderSurface))
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            PinkoraLogoMark(size = 84.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.displaySmall,
                color = PinkPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = strings.tagline,
                style = MaterialTheme.typography.titleMedium,
                color = LavenderDeep,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = PinkPrimary, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun AuthWelcomeScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAuthSuccess: () -> Unit,
) {
    val strings = AppStrings.get(language)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BlushBackground, PinkLight.copy(alpha = 0.45f), LavenderSurface.copy(alpha = 0.5f)))
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PinkoraLogoMark(size = 40.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleLarge,
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LanguageTogglePill(
                    currentLanguage = language,
                    onLanguageChange = onLanguageChange,
                )
            }

            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.img_welcome_hero),
                contentDescription = strings.tagline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = strings.tagline,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = strings.welcomeSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            SoftGradientCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = PinkPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.authNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (authError != null) {
                Spacer(Modifier.height(12.dp))
                PinkoraErrorState(message = authError!!)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    authError = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        scope = scope,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            authError = msg
                        },
                        onAuthCancelled = {
                            isLoading = false
                        },
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = strings.continueWithGoogle,
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = if (language == AppLanguage.HI) {
                    "बैकएंड Firebase Google साइन-इन द्वारा सुरक्षित है।"
                } else {
                    "Secured by Firebase Authentication with Google Sign-In."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun OnboardingFlowScreen(
    initialName: String,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onComplete: (
        preferredName: String,
        ageGroup: AgeGroup,
        language: AppLanguage,
        parentalConsentConfirmed: Boolean,
        aiConsent: Boolean,
        cycleLength: Int,
        periodLength: Int,
        ovulationEnabled: Boolean,
        initialPeriodStartEpochDay: Long?,
    ) -> Unit,
) {
    val strings = AppStrings.get(language)
    var step by remember { mutableIntStateOf(0) }
    var selectedAge by remember { mutableStateOf(AgeGroup.ADULT_18_PLUS) }
    var parentalConsent by remember { mutableStateOf(false) }
    var preferredName by remember { mutableStateOf(initialName.ifBlank { "" }) }
    var aiConsent by remember { mutableStateOf(false) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var periodLength by remember { mutableIntStateOf(5) }
    var ovulationEnabled by remember { mutableStateOf(false) }
    var includeLastPeriod by remember { mutableStateOf(false) }
    var daysAgoLastPeriod by remember { mutableIntStateOf(7) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlushBackground)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${step + 1} / 5",
                    style = MaterialTheme.typography.labelLarge,
                    color = PinkPrimary,
                )
                LanguageTogglePill(currentLanguage = language, onLanguageChange = onLanguageChange)
            }

            Spacer(Modifier.height(20.dp))

            when (step) {
                0 -> {
                    // Language Selection Step
                    Text(strings.chooseLanguage, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    AppLanguage.entries.forEach { lang ->
                        val selected = lang == language
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onLanguageChange(lang) }
                                .testTag("onboarding_lang_${lang.code}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) PinkLight else PureWhite,
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (selected) PinkPrimary else PinkSoftBorder,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.EN) "English" else "हिन्दी (Hindi)",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                if (selected) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = PinkPrimary)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Age Selection Step
                    Text(strings.chooseAgeGroup, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(14.dp))

                    listOf(
                        Triple(AgeGroup.TEEN_13_17, strings.teenLabel, strings.teenDesc),
                        Triple(AgeGroup.ADULT_18_PLUS, strings.adultLabel, strings.adultDesc),
                    ).forEach { (group, title, desc) ->
                        val selected = selectedAge == group
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedAge = group
                                    if (group == AgeGroup.TEEN_13_17) {
                                        ovulationEnabled = false
                                    }
                                }
                                .testTag("onboarding_age_${group.wireValue}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) PinkLight else PureWhite,
                            ),
                            border = BorderStroke(1.5.dp, if (selected) PinkPrimary else PinkSoftBorder),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(title, style = MaterialTheme.typography.titleMedium, color = PinkPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(desc, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (selectedAge == AgeGroup.TEEN_13_17) {
                        Spacer(Modifier.height(12.dp))
                        SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.6f), PureWhite)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Checkbox(
                                    checked = parentalConsent,
                                    onCheckedChange = { parentalConsent = it },
                                    modifier = Modifier.testTag("parental_consent_checkbox"),
                                )
                                Column {
                                    Text(strings.parentalConsentLabel, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        strings.parentalConsentNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Onboarding Highlights Step
                    Text(strings.tagline, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    val highlights = if (language == AppLanguage.HI) {
                        listOf(
                            Triple(Icons.Rounded.CalendarMonth, "चक्र और लक्षण ट्रैकिंग", "पीरियड्स, दर्द, मूड और पानी को सरलता से दर्ज करें।"),
                            Triple(Icons.Rounded.Shield, "निजी PIN-लॉक्ड डायरी", "आपकी डायरी और स्वास्थ्य डेटा केवल आपके खाते में सुरक्षित है।"),
                            Triple(Icons.Rounded.AutoAwesome, "द्विभाषी देखभाल और शिक्षा", "हिंदी व अंग्रेजी में स्वच्छता गाइड, मिथक बनाम तथ्य और AI सहायक।"),
                        )
                    } else {
                        listOf(
                            Triple(Icons.Rounded.CalendarMonth, "Gentle Cycle & Symptom Care", "Track periods, cramps, moods, and hydration with clear estimate notices."),
                            Triple(Icons.Rounded.Shield, "Private PIN-Locked Diary", "Your diary and personal logs stay isolated to your account and invisible to admins."),
                            Triple(Icons.Rounded.AutoAwesome, "Hindi & English Education", "Explore First Period guides, nutrition ideas, and consent-based Care Assistant."),
                        )
                    }
                    highlights.forEach { (icon, title, body) ->
                        SoftGradientCard(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(2.dp))
                                    Text(body, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Profile Setup Step
                    Text(
                        if (language == AppLanguage.HI) "प्रोफ़ाइल सेटअप" else "Profile Setup",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = preferredName,
                        onValueChange = { preferredName = it.take(50) },
                        label = { Text(strings.preferredNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preferred_name_input"),
                    )
                    Spacer(Modifier.height(16.dp))
                    SoftGradientCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(strings.aiConsentTitle, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(strings.aiConsentDesc, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = aiConsent,
                                onCheckedChange = { aiConsent = it },
                                modifier = Modifier.testTag("onboarding_ai_consent_switch"),
                            )
                        }
                    }
                }

                4 -> {
                    // Period Information Setup Step
                    Text(
                        if (language == AppLanguage.HI) "चक्र जानकारी सेटअप" else "Cycle Information Setup",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(14.dp))

                    SoftGradientCard {
                        Text("${strings.cycleLengthLabel}: $cycleLength", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = cycleLength.toFloat(),
                            onValueChange = { cycleLength = it.roundToInt().coerceIn(21, 45) },
                            valueRange = 21f..45f,
                            colors = SliderDefaults.colors(thumbColor = PinkPrimary, activeTrackColor = PinkPrimary),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("${strings.periodLengthLabel}: $periodLength", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = periodLength.toFloat(),
                            onValueChange = { periodLength = it.roundToInt().coerceIn(2, 10) },
                            valueRange = 2f..10f,
                            colors = SliderDefaults.colors(thumbColor = PinkPrimary, activeTrackColor = PinkPrimary),
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    SoftGradientCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (language == AppLanguage.HI) "पिछली पीरियड तिथि जोड़ें (वैकल्पिक)" else "Add last period date (optional)",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(checked = includeLastPeriod, onCheckedChange = { includeLastPeriod = it })
                        }
                        if (includeLastPeriod) {
                            Spacer(Modifier.height(8.dp))
                            val chosenEpoch = LocalDate.now().toEpochDay() - daysAgoLastPeriod
                            Text(
                                "${if (language == AppLanguage.HI) "प्रारंभ तिथि" else "Started"}: ${LocalDate.ofEpochDay(chosenEpoch)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PinkPrimary,
                            )
                            Slider(
                                value = daysAgoLastPeriod.toFloat(),
                                onValueChange = { daysAgoLastPeriod = it.roundToInt().coerceIn(0, 45) },
                                valueRange = 0f..45f,
                            )
                        }
                    }

                    if (selectedAge == AgeGroup.ADULT_18_PLUS) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = PureWhite,
                            border = BorderStroke(1.dp, PinkSoftBorder),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    strings.optionalOvulationLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                )
                                Switch(
                                    checked = ovulationEnabled,
                                    onCheckedChange = { ovulationEnabled = it },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            val canContinue = when (step) {
                1 -> selectedAge == AgeGroup.ADULT_18_PLUS || parentalConsent
                3 -> preferredName.isNotBlank()
                else -> true
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f).height(50.dp),
                    ) {
                        Text(strings.back)
                    }
                }
                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            val startEpoch = if (includeLastPeriod) {
                                LocalDate.now().toEpochDay() - daysAgoLastPeriod
                            } else {
                                null
                            }
                            onComplete(
                                preferredName.trim(),
                                selectedAge,
                                language,
                                if (selectedAge == AgeGroup.TEEN_13_17) parentalConsent else true,
                                aiConsent,
                                cycleLength,
                                periodLength,
                                ovulationEnabled,
                                startEpoch,
                            )
                        }
                    },
                    enabled = canContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(if (step < 4) strings.next else strings.finishSetup, color = PureWhite)
                }
            }
        }
    }
}
