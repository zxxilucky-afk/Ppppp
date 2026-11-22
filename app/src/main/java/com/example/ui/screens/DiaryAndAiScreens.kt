package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.domain.DiaryEntry
import com.example.domain.FaqItem
import com.example.domain.UiState
import com.example.domain.UserProfile
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.security.SecurityManager
import com.example.ui.AiStreamState
import com.example.ui.ChatMessage
import com.example.ui.components.PinkoraEmptyState
import com.example.ui.components.PinkoraErrorState
import com.example.ui.components.PinkoraLoadingState
import com.example.ui.components.SoftGradientCard
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite

@Composable
fun PrivateDiaryScreen(
    diaryState: UiState<List<DiaryEntry>>,
    hasPin: Boolean,
    isUnlocked: Boolean,
    biometricEnabled: Boolean,
    securityManager: SecurityManager,
    language: AppLanguage,
    onSetPin: (String) -> Unit,
    onVerifyPin: (String, (Boolean) -> Unit) -> Unit,
    onBiometricSuccess: () -> Unit,
    onLockNow: () -> Unit,
    onSaveEntry: (entryId: String?, title: String, body: String, moodTag: String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                    Text(strings.diaryTitle, style = MaterialTheme.typography.headlineSmall)
                }
                Row {
                    if (!hasPin) {
                        TextButton(onClick = { showSetPinDialog = true }) {
                            Text(strings.setPin, color = PinkPrimary)
                        }
                    } else if (isUnlocked) {
                        IconButton(onClick = onLockNow) {
                            Icon(Icons.Rounded.Lock, contentDescription = "Lock diary", tint = PinkPrimary)
                        }
                    }
                    if (!hasPin || isUnlocked) {
                        Button(
                            onClick = {
                                editingEntry = null
                                showEditor = true
                            },
                            modifier = Modifier.testTag("new_diary_entry_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.newDiaryEntry, color = PureWhite)
                        }
                    }
                }
            }

            if (hasPin && !isUnlocked) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(strings.diaryLockPrompt, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinInput = it },
                        label = { Text(strings.enterPin) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("diary_pin_input"),
                    )
                    if (pinError != null) {
                        Spacer(Modifier.height(8.dp))
                        PinkoraErrorState(pinError!!)
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onVerifyPin(pinInput) { ok ->
                                if (ok) {
                                    pinInput = ""
                                    pinError = null
                                } else {
                                    pinError = if (language == AppLanguage.HI) "गलत PIN" else "Incorrect PIN"
                                }
                            }
                        },
                        enabled = pinInput.length == 4,
                        modifier = Modifier.fillMaxWidth().testTag("diary_unlock_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    ) {
                        Text(strings.enterPin, color = PureWhite)
                    }
                    if (biometricEnabled && securityManager.isBiometricAvailable()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                securityManager.authenticateWithBiometrics(
                                    activityContext = context,
                                    title = strings.diaryTitle,
                                    subtitle = strings.diaryLockPrompt,
                                    negativeText = strings.cancel,
                                    onSuccess = onBiometricSuccess,
                                    onError = { msg -> pinError = msg },
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.unlockWithBiometric)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text(strings.searchDiaryHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("diary_search_input"),
                )
                Spacer(Modifier.height(10.dp))

                when (diaryState) {
                    is UiState.Loading -> PinkoraLoadingState()
                    is UiState.Error -> Box(Modifier.padding(16.dp)) { PinkoraErrorState(diaryState.message) }
                    is UiState.Success -> {
                        val filtered = diaryState.data.filter {
                            searchQuery.isBlank() ||
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                it.body.contains(searchQuery, ignoreCase = true)
                        }
                        if (filtered.isEmpty()) {
                            Box(Modifier.padding(16.dp)) { PinkoraEmptyState(strings.noRecordsYet) }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(filtered, key = { it.id }) { entry ->
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
                                                    Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                                    Text(
                                                        formatEpochDay(entry.dateEpochDay),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PinkPrimary,
                                                    )
                                                }
                                                Row {
                                                    IconButton(onClick = {
                                                        editingEntry = entry
                                                        showEditor = true
                                                    }) {
                                                        Icon(Icons.Rounded.Edit, contentDescription = strings.edit, tint = LavenderDeep)
                                                    }
                                                    IconButton(onClick = { onDeleteEntry(entry.id) }) {
                                                        Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = PinkPrimary)
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(entry.body, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        var title by remember(editingEntry) { mutableStateOf(editingEntry?.title ?: "") }
        var body by remember(editingEntry) { mutableStateOf(editingEntry?.body ?: "") }
        var tag by remember(editingEntry) { mutableStateOf(editingEntry?.moodTag ?: "") }

        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingEntry == null) strings.newDiaryEntry else strings.edit) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(120) },
                        label = { Text(if (language == AppLanguage.HI) "शीर्षक" else "Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("diary_title_input"),
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it.take(5000) },
                        label = { Text(if (language == AppLanguage.HI) "आपका निजी नोट" else "Your private reflection") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("diary_body_input"),
                    )
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it.take(40) },
                        label = { Text(if (language == AppLanguage.HI) "टैग (वैकल्पिक)" else "Tag (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveEntry(editingEntry?.id, title, body, tag)
                        showEditor = false
                    },
                    enabled = title.isNotBlank() && body.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text(strings.cancel) }
            },
        )
    }

    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
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
                        showSetPinDialog = false
                    },
                    enabled = newPin.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                ) {
                    Text(strings.save, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) { Text(strings.cancel) }
            },
        )
    }
}

