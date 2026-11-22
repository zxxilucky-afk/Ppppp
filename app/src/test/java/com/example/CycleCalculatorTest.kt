package com.example

import com.example.domain.AgeGroup
import com.example.domain.CycleCalculator
import com.example.domain.CyclePhase
import com.example.domain.PeriodRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleCalculatorTest {

    @Test
    fun emptyHistory_usesSafeFallbacksAndNoPredictions() {
        val summary = CycleCalculator.calculate(
            records = emptyList(),
            todayEpochDay = 20000L,
            fallbackCycleLength = 29,
            fallbackPeriodLength = 4,
            ageGroup = AgeGroup.ADULT_18_PLUS,
            ovulationEnabled = true,
        )

        assertEquals(29, summary.averageCycleLength)
        assertEquals(4, summary.averagePeriodLength)
        assertFalse(summary.isIrregular)
        assertNull(summary.currentCycleDay)
        assertEquals(CyclePhase.UNKNOWN, summary.currentPhase)
        assertNull(summary.estimatedNextPeriodStartEpochDay)
        assertNull(summary.estimatedOvulationEpochDay)
        assertNull(summary.fertileWindowRange)
    }

    @Test
    fun regularCycles_computesAverageAndNextPrediction() {
        val records = listOf(
            PeriodRecord(id = "1", startDateEpochDay = 19900L, endDateEpochDay = 19904L),
            PeriodRecord(id = "2", startDateEpochDay = 19928L, endDateEpochDay = 19932L),
            PeriodRecord(id = "3", startDateEpochDay = 19956L, endDateEpochDay = 19960L),
        )

        val summary = CycleCalculator.calculate(
            records = records,
            todayEpochDay = 19970L, // Day 15 of cycle (19970 - 19956 + 1)
            ageGroup = AgeGroup.ADULT_18_PLUS,
            ovulationEnabled = true,
        )

        assertEquals(28, summary.averageCycleLength)
        assertEquals(5, summary.averagePeriodLength)
        assertFalse(summary.isIrregular)
        assertEquals(15, summary.currentCycleDay)
        assertEquals(19984L, summary.estimatedNextPeriodStartEpochDay)
        assertEquals(19988L, summary.estimatedNextPeriodEndEpochDay)
        assertEquals(14, summary.daysUntilNextPeriod)
        assertEquals(19970L, summary.estimatedOvulationEpochDay)
        assertNotNull(summary.fertileWindowRange)
        assertEquals(CyclePhase.OVULATORY, summary.currentPhase)
    }

    @Test
    fun teenAccount_neverExposesOvulationOrFertileWindow() {
        val records = listOf(
            PeriodRecord(id = "1", startDateEpochDay = 19900L, endDateEpochDay = 19904L),
            PeriodRecord(id = "2", startDateEpochDay = 19928L, endDateEpochDay = 19932L),
        )

        val summary = CycleCalculator.calculate(
            records = records,
            todayEpochDay = 19940L,
            ageGroup = AgeGroup.TEEN_13_17,
            ovulationEnabled = true, // even if true, must be suppressed for 13-17
        )

        assertNull(summary.estimatedOvulationEpochDay)
        assertNull(summary.fertileWindowRange)
        assertEquals(19956L, summary.estimatedNextPeriodStartEpochDay)
    }

    @Test
    fun irregularCycles_detectedWhenVarianceExceedsSevenDaysOrOutOfRange() {
        val varied = listOf(
            PeriodRecord(id = "1", startDateEpochDay = 19900L, endDateEpochDay = 19904L),
            PeriodRecord(id = "2", startDateEpochDay = 19922L, endDateEpochDay = 19926L), // 22 days
            PeriodRecord(id = "3", startDateEpochDay = 19957L, endDateEpochDay = 19961L), // 35 days (diff 13 > 7)
        )

        val summary = CycleCalculator.calculate(
            records = varied,
            todayEpochDay = 19959L,
        )

        assertTrue(summary.isIrregular)
        assertEquals(CyclePhase.MENSTRUAL, summary.currentPhase)
        assertTrue(CycleCalculator.isLoggedPeriodDay(19959L, varied))
        assertFalse(CycleCalculator.isLoggedPeriodDay(19965L, varied))
        assertTrue(CycleCalculator.isPredictedPeriodDay(summary.estimatedNextPeriodStartEpochDay!!, summary))
    }
}
