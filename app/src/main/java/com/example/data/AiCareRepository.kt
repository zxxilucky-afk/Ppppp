package com.example.data

import android.util.Log
import com.example.domain.AgeGroup
import com.example.i18n.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class AiCareRepository {
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are PINKORA Care Assistant, a warm, respectful, non-judgmental women's health & menstrual wellness companion. " +
                        "Rules: (1) NEVER diagnose medical conditions or prescribe/recommend specific medicines or dosages. " +
                        "(2) Offer gentle, evidence-based general education on menstrual hygiene, cycle rhythm, mild comfort self-care (warm compress, rest, hydration), nutrition, puberty, first period support, and emotional wellness. " +
                        "(3) If the user mentions severe pain, fainting, dizziness, soaking pads hourly, fever, or unusual bleeding, clearly advise seeking immediate medical help from a qualified doctor. " +
                        "(4) If ageGroup is teen_13_17, keep language strictly teen-safe, reassuring, and age-appropriate. " +
                        "(5) Respond in the requested language (Hindi Devanagari or clear English). Keep replies concise, supportive, and easy to read."
                )
            },
        )
    }

    fun streamCareReply(
        userMessage: String,
        language: AppLanguage,
        ageGroup: AgeGroup,
        cycleContextSummary: String,
    ): Flow<String> = flow {
        val langInstruction = if (language == AppLanguage.HI) {
            "Respond in warm, natural Hindi (Devanagari script)."
        } else {
            "Respond in warm, clear English."
        }
        val ageInstruction = if (ageGroup == AgeGroup.TEEN_13_17) {
            "Audience: Teenager (13-17 years old). Keep tone supportive, educational, and teen-safe."
        } else {
            "Audience: Adult (18+ years old)."
        }
        val prompt = buildString {
            appendLine(langInstruction)
            appendLine(ageInstruction)
            if (cycleContextSummary.isNotBlank()) {
                appendLine("Consented wellness context: $cycleContextSummary")
            }
            appendLine("User question: $userMessage")
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Care Assistant timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Care Assistant error", e)
            throw e
        }
    }

    private companion object {
        const val TAG = "AiCareRepo"
    }
}
