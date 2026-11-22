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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.AdminLog
import com.example.domain.EducationalArticle
import com.example.domain.FaqItem
import com.example.domain.SupportRequest
import com.example.domain.UiState
import com.example.domain.WellnessTip
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.PinkoraEmptyState
import com.example.ui.components.PinkoraErrorState
import com.example.ui.components.PinkoraLoadingState
import com.example.ui.components.SoftGradientCard
import com.example.ui.theme.LavenderDeep
import com.example.ui.theme.LavenderSurface
import com.example.ui.theme.PinkLight
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSoftBorder
import com.example.ui.theme.PureWhite

@Composable
fun AboutPinkoraSubScreen(language: AppLanguage, onBack: () -> Unit) {
    val strings = AppStrings.get(language)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SubScreenTopBar(strings.aboutTitle, onBack) }
            item {
                SoftGradientCard {
                    Text("PINKORA v1.0", style = MaterialTheme.typography.titleLarge, color = PinkPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(strings.tagline, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (language == AppLanguage.HI) {
                            "PINKORA किशोरियों (13–17) और महिलाओं (18+) के लिए बनाया गया एक सुरक्षित, गोपनीय स्वास्थ्य व स्वच्छता साथी है। सभी चक्र अनुमान केवल शैक्षिक मार्गदर्शन हैं—कोई चिकित्सकीय निदान या दवा सलाह नहीं। सभी लेख डॉक्टर समीक्षा तक ड्राफ्ट के रूप में चिह्नित रहते हैं।"
                        } else {
                            "PINKORA is a calm, bilingual (Hindi & English) wellness companion built for teens (13–17) and adults (18+). All cycle calculations are estimates—never medical diagnoses or prescriptions. Educational articles remain labeled 'Draft – pending doctor review' until approved by a qualified doctor."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun HelpSupportSubScreen(
    supportState: UiState<List<SupportRequest>>,
    faqs: List<FaqItem>,
    language: AppLanguage,
    onSubmitTicket: (subject: String, message: String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SubScreenTopBar(strings.helpSupportTitle, onBack) }

            item {
                SoftGradientCard {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it.take(120) },
                        label = { Text(if (language == AppLanguage.HI) "विषय" else "Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("support_subject_input"),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it.take(2000) },
                        label = { Text(if (language == AppLanguage.HI) "संदेश" else "Message") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("support_message_input"),
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSubmitTicket(subject, message)
                            subject = ""
                            message = ""
                        },
                        enabled = subject.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("submit_support_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    ) {
                        Text(strings.submitTicketBtn, color = PureWhite)
                    }
                }
            }

            when (supportState) {
                is UiState.Loading -> item { PinkoraLoadingState() }
                is UiState.Error -> item { PinkoraErrorState(supportState.message) }
                is UiState.Success -> {
                    if (supportState.data.isEmpty()) {
                        item { PinkoraEmptyState(strings.noRecordsYet) }
                    } else {
                        items(supportState.data, key = { it.id }) { req ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, PinkSoftBorder),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(req.subject, style = MaterialTheme.typography.titleMedium)
                                        Surface(color = PinkLight, shape = MaterialTheme.shapes.small) {
                                            Text(
                                                req.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PinkPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(req.message, style = MaterialTheme.typography.bodySmall)
                                    if (req.adminReply.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Surface(color = LavenderSurface, shape = MaterialTheme.shapes.small) {
                                            Text(
                                                "Reply: ${req.adminReply}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LavenderDeep,
                                                modifier = Modifier.padding(8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(strings.faqFallbackTitle, style = MaterialTheme.typography.titleMedium)
            }
            items(faqs, key = { it.id }) { faq ->
                Card(colors = CardDefaults.cardColors(containerColor = PureWhite), border = BorderStroke(1.dp, PinkSoftBorder)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (language == AppLanguage.HI) faq.questionHi else faq.questionEn, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(if (language == AppLanguage.HI) faq.answerHi else faq.answerEn, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelSubScreen(
    articles: List<EducationalArticle>,
    tips: List<WellnessTip>,
    faqs: List<FaqItem>,
    supportState: UiState<List<SupportRequest>>,
    adminLogs: List<AdminLog>,
    language: AppLanguage,
    onCreateArticle: (String, String, String, String, String, String, String, String) -> Unit,
    onCreateTip: (String, String, String) -> Unit,
    onCreateFaq: (String, String, String, String, String) -> Unit,
    onReplyTicket: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = AppStrings.get(language)
    var tab by remember { mutableIntStateOf(0) }
    val tickets = (supportState as? UiState.Success)?.data ?: emptyList()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SubScreenTopBar(strings.adminPanelTitle, onBack)
            }

            SecondaryTabRow(selectedTabIndex = tab, containerColor = PureWhite, contentColor = LavenderDeep) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Overview") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Content") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Tickets") })
                Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Logs") })
            }

            when (tab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            SoftGradientCard(colors = listOf(LavenderSurface.copy(alpha = 0.6f), PureWhite)) {
                                Text(strings.adminPrivacyNote, style = MaterialTheme.typography.bodyMedium, color = LavenderDeep)
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SoftGradientCard(modifier = Modifier.weight(1f)) {
                                    Text("Articles", style = MaterialTheme.typography.labelLarge)
                                    Text("${articles.size}", style = MaterialTheme.typography.headlineMedium, color = PinkPrimary)
                                }
                                SoftGradientCard(modifier = Modifier.weight(1f)) {
                                    Text("Open Tickets", style = MaterialTheme.typography.labelLarge)
                                    Text("${tickets.count { it.status != "resolved" }}", style = MaterialTheme.typography.headlineMedium, color = LavenderDeep)
                                }
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SoftGradientCard(modifier = Modifier.weight(1f)) {
                                    Text("Tips", style = MaterialTheme.typography.labelLarge)
                                    Text("${tips.size}", style = MaterialTheme.typography.headlineMedium, color = PinkPrimary)
                                }
                                SoftGradientCard(modifier = Modifier.weight(1f)) {
                                    Text("FAQs", style = MaterialTheme.typography.labelLarge)
                                    Text("${faqs.size}", style = MaterialTheme.typography.headlineMedium, color = LavenderDeep)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    var titleEn by remember { mutableStateOf("") }
                    var titleHi by remember { mutableStateOf("") }
                    var bodyEn by remember { mutableStateOf("") }
                    var bodyHi by remember { mutableStateOf("") }
                    var tipEn by remember { mutableStateOf("") }
                    var tipHi by remember { mutableStateOf("") }
                    var faqQEn by remember { mutableStateOf("") }
                    var faqQHi by remember { mutableStateOf("") }
                    var faqAEn by remember { mutableStateOf("") }
                    var faqAHi by remember { mutableStateOf("") }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            SoftGradientCard {
                                Text("Publish Bilingual Article (Draft – pending doctor review)", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Title (EN)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(titleHi, { titleHi = it }, label = { Text("Title (HI)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(bodyEn, { bodyEn = it }, label = { Text("Body (EN)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(bodyHi, { bodyHi = it }, label = { Text("Body (HI)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onCreateArticle(
                                            titleEn, titleHi, titleEn.take(100), titleHi.take(100),
                                            bodyEn, bodyHi, "hygiene", "all",
                                        )
                                        titleEn = ""; titleHi = ""; bodyEn = ""; bodyHi = ""
                                    },
                                    enabled = titleEn.isNotBlank() && titleHi.isNotBlank() && bodyEn.isNotBlank() && bodyHi.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                                ) {
                                    Text("Publish Draft Article", color = PureWhite)
                                }
                            }
                        }

                        item {
                            SoftGradientCard {
                                Text("Add Wellness Tip", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(tipEn, { tipEn = it }, label = { Text("Tip (EN)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(tipHi, { tipHi = it }, label = { Text("Tip (HI)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onCreateTip(tipEn, tipHi, "comfort")
                                        tipEn = ""; tipHi = ""
                                    },
                                    enabled = tipEn.isNotBlank() && tipHi.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                                ) {
                                    Text("Add Tip", color = PureWhite)
                                }
                            }
                        }

                        item {
                            SoftGradientCard {
                                Text("Add Built-in FAQ", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(faqQEn, { faqQEn = it }, label = { Text("Question (EN)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(faqQHi, { faqQHi = it }, label = { Text("Question (HI)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(faqAEn, { faqAEn = it }, label = { Text("Answer (EN)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(faqAHi, { faqAHi = it }, label = { Text("Answer (HI)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onCreateFaq(faqQEn, faqQHi, faqAEn, faqAHi, "general")
                                        faqQEn = ""; faqQHi = ""; faqAEn = ""; faqAHi = ""
                                    },
                                    enabled = faqQEn.isNotBlank() && faqQHi.isNotBlank() && faqAEn.isNotBlank() && faqAHi.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                                ) {
                                    Text("Add FAQ", color = PureWhite)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (tickets.isEmpty()) {
                            item { PinkoraEmptyState("No support tickets yet.") }
                        } else {
                            items(tickets, key = { it.id }) { req ->
                                var reply by remember(req.id) { mutableStateOf(req.adminReply) }
                                var status by remember(req.id) { mutableStateOf(req.status) }
                                Card(colors = CardDefaults.cardColors(containerColor = PureWhite), border = BorderStroke(1.dp, PinkSoftBorder)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(req.subject, style = MaterialTheme.typography.titleMedium)
                                        Text(req.message, style = MaterialTheme.typography.bodySmall)
                                        Spacer(Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("open", "in_progress", "resolved").forEach { st ->
                                                FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st) })
                                            }
                                        }
                                        OutlinedTextField(reply, { reply = it }, label = { Text("Admin Reply") }, modifier = Modifier.fillMaxWidth())
                                        Spacer(Modifier.height(6.dp))
                                        Button(
                                            onClick = { onReplyTicket(req.id, status, reply) },
                                            colors = ButtonDefaults.buttonColors(containerColor = LavenderDeep),
                                        ) {
                                            Text("Update Ticket", color = PureWhite)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (adminLogs.isEmpty()) {
                            item { PinkoraEmptyState("No admin activity logs yet.") }
                        } else {
                            items(adminLogs, key = { it.id }) { log ->
                                Card(colors = CardDefaults.cardColors(containerColor = PureWhite), border = BorderStroke(1.dp, PinkSoftBorder)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("${log.action} → ${log.target}", style = MaterialTheme.typography.titleSmall)
                                        Text("By: ${log.adminId}", style = MaterialTheme.typography.bodySmall)
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
