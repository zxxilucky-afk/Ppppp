package com.example.data

import android.content.Context
import com.example.R
import com.example.domain.AdminLog
import com.example.domain.AgeGroup
import com.example.domain.DiaryEntry
import com.example.domain.EducationalArticle
import com.example.domain.FaqItem
import com.example.domain.FlowIntensity
import com.example.domain.MoodRecord
import com.example.domain.MoodType
import com.example.domain.PeriodRecord
import com.example.domain.ReminderSetting
import com.example.domain.SupportRequest
import com.example.domain.SymptomRecord
import com.example.domain.UserProfile
import com.example.domain.WaterLog
import com.example.domain.WellnessTip
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class PinkoraRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(createFirestore(context.applicationContext))

    companion object {
        private const val ADMIN_BOOTSTRAP_EMAIL = "luckymbbs44@gmail.com"

        fun createFirestore(context: Context): FirebaseFirestore {
            val databaseId = context.getString(R.string.firestore_database_id)
            val instance = FirebaseFirestore.getInstance(databaseId)
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                instance.firestoreSettings = settings
            } catch (_: IllegalStateException) {
                // Already initialized with settings
            }
            return instance
        }
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun verifyMatchingUser(expectedUserId: String) {
        val uid = requireUserId()
        check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user access." }
    }

    // --- Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }

    suspend fun saveOrUpdateProfile(
        userId: String,
        preferredName: String,
        email: String?,
        ageGroup: AgeGroup,
        language: String,
        parentalConsentConfirmed: Boolean,
        aiHealthDataConsent: Boolean,
        onboardingCompleted: Boolean,
        averageCycleLength: Int,
        averagePeriodLength: Int,
        ovulationTrackingEnabled: Boolean,
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            verifyMatchingUser(userId)
            val cleanName = preferredName.trim().take(50).ifBlank { "Friend" }
            val safeAge = ageGroup.wireValue
            val safeLang = if (language == "hi") "hi" else "en"
            val safeCycle = averageCycleLength.coerceIn(21, 45).toLong()
            val safePeriod = averagePeriodLength.coerceIn(2, 10).toLong()
            val safeOvulation = if (ageGroup == AgeGroup.ADULT_18_PLUS) ovulationTrackingEnabled else false

            val docRef = db.collection("users").document(userId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updateMap = mutableMapOf<String, Any>(
                    "preferredName" to cleanName,
                    "ageGroup" to safeAge,
                    "language" to safeLang,
                    "parentalConsentConfirmed" to parentalConsentConfirmed,
                    "aiHealthDataConsent" to aiHealthDataConsent,
                    "onboardingCompleted" to onboardingCompleted,
                    "averageCycleLength" to safeCycle,
                    "averagePeriodLength" to safePeriod,
                    "ovulationTrackingEnabled" to safeOvulation,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (!email.isNullOrBlank()) {
                    updateMap["email"] = email.trim().take(120)
                }
                docRef.update(updateMap).await()
            } else {
                val createMap = mutableMapOf<String, Any>(
                    "userId" to userId,
                    "preferredName" to cleanName,
                    "ageGroup" to safeAge,
                    "language" to safeLang,
                    "parentalConsentConfirmed" to parentalConsentConfirmed,
                    "aiHealthDataConsent" to aiHealthDataConsent,
                    "onboardingCompleted" to onboardingCompleted,
                    "averageCycleLength" to safeCycle,
                    "averagePeriodLength" to safePeriod,
                    "ovulationTrackingEnabled" to safeOvulation,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (!email.isNullOrBlank()) {
                    createMap["email"] = email.trim().take(120)
                }
                docRef.set(createMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- Period Records ---

    fun observePeriodRecords(userId: String): Flow<List<PeriodRecord>> =
        db.collection("users").document(userId).collection("period_records")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PeriodRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.startDateEpochDay }
            }

    suspend fun savePeriodRecord(
        userId: String,
        recordId: String?,
        startDateEpochDay: Long,
        endDateEpochDay: Long,
        flowIntensity: FlowIntensity,
        notes: String,
    ): Result<Unit> {
        val colRef = db.collection("users").document(userId).collection("period_records")
        val docRef = if (recordId.isNullOrBlank()) colRef.document() else colRef.document(recordId)
        return try {
            verifyMatchingUser(userId)
            val safeStart = startDateEpochDay.coerceIn(10000L, 40000L)
            val safeEnd = if (endDateEpochDay == 0L) 0L else endDateEpochDay.coerceIn(safeStart, 40000L)
            val cleanNotes = notes.trim().take(500)
            if (recordId.isNullOrBlank()) {
                val payload = mutableMapOf<String, Any>(
                    "userId" to userId,
                    "startDateEpochDay" to safeStart,
                    "endDateEpochDay" to safeEnd,
                    "flowIntensity" to flowIntensity.wireValue,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (cleanNotes.isNotEmpty()) payload["notes"] = cleanNotes
                docRef.set(payload).await()
            } else {
                val payload = mutableMapOf<String, Any>(
                    "startDateEpochDay" to safeStart,
                    "endDateEpochDay" to safeEnd,
                    "flowIntensity" to flowIntensity.wireValue,
                    "notes" to cleanNotes,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePeriodRecord(userId: String, recordId: String): Result<Unit> {
        val docPath = "users/$userId/period_records/$recordId"
        return try {
            verifyMatchingUser(userId)
            require(recordId.isNotBlank())
            db.document(docPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Symptom Records ---

    fun observeSymptomRecords(userId: String): Flow<List<SymptomRecord>> =
        db.collection("users").document(userId).collection("symptom_records")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SymptomRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }

    suspend fun addSymptomRecord(
        userId: String,
        dateEpochDay: Long,
        symptoms: List<String>,
        painIntensity: Int,
        notes: String,
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("symptom_records").document()
        return try {
            verifyMatchingUser(userId)
            val safeSymptoms = symptoms.take(10).map { it.trim().take(50) }.filter { it.isNotEmpty() }
            val safePain = painIntensity.coerceIn(0, 10).toLong()
            val redFlag = safePain >= 8L || safeSymptoms.contains("fainting_heavy")
            val cleanNotes = notes.trim().take(500)
            val payload = mutableMapOf<String, Any>(
                "userId" to userId,
                "dateEpochDay" to dateEpochDay.coerceIn(10000L, 40000L),
                "symptoms" to safeSymptoms,
                "painIntensity" to safePain,
                "redFlagAlert" to redFlag,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            if (cleanNotes.isNotEmpty()) payload["notes"] = cleanNotes
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSymptomRecord(userId: String, recordId: String): Result<Unit> {
        val docPath = "users/$userId/symptom_records/$recordId"
        return try {
            verifyMatchingUser(userId)
            require(recordId.isNotBlank())
            db.document(docPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Mood Records ---

    fun observeMoodRecords(userId: String): Flow<List<MoodRecord>> =
        db.collection("users").document(userId).collection("mood_records")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MoodRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }

    suspend fun addMoodRecord(
        userId: String,
        dateEpochDay: Long,
        mood: MoodType,
        energyLevel: Int,
        notes: String,
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("mood_records").document()
        return try {
            verifyMatchingUser(userId)
            val cleanNotes = notes.trim().take(500)
            val payload = mutableMapOf<String, Any>(
                "userId" to userId,
                "dateEpochDay" to dateEpochDay.coerceIn(10000L, 40000L),
                "mood" to mood.wireValue,
                "energyLevel" to energyLevel.coerceIn(1, 5).toLong(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            if (cleanNotes.isNotEmpty()) payload["notes"] = cleanNotes
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMoodRecord(userId: String, recordId: String): Result<Unit> {
        val docPath = "users/$userId/mood_records/$recordId"
        return try {
            verifyMatchingUser(userId)
            require(recordId.isNotBlank())
            db.document(docPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Private Diary ---

    fun observeDiaryEntries(userId: String): Flow<List<DiaryEntry>> =
        db.collection("users").document(userId).collection("diary_entries")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DiaryEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.dateEpochDay }
            }

    suspend fun saveDiaryEntry(
        userId: String,
        entryId: String?,
        title: String,
        body: String,
        dateEpochDay: Long,
        moodTag: String,
    ): Result<Unit> {
        val col = db.collection("users").document(userId).collection("diary_entries")
        val docRef = if (entryId.isNullOrBlank()) col.document() else col.document(entryId)
        return try {
            verifyMatchingUser(userId)
            val safeTitle = title.trim().take(120)
            val safeBody = body.trim().take(5000)
            require(safeTitle.isNotEmpty() && safeBody.isNotEmpty()) { "Title and entry text are required." }
            val safeTag = moodTag.trim().take(40)
            if (entryId.isNullOrBlank()) {
                val payload = mutableMapOf<String, Any>(
                    "userId" to userId,
                    "title" to safeTitle,
                    "body" to safeBody,
                    "dateEpochDay" to dateEpochDay.coerceIn(10000L, 40000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (safeTag.isNotEmpty()) payload["moodTag"] = safeTag
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to safeTitle,
                        "body" to safeBody,
                        "dateEpochDay" to dateEpochDay.coerceIn(10000L, 40000L),
                        "moodTag" to safeTag,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteDiaryEntry(userId: String, entryId: String): Result<Unit> {
        val docPath = "users/$userId/diary_entries/$entryId"
        return try {
            verifyMatchingUser(userId)
            require(entryId.isNotBlank())
            db.document(docPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Water Logs ---

    fun observeWaterLogs(userId: String): Flow<List<WaterLog>> =
        db.collection("users").document(userId).collection("water_logs")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WaterLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    suspend fun upsertWaterLog(
        userId: String,
        dateEpochDay: Long,
        glassesCount: Int,
        targetGlasses: Int,
    ): Result<Unit> {
        val docId = "day_$dateEpochDay"
        val docRef = db.collection("users").document(userId).collection("water_logs").document(docId)
        return try {
            verifyMatchingUser(userId)
            val safeGlasses = glassesCount.coerceIn(0, 30).toLong()
            val safeTarget = targetGlasses.coerceIn(1, 20).toLong()
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "glassesCount" to safeGlasses,
                        "targetGlasses" to safeTarget,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "dateEpochDay" to dateEpochDay.coerceIn(10000L, 40000L),
                        "glassesCount" to safeGlasses,
                        "targetGlasses" to safeTarget,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Reminders ---

    fun observeReminders(userId: String): Flow<List<ReminderSetting>> =
        db.collection("users").document(userId).collection("reminders")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ReminderSetting::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    suspend fun saveReminder(
        userId: String,
        type: String,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        intervalHours: Int,
        discreetText: Boolean,
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("reminders").document(type)
        return try {
            verifyMatchingUser(userId)
            val snap = docRef.get().await()
            val safeHour = hour.coerceIn(0, 23).toLong()
            val safeMinute = minute.coerceIn(0, 59).toLong()
            val safeInterval = intervalHours.coerceIn(0, 24).toLong()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "enabled" to enabled,
                        "hour" to safeHour,
                        "minute" to safeMinute,
                        "intervalHours" to safeInterval,
                        "discreetText" to discreetText,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "type" to type,
                        "enabled" to enabled,
                        "hour" to safeHour,
                        "minute" to safeMinute,
                        "intervalHours" to safeInterval,
                        "discreetText" to discreetText,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Shared Content (Articles, Tips, FAQs) ---

    fun observeArticles(): Flow<List<EducationalArticle>> =
        db.collection("educational_articles")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(EducationalArticle::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeTips(): Flow<List<WellnessTip>> =
        db.collection("wellness_tips")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WellnessTip::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeFaqs(): Flow<List<FaqItem>> =
        db.collection("faq")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FaqItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    // --- Support Requests ---

    fun observeUserSupportRequests(userId: String): Flow<List<SupportRequest>> =
        db.collection("support_requests")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    suspend fun createSupportRequest(userId: String, subject: String, message: String): Result<Unit> {
        val docRef = db.collection("support_requests").document()
        return try {
            verifyMatchingUser(userId)
            val cleanSubject = subject.trim().take(120)
            val cleanMessage = message.trim().take(2000)
            require(cleanSubject.isNotEmpty() && cleanMessage.isNotEmpty()) { "Subject and message are required." }
            docRef.set(
                mapOf(
                    "userId" to userId,
                    "subject" to cleanSubject,
                    "message" to cleanMessage,
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Admin Operations ---

    suspend fun checkIsAdmin(userId: String): Boolean {
        val user = auth.currentUser ?: return false
        if (user.isEmailVerified && user.email.equals(ADMIN_BOOTSTRAP_EMAIL, ignoreCase = true)) {
            return true
        }
        return try {
            db.collection("admins").document(userId).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    fun observeAllSupportRequestsForAdmin(): Flow<List<SupportRequest>> =
        db.collection("support_requests")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeAdminLogs(): Flow<List<AdminLog>> =
        db.collection("admin_logs")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AdminLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    suspend fun adminCreateArticle(
        adminId: String,
        titleEn: String,
        titleHi: String,
        summaryEn: String,
        summaryHi: String,
        bodyEn: String,
        bodyHi: String,
        category: String,
        ageAppropriateFor: String,
    ): Result<Unit> {
        val docRef = db.collection("educational_articles").document()
        return try {
            verifyMatchingUser(adminId)
            docRef.set(
                mapOf(
                    "titleEn" to titleEn.trim().take(150),
                    "titleHi" to titleHi.trim().take(150),
                    "summaryEn" to summaryEn.trim().take(300),
                    "summaryHi" to summaryHi.trim().take(300),
                    "bodyEn" to bodyEn.trim().take(8000),
                    "bodyHi" to bodyHi.trim().take(8000),
                    "category" to category,
                    "ageAppropriateFor" to ageAppropriateFor,
                    "reviewStatus" to "draft_pending_doctor_review",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            logAdminAction(adminId, "CREATE_ARTICLE", docRef.id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminCreateTip(
        adminId: String,
        textEn: String,
        textHi: String,
        category: String,
    ): Result<Unit> {
        val docRef = db.collection("wellness_tips").document()
        return try {
            verifyMatchingUser(adminId)
            docRef.set(
                mapOf(
                    "textEn" to textEn.trim().take(500),
                    "textHi" to textHi.trim().take(500),
                    "category" to category,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            logAdminAction(adminId, "CREATE_TIP", docRef.id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminCreateFaq(
        adminId: String,
        questionEn: String,
        questionHi: String,
        answerEn: String,
        answerHi: String,
        category: String,
    ): Result<Unit> {
        val docRef = db.collection("faq").document()
        return try {
            verifyMatchingUser(adminId)
            docRef.set(
                mapOf(
                    "questionEn" to questionEn.trim().take(300),
                    "questionHi" to questionHi.trim().take(300),
                    "answerEn" to answerEn.trim().take(2000),
                    "answerHi" to answerHi.trim().take(2000),
                    "category" to category.trim().take(60).ifBlank { "general" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            logAdminAction(adminId, "CREATE_FAQ", docRef.id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminReplySupportRequest(
        adminId: String,
        requestId: String,
        status: String,
        reply: String,
    ): Result<Unit> {
        val docPath = "support_requests/$requestId"
        return try {
            verifyMatchingUser(adminId)
            db.document(docPath).update(
                mapOf(
                    "status" to status,
                    "adminReply" to reply.trim().take(2000),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            logAdminAction(adminId, "REPLY_SUPPORT", requestId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    private suspend fun logAdminAction(adminId: String, action: String, target: String) {
        try {
            db.collection("admin_logs").document().set(
                mapOf(
                    "adminId" to adminId,
                    "action" to action.take(120),
                    "target" to target.take(120),
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } catch (_: Exception) {
            // Non-blocking audit log best-effort
        }
    }

    // --- Export & Account Deletion ---

    suspend fun exportUserDataJson(userId: String): Result<String> {
        return try {
            verifyMatchingUser(userId)
            val userDoc = db.collection("users").document(userId).get().await()
            val periods = db.collection("users").document(userId).collection("period_records").get().await()
            val symptoms = db.collection("users").document(userId).collection("symptom_records").get().await()
            val moods = db.collection("users").document(userId).collection("mood_records").get().await()
            val diary = db.collection("users").document(userId).collection("diary_entries").get().await()
            val water = db.collection("users").document(userId).collection("water_logs").get().await()

            val root = JSONObject().apply {
                put("exportedAtMillis", System.currentTimeMillis())
                put("profile", JSONObject(userDoc.data ?: emptyMap<String, Any>()))
                put("period_records", JSONArray(periods.documents.map { JSONObject(it.data ?: emptyMap<String, Any>()) }))
                put("symptom_records", JSONArray(symptoms.documents.map { JSONObject(it.data ?: emptyMap<String, Any>()) }))
                put("mood_records", JSONArray(moods.documents.map { JSONObject(it.data ?: emptyMap<String, Any>()) }))
                put("diary_entries", JSONArray(diary.documents.map { JSONObject(it.data ?: emptyMap<String, Any>()) }))
                put("water_logs", JSONArray(water.documents.map { JSONObject(it.data ?: emptyMap<String, Any>()) }))
            }
            Result.success(root.toString(2))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "users/$userId")
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserData(userId: String): Result<Unit> {
        val userDocPath = "users/$userId"
        return try {
            verifyMatchingUser(userId)
            val subcollections = listOf(
                "period_records",
                "symptom_records",
                "mood_records",
                "diary_entries",
                "reminders",
                "water_logs",
            )
            for (sub in subcollections) {
                val docs = db.collection("users").document(userId).collection(sub).get().await()
                for (doc in docs.documents) {
                    doc.reference.delete().await()
                }
            }
            val supportDocs = db.collection("support_requests").whereEqualTo("userId", userId).get().await()
            for (doc in supportDocs.documents) {
                doc.reference.delete().await()
            }
            db.document(userDocPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userDocPath)
            Result.failure(e)
        }
    }
}