@Composable
fun CareAssistantScreen(
    profile: UserProfile?,
    chatMessages: List<ChatMessage>,
    streamState: AiStreamState,
    faqs: List<FaqItem>,
    language: AppLanguage,
    onGrantAiConsent: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var input by remember { mutableStateOf("") }
    val hasConsent = profile?.aiHealthDataConsent == true

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = LavenderDeep)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.aiAssistantTitle, style = MaterialTheme.typography.titleLarge)
                }
                if (profile?.isTeen() == true) {
                    Surface(color = LavenderSurface, shape = MaterialTheme.shapes.small) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = LavenderDeep, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("13–17 Safe", style = MaterialTheme.typography.labelSmall, color = LavenderDeep)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!hasConsent) {
                    item {
                        SoftGradientCard(colors = listOf(PinkLight, PureWhite)) {
                            Text(strings.aiConsentRequiredBanner, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = onGrantAiConsent,
                                modifier = Modifier.testTag("grant_ai_consent_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                            ) {
                                Text(strings.enableAiConsent, color = PureWhite)
                            }
                        }
                    }
                } else {
                    items(chatMessages, key = { it.id }) { msg ->
                        val bg = if (msg.isUser) PinkLight else LavenderSurface.copy(alpha = 0.65f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
                        ) {
                            Surface(
                                color = bg,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.widthIn(max = 420.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }

                    when (streamState) {
                        is AiStreamState.Loading -> item {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = LavenderDeep, strokeWidth = 2.dp)
                                Text(if (language == AppLanguage.HI) "सोच रहा हूँ..." else "Thinking...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is AiStreamState.Streaming -> item {
                            Surface(color = LavenderSurface.copy(alpha = 0.65f), shape = MaterialTheme.shapes.medium) {
                                Text(streamState.partial, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                            }
                        }
                        is AiStreamState.Error -> item { PinkoraErrorState(streamState.message) }
                        AiStreamState.Idle -> Unit
                    }
                }

                item {
                    Spacer(Modifier.height(6.dp))
                    Text(strings.faqFallbackTitle, style = MaterialTheme.typography.titleMedium, color = LavenderDeep)
                }

                items(faqs, key = { it.id }) { faq ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, PinkSoftBorder),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (language == AppLanguage.HI) faq.questionHi else faq.questionEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.HI) faq.answerHi else faq.answerEn,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            if (hasConsent) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text(strings.askCareHint) },
                        modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                        maxLines = 3,
                    )
                    Button(
                        onClick = {
                            val q = input
                            input = ""
                            onSendMessage(q)
                        },
                        enabled = input.isNotBlank() && streamState !is AiStreamState.Loading && streamState !is AiStreamState.Streaming,
                        modifier = Modifier.testTag("ai_send_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = PureWhite)
                    }
                }
            }
        }
    }
}
