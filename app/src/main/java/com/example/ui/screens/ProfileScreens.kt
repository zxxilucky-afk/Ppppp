package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.domain.AgeGroup
import com.example.domain.ReminderSetting
import com.example.domain.UiState
import com.example.domain.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.notifications.PinkoraNotificationManager
import com.example.security.SecurityManager
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.SoftGradientCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite
import kotlin.math.roundToInt

enum class ProfileSubPage {
    MENU,
    SETTINGS,
    NOTIFICATIONS,
    PRIVACY,
    ABOUT,
    SUPPORT,
    ADMIN,
}

@Composable
fun ProfileHubScreen(
    profile: UserProfile?,
    userEmail: String?,
    isAdmin: Boolean,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateSubPage: (ProfileSubPage) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SoftGradientCard(colors = listOf(PinkLight.copy(alpha = 0.7f), LavenderSurface.copy(alpha = 0.55f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.preferredName?.ifBlank { "Friend" } ?: "Friend",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = userEmail ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(color = PureWhite, shape = MaterialTheme.shapes.small) {
                                    Text(
                                        text = if (profile?.isTeen() == true) strings.teenLabel else strings.adultLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LavenderDeep,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }
                        }
                        LanguageTogglePill(currentLanguage = language, onLanguageChange = onLanguageChange)
                    }
                }
            }

            item {
                ProfileMenuRow(Icons.Rounded.Settings, strings.settingsTitle, "menu_settings") {
                    onNavigateSubPage(ProfileSubPage.SETTINGS)
                }
            }
            item {
                ProfileMenuRow(Icons.Rounded.NotificationsActive, strings.notificationsTitle, "menu_notifications") {
                    onNavigateSubPage(ProfileSubPage.NOTIFICATIONS)
                }
            }
            item {
                ProfileMenuRow(Icons.Rounded.Security, strings.privacySecurityTitle, "menu_privacy") {
                    onNavigateSubPage(ProfileSubPage.PRIVACY)
                }
            }
            item {
                ProfileMenuRow(Icons.AutoMirrored.Rounded.HelpOutline, strings.helpSupportTitle, "menu_support") {
                    onNavigateSubPage(ProfileSubPage.SUPPORT)
                }
            }
            item {
                ProfileMenuRow(Icons.Rounded.Info, strings.aboutTitle, "menu_about") {
                    onNavigateSubPage(ProfileSubPage.ABOUT)
                }
            }

            if (isAdmin) {
                item {
                    ProfileMenuRow(Icons.Rounded.AdminPanelSettings, strings.adminPanelTitle, "menu_admin", tint = LavenderDeep) {
                        onNavigateSubPage(ProfileSubPage.ADMIN)
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("logout_button"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.logoutBtn)
                }
            }

            item {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("delete_account_button"),
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = AlertRed)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.deleteAccountBtn, color = AlertRed)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteAccountBtn, color = AlertRed) },
            text = { Text(strings.deleteAccountConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                ) {
                    Text(strings.delete, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(strings.cancel) }
            },
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    tag: String,
    tint: Color = PinkPrimary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(tag),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, PinkSoftBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun SettingsSubScreen(
    profile: UserProfile?,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onSaveSettings: (String, AgeGroup, Boolean, Int, Int, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var name by remember(profile) { mutableStateOf(profile?.preferredName ?: "") }
    var ageGroup by remember(profile) { mutableStateOf(profile?.resolvedAgeGroup() ?: AgeGroup.ADULT_18_PLUS) }
    var aiConsent by remember(profile) { mutableStateOf(profile?.aiHealthDataConsent ?: false) }
    var cycleLen by remember(profile) { mutableIntStateOf((profile?.averageCycleLength ?: 28L).toInt()) }
    var periodLen by remember(profile) { mutableIntStateOf((profile?.averagePeriodLength ?: 5L).toInt()) }
    var ovulation by remember(profile) { mutableStateOf(profile?.ovulationTrackingEnabled ?: false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SubScreenTopBar(strings.settingsTitle, onBack)
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(50) },
                    label = { Text(strings.preferredNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SoftGradientCard {
                    Text(strings.chooseLanguage, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LanguageTogglePill(currentLanguage = language, onLanguageChange = onLanguageChange)
                }
            }
            item {
                SoftGradientCard {
                    Text("${strings.cycleLengthLabel}: $cycleLen", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = cycleLen.toFloat(),
                        onValueChange = { cycleLen = it.roundToInt().coerceIn(21, 45) },
                        valueRange = 21f..45f,
                    )
                    Text("${strings.periodLengthLabel}: $periodLen", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = periodLen.toFloat(),
                        onValueChange = { periodLen = it.roundToInt().coerceIn(2, 10) },
                        valueRange = 2f..10f,
                    )
                }
            }
            if (ageGroup == AgeGroup.ADULT_18_PLUS) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = PureWhite), border = BorderStroke(1.dp, PinkSoftBorder)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(strings.optionalOvulationLabel, modifier = Modifier.weight(1f).padding(end = 8.dp))
                            Switch(checked = ovulation, onCheckedChange = { ovulation = it })
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { onSaveSettings(name, ageGroup, aiConsent, cycleLen, periodLen, ovulation) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            }
        }
    }
}

@Composable
fun NotificationsCenterSubScreen(
    remindersState: UiState<List<ReminderSetting>>,
    language: AppLanguage,
    onSaveReminder: (type: String, enabled: Boolean, hour: Int, minute: Int, intervalHours: Int, discreet: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PinkoraNotificationManager.hasNotificationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    val existingMap = ((remindersState as? UiState.Success)?.data ?: emptyList()).associateBy { it.type }
    val reminderTypes = listOf("upcoming_period", "log_reminder", "water", "mood_checkin", "wellness_tip", "self_care")

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SubScreenTopBar(strings.notificationsTitle, onBack) }

            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    SoftGradientCard {
                        Text(
                            if (language == AppLanguage.HI) "रिमाइंडर पाने के लिए सूचना अनुमति दें।" else "Allow notifications to receive scheduled wellness reminders.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        ) {
                            Text(if (language == AppLanguage.HI) "अनुमति दें" else "Grant Notification Permission", color = PureWhite)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            PinkoraNotificationManager.showReminderNotification(context, "mood_checkin", true, language.code)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("test_notification_btn"),
                ) {
                    Text(strings.testNotificationBtn)
                }
            }

            items(reminderTypes, key = { it }) { type ->
                val current = existingMap[type] ?: ReminderSetting(
                    type = type,
                    enabled = false,
                    hour = if (type == "water") 9L else 20L,
                    minute = 0L,
                    intervalHours = if (type == "water") 2L else 24L,
                    discreetText = true,
                )
                var enabled by remember(current) { mutableStateOf(current.enabled) }
                var discreet by remember(current) { mutableStateOf(current.discreetText) }
                var hour by remember(current) { mutableIntStateOf(current.hour.toInt()) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, PinkSoftBorder),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.reminderNames[type] ?: type, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${String.format("%02d:00", hour)} • ${strings.discreetExample}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { next ->
                                    enabled = next
                                    if (next && !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    onSaveReminder(type, next, hour, 0, current.intervalHours.toInt(), discreet)
                                },
                            )
                        }
                        if (enabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(strings.discreetTextLabel, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = discreet,
                                    onCheckedChange = { d ->
                                        discreet = d
                                        onSaveReminder(type, enabled, hour, 0, current.intervalHours.toInt(), d)
                                    },
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = {
                                    hour = (hour - 1).coerceAtLeast(6)
                                    onSaveReminder(type, enabled, hour, 0, current.intervalHours.toInt(), discreet)
                                }) { Text("-1h") }
                                TextButton(onClick = {
                                    hour = (hour + 1).coerceAtMost(22)
                                    onSaveReminder(type, enabled, hour, 0, current.intervalHours.toInt(), discreet)
                                }) { Text("+1h") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySecuritySubScreen(
    profile: UserProfile?,
    hasPin: Boolean,
    biometricEnabled: Boolean,
    securityManager: SecurityManager,
    language: AppLanguage,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleAiConsent: (Boolean) -> Unit,
    onExportJson: ((String) -> Unit) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SubScreenTopBar(strings.privacySecurityTitle, onBack) }

            item {
                SoftGradientCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = PinkPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.diaryTitle, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showPinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        ) {
                            Text(strings.setPin, color = PureWhite)
                        }
                        if (hasPin) {
                            OutlinedButton(onClick = onRemovePin) {
                                Text(if (language == AppLanguage.HI) "PIN हटाएं" else "Remove PIN")
                            }
                        }
                    }
                    if (hasPin && securityManager.isBiometricAvailable()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(strings.unlockWithBiometric)
                            Switch(checked = biometricEnabled, onCheckedChange = onToggleBiometric)
                        }
                    }
                }
            }

            item {
                SoftGradientCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(strings.aiConsentTitle, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(strings.aiConsentDesc, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = profile?.aiHealthDataConsent == true,
                            onCheckedChange = onToggleAiConsent,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onExportJson { json ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "PINKORA Health Data Export")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, strings.exportJsonBtn))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("export_json_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, tint = PureWhite)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.exportJsonBtn, color = PureWhite)
                }
            }

            item {
                SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.5f), PureWhite)) {
                    Text(
                        if (language == AppLanguage.HI) {
                            "गोपनीयता प्रतिबद्धता: PINKORA आपके स्वास्थ्य डेटा को कभी नहीं बेचता और न ही स्वास्थ्य-आधारित विज्ञापन दिखाता है। आपकी डायरी और पीरियड लॉग एडमिन के लिए भी बंद हैं।"
                        } else {
                            "Privacy Commitment: PINKORA never sells health data and shows zero health-based ads. Your diaries, period logs, symptoms, and mood records remain strictly isolated to your account and inaccessible to admins."
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(strings.setPin) },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) newPin = it },
                    label = { Text(strings.enterPin) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetPin(newPin)
                        showPinDialog = false
                    },
                    enabled = newPin.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text(strings.cancel) } },
        )
    }
}

@Composable
fun SubScreenTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}
