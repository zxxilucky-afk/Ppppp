package com.example.domain

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class AgeGroup(val wireValue: String) {
    TEEN_13_17("teen_13_17"),
    ADULT_18_PLUS("adult_18_plus");

    companion object {
        fun fromWire(value: String?): AgeGroup =
            entries.firstOrNull { it.wireValue == value } ?: ADULT_18_PLUS
    }
}

enum class FlowIntensity(val wireValue: String) {
    SPOTTING("spotting"),
    LIGHT("light"),
    MEDIUM("medium"),
    HEAVY("heavy");

    companion object {
        fun fromWire(value: String?): FlowIntensity =
            entries.firstOrNull { it.wireValue == value } ?: MEDIUM
    }
}

enum class MoodType(val wireValue: String) {
    HAPPY("happy"),
    SAD("sad"),
    ANGRY("angry"),
    ANXIOUS("anxious"),
    STRESSED("stressed"),
    TIRED("tired"),
    RELAXED("relaxed"),
    EMOTIONAL("emotional");

    companion object {
        fun fromWire(value: String?): MoodType =
            entries.firstOrNull { it.wireValue == value } ?: RELAXED
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val preferredName: String = "",
    val email: String = "",
    val ageGroup: String = AgeGroup.ADULT_18_PLUS.wireValue,
    val language: String = "en",
    val parentalConsentConfirmed: Boolean = false,
    val aiHealthDataConsent: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val averageCycleLength: Long = 28L,
    val averagePeriodLength: Long = 5L,
    val ovulationTrackingEnabled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun resolvedAgeGroup(): AgeGroup = AgeGroup.fromWire(ageGroup)
    fun isTeen(): Boolean = resolvedAgeGroup() == AgeGroup.TEEN_13_17
}

@Keep
@IgnoreExtraProperties
data class PeriodRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val startDateEpochDay: Long = 0L,
    val endDateEpochDay: Long = 0L,
    val flowIntensity: String = FlowIntensity.MEDIUM.wireValue,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SymptomRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val symptoms: List<String> = emptyList(),
    val painIntensity: Long = 0L,
    val redFlagAlert: Boolean = false,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MoodRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val mood: String = MoodType.RELAXED.wireValue,
    val energyLevel: Long = 3L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class DiaryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val dateEpochDay: Long = 0L,
    val moodTag: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ReminderSetting(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "water",
    val enabled: Boolean = false,
    val hour: Long = 9L,
    val minute: Long = 0L,
    val intervalHours: Long = 2L,
    val discreetText: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class WaterLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val glassesCount: Long = 0L,
    val targetGlasses: Long = 8L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class EducationalArticle(
    @DocumentId val id: String = "",
    val titleEn: String = "",
    val titleHi: String = "",
    val summaryEn: String = "",
    val summaryHi: String = "",
    val bodyEn: String = "",
    val bodyHi: String = "",
    val category: String = "hygiene",
    val ageAppropriateFor: String = "all",
    val reviewStatus: String = "draft_pending_doctor_review",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class WellnessTip(
    @DocumentId val id: String = "",
    val textEn: String = "",
    val textHi: String = "",
    val category: String = "comfort",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class FaqItem(
    @DocumentId val id: String = "",
    val questionEn: String = "",
    val questionHi: String = "",
    val answerEn: String = "",
    val answerHi: String = "",
    val category: String = "general",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SupportRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "open",
    val adminReply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AdminLog(
    @DocumentId val id: String = "",
    val adminId: String = "",
    val action: String = "",
    val target: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)
