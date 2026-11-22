package com.example.domain

import kotlin.math.roundToInt

enum class CyclePhase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATORY,
    LUTEAL,
    UNKNOWN,
}

data class CycleSummary(
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val observedCycleLengths: List<Int>,
    val isIrregular: Boolean,
    val currentCycleDay: Int?,
    val currentPhase: CyclePhase,
    val lastPeriodStartEpochDay: Long?,
    val estimatedNextPeriodStartEpochDay: Long?,
    val estimatedNextPeriodEndEpochDay: Long?,
    val daysUntilNextPeriod: Int?,
    val estimatedOvulationEpochDay: Long?,
    val fertileWindowRange: LongRange?,
)

object CycleCalculator {
    const val MIN_VALID_CYCLE_DAYS = 18
    const val MAX_VALID_CYCLE_DAYS = 60

    fun calculate(
        records: List<PeriodRecord>,
        todayEpochDay: Long,
        fallbackCycleLength: Int = 28,
        fallbackPeriodLength: Int = 5,
        ageGroup: AgeGroup = AgeGroup.ADULT_18_PLUS,
        ovulationEnabled: Boolean = false,
    ): CycleSummary {
        val sorted = records
            .filter { it.startDateEpochDay > 0L }
            .sortedBy { it.startDateEpochDay }

        val safeFallbackCycle = fallbackCycleLength.coerceIn(21, 45)
        val safeFallbackPeriod = fallbackPeriodLength.coerceIn(2, 10)

        if (sorted.isEmpty()) {
            return CycleSummary(
                averageCycleLength = safeFallbackCycle,
                averagePeriodLength = safeFallbackPeriod,
                observedCycleLengths = emptyList(),
                isIrregular = false,
                currentCycleDay = null,
                currentPhase = CyclePhase.UNKNOWN,
                lastPeriodStartEpochDay = null,
                estimatedNextPeriodStartEpochDay = null,
                estimatedNextPeriodEndEpochDay = null,
                daysUntilNextPeriod = null,
                estimatedOvulationEpochDay = null,
                fertileWindowRange = null,
            )
        }

        val observedCycles = sorted.zipWithNext { prev, next ->
            (next.startDateEpochDay - prev.startDateEpochDay).toInt()
        }.filter { it in MIN_VALID_CYCLE_DAYS..MAX_VALID_CYCLE_DAYS }

        val avgCycle = if (observedCycles.isNotEmpty()) {
            observedCycles.average().roundToInt().coerceIn(21, 45)
        } else {
            safeFallbackCycle
        }

        val periodLengths = sorted.mapNotNull { rec ->
            if (rec.endDateEpochDay >= rec.startDateEpochDay) {
                (rec.endDateEpochDay - rec.startDateEpochDay + 1).toInt().coerceIn(1, 14)
            } else {
                null
            }
        }
        val avgPeriod = if (periodLengths.isNotEmpty()) {
            periodLengths.average().roundToInt().coerceIn(2, 10)
        } else {
            safeFallbackPeriod
        }

        val isIrregular = if (observedCycles.size >= 2) {
            val minC = observedCycles.min()
            val maxC = observedCycles.max()
            (maxC - minC) > 7 || observedCycles.any { it < 21 || it > 35 }
        } else if (observedCycles.size == 1) {
            observedCycles.first() < 21 || observedCycles.first() > 35
        } else {
            false
        }

        val latest = sorted.last()
        val lastStart = latest.startDateEpochDay
        val currentDayRaw = (todayEpochDay - lastStart + 1).toInt()
        val currentCycleDay = if (currentDayRaw >= 1) currentDayRaw else null

        var nextStart = lastStart + avgCycle
        while (nextStart < todayEpochDay) {
            nextStart += avgCycle
        }
        val nextEnd = nextStart + (avgPeriod - 1)
        val daysUntil = (nextStart - todayEpochDay).toInt()

        val activeEnd = if (latest.endDateEpochDay >= latest.startDateEpochDay) {
            latest.endDateEpochDay
        } else {
            latest.startDateEpochDay + avgPeriod - 1
        }

        val ovulationDayRaw = nextStart - 14
        val showOvulation = ageGroup == AgeGroup.ADULT_18_PLUS && ovulationEnabled

        val phase = when {
            todayEpochDay in latest.startDateEpochDay..activeEnd -> CyclePhase.MENSTRUAL
            currentCycleDay == null -> CyclePhase.UNKNOWN
            todayEpochDay < ovulationDayRaw - 2 -> CyclePhase.FOLLICULAR
            todayEpochDay in (ovulationDayRaw - 2)..(ovulationDayRaw + 1) -> CyclePhase.OVULATORY
            else -> CyclePhase.LUTEAL
        }

        return CycleSummary(
            averageCycleLength = avgCycle,
            averagePeriodLength = avgPeriod,
            observedCycleLengths = observedCycles,
            isIrregular = isIrregular,
            currentCycleDay = currentCycleDay,
            currentPhase = phase,
            lastPeriodStartEpochDay = lastStart,
            estimatedNextPeriodStartEpochDay = nextStart,
            estimatedNextPeriodEndEpochDay = nextEnd,
            daysUntilNextPeriod = daysUntil,
            estimatedOvulationEpochDay = if (showOvulation) ovulationDayRaw else null,
            fertileWindowRange = if (showOvulation) (ovulationDayRaw - 5)..(ovulationDayRaw + 1) else null,
        )
    }

    fun isLoggedPeriodDay(epochDay: Long, records: List<PeriodRecord>, defaultLength: Int = 5): Boolean {
        return records.any { rec ->
            val end = if (rec.endDateEpochDay >= rec.startDateEpochDay) {
                rec.endDateEpochDay
            } else {
                rec.startDateEpochDay + defaultLength - 1
            }
            epochDay in rec.startDateEpochDay..end
        }
    }

    fun isPredictedPeriodDay(epochDay: Long, summary: CycleSummary): Boolean {
        val start = summary.estimatedNextPeriodStartEpochDay ?: return false
        val end = summary.estimatedNextPeriodEndEpochDay ?: return false
        return epochDay in start..end
    }
}
