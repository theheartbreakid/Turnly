package com.crescentapps.turnly.domain.engine

import com.crescentapps.turnly.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RotationEngineTest {

    private val participantA = Participant(id = 1L, name = "A")
    private val participantB = Participant(id = 2L, name = "B")
    private val participantC = Participant(id = 3L, name = "C")
    private val participants = listOf(participantA, participantB, participantC)

    @Test
    fun testDailySequentialRotation() {
        val schedule = Schedule(
            id = 1L,
            name = "Test Daily",
            frequencyType = FrequencyType.DAILY,
            frequencyInterval = 1,
            startDate = "2026-09-20"
        )

        // 20 Sep -> A, 21 Sep -> B, 22 Sep -> C, 23 Sep -> A
        val turn20 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-20"))
        val turn21 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-21"))
        val turn22 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-22"))
        val turn23 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-23"))

        assertEquals(participantA.id, turn20?.id)
        assertEquals(participantB.id, turn21?.id)
        assertEquals(participantC.id, turn22?.id)
        assertEquals(participantA.id, turn23?.id)
    }

    @Test
    fun testCustomRepeatingPattern() {
        // Pattern: A -> A -> B -> C
        val pattern = listOf(
            RotationPatternItem(scheduleId = 1L, participantId = participantA.id, patternPosition = 0),
            RotationPatternItem(scheduleId = 1L, participantId = participantA.id, patternPosition = 1),
            RotationPatternItem(scheduleId = 1L, participantId = participantB.id, patternPosition = 2),
            RotationPatternItem(scheduleId = 1L, participantId = participantC.id, patternPosition = 3)
        )

        val schedule = Schedule(
            id = 1L,
            name = "Test Repeating Pattern",
            frequencyType = FrequencyType.DAILY,
            startDate = "2026-09-20"
        )

        val turn20 = RotationEngine.calculateParticipantForDate(schedule, participants, pattern, LocalDate.parse("2026-09-20"))
        val turn21 = RotationEngine.calculateParticipantForDate(schedule, participants, pattern, LocalDate.parse("2026-09-21"))
        val turn22 = RotationEngine.calculateParticipantForDate(schedule, participants, pattern, LocalDate.parse("2026-09-22"))
        val turn23 = RotationEngine.calculateParticipantForDate(schedule, participants, pattern, LocalDate.parse("2026-09-23"))
        val turn24 = RotationEngine.calculateParticipantForDate(schedule, participants, pattern, LocalDate.parse("2026-09-24"))

        assertEquals(participantA.id, turn20?.id)
        assertEquals(participantA.id, turn21?.id)
        assertEquals(participantB.id, turn22?.id)
        assertEquals(participantC.id, turn23?.id)
        assertEquals(participantA.id, turn24?.id)
    }

    @Test
    fun testWeeklyRotation() {
        // 2026-09-21 is a Monday
        val schedule = Schedule(
            id = 1L,
            name = "Test Weekly",
            frequencyType = FrequencyType.WEEKLY,
            frequencyInterval = 1,
            startDate = "2026-09-21"
        )

        val monday1 = LocalDate.parse("2026-09-21")
        val tuesday = LocalDate.parse("2026-09-22")
        val monday2 = LocalDate.parse("2026-09-28")
        val monday3 = LocalDate.parse("2026-10-05")

        assertEquals(participantA.id, RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), monday1)?.id)
        assertNull(RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), tuesday))
        assertEquals(participantB.id, RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), monday2)?.id)
        assertEquals(participantC.id, RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), monday3)?.id)
    }

    @Test
    fun testSkipDateHandling() {
        val schedule = Schedule(
            id = 1L,
            name = "Test Skip",
            frequencyType = FrequencyType.DAILY,
            startDate = "2026-09-20"
        )
        val skipDates = setOf("2026-09-21")

        val turn20 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-20"), skipDates = skipDates)
        val turn21 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-21"), skipDates = skipDates)
        val turn22 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-22"), skipDates = skipDates)

        assertEquals(participantA.id, turn20?.id)
        assertNull(turn21) // Skipped date returns null
        assertEquals(participantB.id, turn22?.id) // Next turn continues with B
    }

    @Test
    fun testManualOverrideSingleDate() {
        val schedule = Schedule(
            id = 1L,
            name = "Test Override Single Date",
            frequencyType = FrequencyType.DAILY,
            startDate = "2026-09-20",
            overrideStrategy = OverrideStrategy.SINGLE_DATE_ONLY
        )
        // Manual override on 21 Sep -> assign C instead of B
        val manual = mapOf("2026-09-21" to participantC.id)

        val turn20 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-20"), manualAssignments = manual)
        val turn21 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-21"), manualAssignments = manual)
        val turn22 = RotationEngine.calculateParticipantForDate(schedule, participants, emptyList(), LocalDate.parse("2026-09-22"), manualAssignments = manual)

        assertEquals(participantA.id, turn20?.id)
        assertEquals(participantC.id, turn21?.id) // Overridden to C
        assertEquals(participantC.id, turn22?.id) // Normal cycle for day index 2 was C
    }

    @Test
    fun testPausedScheduleHalt() {
        val schedule = Schedule(
            id = 1L,
            name = "Test Paused",
            frequencyType = FrequencyType.DAILY,
            startDate = "2026-09-20",
            isPaused = true,
            pauseUntil = "2026-09-25"
        )

        val duringPause = LocalDate.parse("2026-09-22")
        val afterPause = LocalDate.parse("2026-09-26")

        assertFalse(RotationEngine.isScheduledDate(schedule, duringPause))
        assertTrue(RotationEngine.isScheduledDate(schedule, afterPause))
    }
}
