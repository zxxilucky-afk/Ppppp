package com.example.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiCareRepository
import com.example.data.PinkoraRepository
import com.example.domain.AdminLog
import com.example.domain.AgeGroup
import com.example.domain.CycleCalculator
import com.example.domain.CycleSummary
import com.example.domain.DiaryEntry
import com.example.domain.EducationalArticle
import com.example.domain.FaqItem
import com.example.domain.FlowIntensity
import com.example.domain.MoodRecord
import com.example.domain.MoodType
import com.example.domain.PeriodRecord
import com.example.domain.ReminderSetting
import com.example.domain.SeedContent
import com.example.domain.SupportRequest
import com.example.domain.SymptomRecord
import com.example.domain.UiState
import com.example.domain.UserProfile
import com.example.domain.WaterLog
import com.example.domain.WellnessTip
import com.example.i18n.AppLanguage
import com.example.notifications.PinkoraNotificationManager
import com.example.security.SecurityManager
import com.google.firebase.firestore.FirebaseFirestoreException
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isError: Boolean = false,
)

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data object Loading : AiStreamState
    data class Streaming(val partial: String) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PinkoraViewModel(
    private val repository: PinkoraRepository,
    private val aiRepository: AiCareRepository,
    private val securityManager: SecurityManager,
    private val appContext: Context,
    val currentUserId: String,
    val userEmail: String?,
    val displayName: String?,
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _diaryUnlocked = MutableStateFlow(false)
    val diaryUnlocked: StateFlow<Boolean> = _diaryUnlocked.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _aiStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val aiStreamState: StateFlow<AiStreamState> = _aiStreamState.asStateFlow()

    val hasPin: StateFlow<Boolean> = securityManager.hasPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val biometricEnabled: StateFlow<Boolean> = securityManager.biometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val profileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId).toUiState("Failed to load profile")

    val periodsState: StateFlow<UiState<List<PeriodRecord>>> =
        repository.observePeriodRecords(currentUserId).toUiState("Failed to load period records")

    val symptomsState: StateFlow<UiState<List<SymptomRecord>>> =
        repository.observeSymptomRecords(currentUserId).toUiState("Failed to load symptom records")

    val moodsState: StateFlow<UiState<List<MoodRecord>>> =
        repository.observeMoodRecords(currentUserId).toUiState("Failed to load mood records")

    val diaryState: StateFlow<UiState<List<DiaryEntry>>> =
        repository.observeDiaryEntries(currentUserId).toUiState("Failed to load diary")

    val waterState: StateFlow<UiState<List<WaterLog>>> =
        repository.observeWaterLogs(currentUserId).toUiState("Failed to load water logs")

    val remindersState: StateFlow<UiState<List<ReminderSetting>>> =
        repository.observeReminders(currentUserId).toUiState("Failed to load reminders")

    val articlesState: StateFlow<List<EducationalArticle>> =
        repository.observeArticles()
            .map { remote -> (SeedContent.defaultArticles + remote).distinctBy { it.id } }
            .catch { emit(SeedContent.defaultArticles) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedContent.defaultArticles)

    val tipsState: StateFlow<List<WellnessTip>> =
        repository.observeTips()
            .map { remote -> (SeedContent.defaultTips + remote).distinctBy { it.id } }
            .catch { emit(SeedContent.defaultTips) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedContent.defaultTips)

    val faqsState: StateFlow<List<FaqItem>> =
        repository.observeFaqs()
            .map { remote -> (SeedContent.defaultFaqs + remote).distinctBy { it.id } }
            .catch { emit(SeedContent.defaultFaqs) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedContent.defaultFaqs)

    val supportRequestsState: StateFlow<UiState<List<SupportRequest>>> =
        repository.observeUserSupportRequests(currentUserId).toUiState("Failed to load support tickets")

    val adminSupportRequestsState: StateFlow<UiState<List<SupportRequest>>> =
        isAdmin.flatMapLatest { admin ->
            if (admin) {
                repository.observeAllSupportRequestsForAdmin().map<List<SupportRequest>, UiState<List<SupportRequest>>> { UiState.Success(it) }
            } else {
                flowOf(UiState.Success(emptyList()))
            }
        }.catch { error ->
            val fs = unwrapFirestoreError(error)
            if (fs == null && error is CancellationException) throw error
            emit(UiState.Success(emptyList()))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(emptyList()))

    val adminLogsState: StateFlow<List<AdminLog>> =
        isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAdminLogs() else flowOf(emptyList())
        }.catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cycleSummary: StateFlow<CycleSummary> = combine(profileState, periodsState) { profUi, perUi ->
        val profile = (profUi as? UiState.Success)?.data
        val periods = (perUi as? UiState.Success)?.data ?: emptyList()
        val today = LocalDate.now().toEpochDay()
        CycleCalculator.calculate(
            records = periods,
            todayEpochDay = today,
            fallbackCycleLength = (profile?.averageCycleLength ?: 28L).toInt(),
            fallbackPeriodLength = (profile?.averagePeriodLength ?: 5L).toInt(),
            ageGroup = profile?.resolvedAgeGroup() ?: AgeGroup.ADULT_18_PLUS,
            ovulationEnabled = profile?.ovulationTrackingEnabled ?: false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        CycleCalculator.calculate(emptyList(), LocalDate.now().toEpochDay()),
    )

    init {
        viewModelScope.launch {
            securityManager.languageFlow.collect { savedLang ->
                _language.value = savedLang
            }
        }
        viewModelScope.launch {
            profileState.collect { state ->
                val prof = (state as? UiState.Success)?.data
                if (prof != null) {
                    _language.value = AppLanguage.fromCode(prof.language)
                }
            }
        }
        viewModelScope.launch {
            _isAdmin.value = repository.checkIsAdmin(currentUserId)
        }
        viewModelScope.launch {
            hasPin.collect { pinSet ->
                if (!pinSet) {
                    _diaryUnlocked.value = true
                }
            }
        }
    }

    private fun <T> Flow<T>.toUiState(fallbackMsg: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, fallbackMsg, rootError)
                emit(UiState.Error(rootError.localizedMessage ?: fallbackMsg))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun switchLanguage(lang: AppLanguage) {
        _language.value = lang
        viewModelScope.launch {
            securityManager.setLanguage(lang)
            val currentProf = (profileState.value as? UiState.Success)?.data
            if (currentProf != null) {
                repository.saveOrUpdateProfile(
                    userId = currentUserId,
                    preferredName = currentProf.preferredName,
                    email = userEmail,
                    ageGroup = currentProf.resolvedAgeGroup(),
                    language = lang.code,
                    parentalConsentConfirmed = currentProf.parentalConsentConfirmed,
                    aiHealthDataConsent = currentProf.aiHealthDataConsent,
                    onboardingCompleted = currentProf.onboardingCompleted,
                    averageCycleLength = currentProf.averageCycleLength.toInt(),
                    averagePeriodLength = currentProf.averagePeriodLength.toInt(),
                    ovulationTrackingEnabled = currentProf.ovulationTrackingEnabled,
                )
            }
        }
    }

    fun completeOnboarding(
        preferredName: String,
        ageGroup: AgeGroup,
        language: AppLanguage,
        parentalConsentConfirmed: Boolean,
        aiConsent: Boolean,
        cycleLength: Int,
        periodLength: Int,
        ovulationEnabled: Boolean,
        initialPeriodStartEpochDay: Long?,
    ) {
        viewModelScope.launch {
            securityManager.setLanguage(language)
            val profRes = repository.saveOrUpdateProfile(
                userId = currentUserId,
                preferredName = preferredName,
                email = userEmail,
                ageGroup = ageGroup,
                language = language.code,
                parentalConsentConfirmed = parentalConsentConfirmed,
                aiHealthDataConsent = aiConsent,
                onboardingCompleted = true,
                averageCycleLength = cycleLength,
                averagePeriodLength = periodLength,
                ovulationTrackingEnabled = ovulationEnabled,
            )
            if (profRes.isFailure) {
                _toastMessage.value = profRes.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
                return@launch
            }
            if (initialPeriodStartEpochDay != null && initialPeriodStartEpochDay > 0L) {
                repository.savePeriodRecord(
                    userId = currentUserId,
                    recordId = null,
                    startDateEpochDay = initialPeriodStartEpochDay,
                    endDateEpochDay = initialPeriodStartEpochDay + periodLength - 1,
                    flowIntensity = FlowIntensity.MEDIUM,
                    notes = "",
                )
            }
        }
    }

    fun updateProfileSettings(
        preferredName: String,
        ageGroup: AgeGroup,
        aiConsent: Boolean,
        cycleLength: Int,
        periodLength: Int,
        ovulationEnabled: Boolean,
    ) {
        viewModelScope.launch {
            val current = (profileState.value as? UiState.Success)?.data
            val res = repository.saveOrUpdateProfile(
                userId = currentUserId,
                preferredName = preferredName,
                email = userEmail,
                ageGroup = ageGroup,
                language = _language.value.code,
                parentalConsentConfirmed = current?.parentalConsentConfirmed ?: (ageGroup == AgeGroup.ADULT_18_PLUS),
                aiHealthDataConsent = aiConsent,
                onboardingCompleted = true,
                averageCycleLength = cycleLength,
                averagePeriodLength = periodLength,
                ovulationTrackingEnabled = ovulationEnabled,
            )
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "सेटिंग्स सहेजी गईं" else "Settings saved"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to update settings"
            }
        }
    }

    fun savePeriod(
        recordId: String?,
        startEpochDay: Long,
        endEpochDay: Long,
        flowIntensity: FlowIntensity,
        notes: String,
    ) {
        viewModelScope.launch {
            val res = repository.savePeriodRecord(
                userId = currentUserId,
                recordId = recordId,
                startDateEpochDay = startEpochDay,
                endDateEpochDay = endEpochDay,
                flowIntensity = flowIntensity,
                notes = notes,
            )
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "पीरियड रिकॉर्ड सहेजा गया" else "Period record saved"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save period"
            }
        }
    }

    fun deletePeriod(recordId: String) {
        viewModelScope.launch {
            val res = repository.deletePeriodRecord(currentUserId, recordId)
            if (res.isFailure) {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Delete failed"
            }
        }
    }

    fun logSymptoms(dateEpochDay: Long, symptoms: List<String>, painIntensity: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.addSymptomRecord(currentUserId, dateEpochDay, symptoms, painIntensity, notes)
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "लक्षण सहेजे गए" else "Symptoms logged"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to log symptoms"
            }
        }
    }

    fun deleteSymptom(recordId: String) {
        viewModelScope.launch {
            repository.deleteSymptomRecord(currentUserId, recordId)
        }
    }

    fun logMood(dateEpochDay: Long, mood: MoodType, energyLevel: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.addMoodRecord(currentUserId, dateEpochDay, mood, energyLevel, notes)
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "मूड सहेजा गया" else "Mood logged"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to log mood"
            }
        }
    }

    fun deleteMood(recordId: String) {
        viewModelScope.launch {
            repository.deleteMoodRecord(currentUserId, recordId)
        }
    }

    fun updateWater(delta: Int, target: Int = 8) {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val logs = (waterState.value as? UiState.Success)?.data ?: emptyList()
            val existing = logs.firstOrNull { it.dateEpochDay == today }
            val nextCount = ((existing?.glassesCount ?: 0L).toInt() + delta).coerceIn(0, 30)
            val finalTarget = (existing?.targetGlasses?.toInt() ?: target).coerceIn(1, 20)
            repository.upsertWaterLog(currentUserId, today, nextCount, finalTarget)
        }
    }

    fun updateWaterTarget(target: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val logs = (waterState.value as? UiState.Success)?.data ?: emptyList()
            val existing = logs.firstOrNull { it.dateEpochDay == today }
            val currentCount = (existing?.glassesCount ?: 0L).toInt()
            repository.upsertWaterLog(currentUserId, today, currentCount, target.coerceIn(1, 20))
        }
    }

    // --- Diary & Lock ---

    fun setPin(pin: String) {
        viewModelScope.launch {
            securityManager.setPin(pin)
            _diaryUnlocked.value = true
            _toastMessage.value = if (_language.value == AppLanguage.HI) "डायरी PIN सेट हो गया" else "Diary PIN set"
        }
    }

    fun unlockDiaryWithPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = securityManager.verifyPin(pin)
            if (ok) _diaryUnlocked.value = true
            onResult(ok)
        }
    }

    fun unlockDiaryWithBiometric() {
        _diaryUnlocked.value = true
    }

    fun lockDiaryNow() {
        if (hasPin.value) {
            _diaryUnlocked.value = false
        }
    }

    fun removePin() {
        viewModelScope.launch {
            securityManager.clearPin()
            _diaryUnlocked.value = true
            _toastMessage.value = if (_language.value == AppLanguage.HI) "PIN हटाया गया" else "PIN lock removed"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityManager.setBiometricEnabled(enabled)
        }
    }

    fun saveDiary(entryId: String?, title: String, body: String, moodTag: String) {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val res = repository.saveDiaryEntry(currentUserId, entryId, title, body, today, moodTag)
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "डायरी सहेजी गई" else "Diary entry saved"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save entry"
            }
        }
    }

    fun deleteDiary(entryId: String) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(currentUserId, entryId)
        }
    }

    // --- Reminders ---

    fun saveReminderSetting(
        type: String,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        intervalHours: Int,
        discreetText: Boolean,
    ) {
        viewModelScope.launch {
            val res = repository.saveReminder(currentUserId, type, enabled, hour, minute, intervalHours, discreetText)
            if (res.isSuccess) {
                PinkoraNotificationManager.syncReminderSchedule(
                    appContext,
                    ReminderSetting(
                        userId = currentUserId,
                        type = type,
                        enabled = enabled,
                        hour = hour.toLong(),
                        minute = minute.toLong(),
                        intervalHours = intervalHours.toLong(),
                        discreetText = discreetText,
                    ),
                    _language.value.code,
                )
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to save reminder"
            }
        }
    }

    // --- AI Care Assistant ---

    fun sendCareMessage(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return
        val profile = (profileState.value as? UiState.Success)?.data
        if (profile?.aiHealthDataConsent != true) {
            _toastMessage.value = if (_language.value == AppLanguage.HI) {
                "AI सहायक का उपयोग करने के लिए पहले सहमति दें।"
            } else {
                "Please enable AI Care consent first."
            }
            return
        }

        val userMsg = ChatMessage(id = "u_${System.currentTimeMillis()}", isUser = true, text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        _aiStreamState.value = AiStreamState.Loading

        val summary = cycleSummary.value
        val contextInfo = buildString {
            summary.currentCycleDay?.let { append("Cycle day: $it. ") }
            append("Avg cycle length: ${summary.averageCycleLength} days.")
        }

        viewModelScope.launch {
            var latestReply = ""
            try {
                aiRepository.streamCareReply(
                    userMessage = trimmed,
                    language = _language.value,
                    ageGroup = profile.resolvedAgeGroup(),
                    cycleContextSummary = contextInfo,
                ).collect { partial ->
                    latestReply = partial
                    _aiStreamState.value = AiStreamState.Streaming(partial)
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = "a_${System.currentTimeMillis()}",
                    isUser = false,
                    text = latestReply,
                )
                _aiStreamState.value = AiStreamState.Idle
            } catch (e: TimeoutCancellationException) {
                _aiStreamState.value = AiStreamState.Error("Request timed out. Please check built-in FAQs below.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "AI Care Assistant unavailable right now. Browse built-in FAQs below."
                _aiStreamState.value = AiStreamState.Error(msg)
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = "e_${System.currentTimeMillis()}",
                    isUser = false,
                    text = msg,
                    isError = true,
                )
            }
        }
    }

    // --- Support, Export, Delete, Admin ---

    fun submitSupportTicket(subject: String, message: String) {
        viewModelScope.launch {
            val res = repository.createSupportRequest(currentUserId, subject, message)
            _toastMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.HI) "सहायता अनुरोध भेजा गया" else "Support request sent"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to send request"
            }
        }
    }

    fun exportDataJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.exportUserDataJson(currentUserId)
            res.onSuccess { json -> onExportReady(json) }
                .onFailure { err -> _toastMessage.value = err.localizedMessage ?: "Export failed" }
        }
    }

    fun deleteAccountData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteAllUserData(currentUserId)
            if (res.isSuccess) {
                securityManager.clearAllLocalSecurity()
                onDeleted()
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to delete data"
            }
        }
    }

    fun adminPublishArticle(
        titleEn: String,
        titleHi: String,
        summaryEn: String,
        summaryHi: String,
        bodyEn: String,
        bodyHi: String,
        category: String,
        ageFor: String,
    ) {
        viewModelScope.launch {
            val res = repository.adminCreateArticle(
                currentUserId, titleEn, titleHi, summaryEn, summaryHi, bodyEn, bodyHi, category, ageFor
            )
            _toastMessage.value = if (res.isSuccess) "Article published (Draft – pending doctor review)" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminPublishTip(textEn: String, textHi: String, category: String) {
        viewModelScope.launch {
            val res = repository.adminCreateTip(currentUserId, textEn, textHi, category)
            _toastMessage.value = if (res.isSuccess) "Wellness tip added" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminPublishFaq(qEn: String, qHi: String, aEn: String, aHi: String, category: String) {
        viewModelScope.launch {
            val res = repository.adminCreateFaq(currentUserId, qEn, qHi, aEn, aHi, category)
            _toastMessage.value = if (res.isSuccess) "FAQ added" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminReplyTicket(requestId: String, status: String, reply: String) {
        viewModelScope.launch {
            val res = repository.adminReplySupportRequest(currentUserId, requestId, status, reply)
            _toastMessage.value = if (res.isSuccess) "Support ticket updated" else res.exceptionOrNull()?.localizedMessage
        }
    }

    private companion object {
        const val TAG = "PinkoraVM"
    }
}
