package com.crescentapps.turnly.domain.engine

import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object RotationEngine {

    /**
     * Determines whether the given date is an active scheduled occurrence date.
     * Checks start date, schedule frequency rule, pause windows, and explicit skip dates.
     */
    fun isScheduledDate(
        schedule: Schedule,
        date: LocalDate,
        skipDates: Set<String> = emptySet()
    ): Boolean {
        val startDate = DateUtils.parse(schedule.startDate)
        if (date.isBefore(startDate)) return false

        // Check if explicitly skipped
        val dateStr = DateUtils.format(date)
        if (skipDates.contains(dateStr)) return false

        // Check pause state
        if (schedule.isPaused) {
            val pauseUntil = schedule.pauseUntil?.let { runCatching { DateUtils.parse(it) }.getOrNull() }
            if (pauseUntil == null) {
                // Paused indefinitely from today or creation
                return false
            } else if (!date.isAfter(pauseUntil)) {
                return false
            }
        }

        return when (schedule.frequencyType) {
            FrequencyType.DAILY -> {
                val interval = schedule.frequencyInterval.coerceAtLeast(1)
                val daysDiff = ChronoUnit.DAYS.between(startDate, date)
                daysDiff % interval == 0L
            }
            FrequencyType.EVERY_X_DAYS -> {
                val interval = schedule.frequencyInterval.coerceAtLeast(1)
                val daysDiff = ChronoUnit.DAYS.between(startDate, date)
                daysDiff % interval == 0L
            }
            FrequencyType.WEEKLY -> {
                val interval = schedule.frequencyInterval.coerceAtLeast(1)
                val weeksDiff = ChronoUnit.WEEKS.between(startDate, date)
                val sameDayOfWeek = date.dayOfWeek == startDate.dayOfWeek
                sameDayOfWeek && (weeksDiff % interval == 0L)
            }
            FrequencyType.EVERY_X_WEEKS -> {
                val interval = schedule.frequencyInterval.coerceAtLeast(1)
                val weeksDiff = ChronoUnit.WEEKS.between(startDate, date)
                val sameDayOfWeek = date.dayOfWeek == startDate.dayOfWeek
                sameDayOfWeek && (weeksDiff % interval == 0L)
            }
            FrequencyType.CUSTOM_WEEKDAYS -> {
                val selected = if (schedule.selectedWeekdays.isEmpty()) {
                    listOf(startDate.dayOfWeek.value)
                } else {
                    schedule.selectedWeekdays
                }
                selected.contains(date.dayOfWeek.value)
            }
            FrequencyType.MONTHLY -> {
                val targetDay = schedule.dayOfMonth ?: startDate.dayOfMonth
                val lengthOfMonth = YearMonth.from(date).lengthOfMonth()
                val adjustedDay = targetDay.coerceAtMost(lengthOfMonth)
                date.dayOfMonth == adjustedDay
            }
        }
    }

    /**
     * Resolves the list of active occurrences up to a given date or for an index,
     * deterministically stepping from startDate to targetDate.
     */
    fun calculateParticipantForDate(
        schedule: Schedule,
        participants: List<Participant>,
        pattern: List<RotationPatternItem>,
        targetDate: LocalDate,
        manualAssignments: Map<String, Long> = emptyMap(), // dateStr -> participantId
        skipDates: Set<String> = emptySet(),
        persistedOccurrences: Map<String, TurnOccurrence> = emptyMap()
    ): Participant? {
        val targetDateStr = DateUtils.format(targetDate)

        // 1. If there's an existing completed/persisted occurrence, return its assigned participant
        persistedOccurrences[targetDateStr]?.let { occ ->
            return participants.find { it.id == occ.participantId }
        }

        // 2. If there's a manual override for this specific date, return it
        manualAssignments[targetDateStr]?.let { pId ->
            return participants.find { it.id == pId }
        }

        // 3. If the date is not a valid scheduled turn date, return null
        if (!isScheduledDate(schedule, targetDate, skipDates)) {
            return null
        }

        // 4. Deterministic progression calculation
        val patternList = buildEffectivePattern(participants, pattern)
        if (patternList.isEmpty()) return null

        val startDate = DateUtils.parse(schedule.startDate)
        if (targetDate.isBefore(startDate)) return null

        var currentIndex = 0
        var cursor = startDate

        // Walk through all calendar days from startDate to targetDate
        while (!cursor.isAfter(targetDate)) {
            val cursorStr = DateUtils.format(cursor)

            if (isScheduledDate(schedule, cursor, skipDates)) {
                val overrideId = manualAssignments[cursorStr]

                if (cursor == targetDate) {
                    if (overrideId != null) {
                        return participants.find { it.id == overrideId }
                    }
                    val currentP = patternList[currentIndex % patternList.size]
                    return participants.find { it.id == currentP.id }
                }

                // Advance index for subsequent days
                if (overrideId != null && schedule.overrideStrategy == OverrideStrategy.CONTINUE_FROM_OVERRIDE) {
                    val overridePos = patternList.indexOfFirst { it.id == overrideId }
                    if (overridePos >= 0) {
                        currentIndex = overridePos + 1
                    } else {
                        currentIndex++
                    }
                } else {
                    currentIndex++
                }
            }

            cursor = cursor.plusDays(1)
        }

        return null
    }

    /**
     * Resolves a complete ResolvedTurn for the target date, including status, amount, previous, and next turns.
     */
    fun resolveTurnForDate(
        schedule: Schedule,
        participants: List<Participant>,
        pattern: List<RotationPatternItem>,
        targetDate: LocalDate,
        manualAssignments: Map<String, Long> = emptyMap(),
        skipDates: Set<String> = emptySet(),
        persistedOccurrences: Map<String, TurnOccurrence> = emptyMap()
    ): ResolvedTurn? {
        val targetDateStr = DateUtils.format(targetDate)
        val participant = calculateParticipantForDate(
            schedule, participants, pattern, targetDate,
            manualAssignments, skipDates, persistedOccurrences
        ) ?: return null

        val persisted = persistedOccurrences[targetDateStr]
        val status = persisted?.status ?: run {
            val today = DateUtils.today()
            when {
                targetDate.isBefore(today) -> OccurrenceStatus.MISSED
                else -> OccurrenceStatus.PENDING
            }
        }

        val isManual = persisted?.isManualOverride ?: manualAssignments.containsKey(targetDateStr)
        val expectedAmount = persisted?.expectedAmount ?: schedule.defaultAmount
        val actualAmount = persisted?.actualAmount

        // Find previous turn
        val prev = findPreviousScheduledTurn(
            schedule, participants, pattern, targetDate,
            manualAssignments, skipDates, persistedOccurrences
        )

        // Find next turn
        val next = findNextScheduledTurn(
            schedule, participants, pattern, targetDate,
            manualAssignments, skipDates, persistedOccurrences
        )

        return ResolvedTurn(
            schedule = schedule,
            participant = participant,
            date = targetDateStr,
            status = status,
            occurrenceId = persisted?.id,
            isManualOverride = isManual,
            expectedAmount = expectedAmount,
            actualAmount = actualAmount,
            note = persisted?.note.orEmpty(),
            previousTurnParticipant = prev?.first,
            previousTurnDate = prev?.second?.let { DateUtils.format(it) },
            nextTurnParticipant = next?.first,
            nextTurnDate = next?.second?.let { DateUtils.format(it) }
        )
    }

    /**
     * Finds the nearest past scheduled turn date and its participant.
     */
    fun findPreviousScheduledTurn(
        schedule: Schedule,
        participants: List<Participant>,
        pattern: List<RotationPatternItem>,
        beforeDate: LocalDate,
        manualAssignments: Map<String, Long> = emptyMap(),
        skipDates: Set<String> = emptySet(),
        persistedOccurrences: Map<String, TurnOccurrence> = emptyMap()
    ): Pair<Participant, LocalDate>? {
        var cursor = beforeDate.minusDays(1)
        val startDate = DateUtils.parse(schedule.startDate)
        val limit = startDate.coerceAtLeast(beforeDate.minusMonths(3))

        while (!cursor.isBefore(limit)) {
            if (isScheduledDate(schedule, cursor, skipDates)) {
                val p = calculateParticipantForDate(
                    schedule, participants, pattern, cursor,
                    manualAssignments, skipDates, persistedOccurrences
                )
                if (p != null) return Pair(p, cursor)
            }
            cursor = cursor.minusDays(1)
        }
        return null
    }

    /**
     * Finds the nearest future scheduled turn date and its participant.
     */
    fun findNextScheduledTurn(
        schedule: Schedule,
        participants: List<Participant>,
        pattern: List<RotationPatternItem>,
        afterDate: LocalDate,
        manualAssignments: Map<String, Long> = emptyMap(),
        skipDates: Set<String> = emptySet(),
        persistedOccurrences: Map<String, TurnOccurrence> = emptyMap()
    ): Pair<Participant, LocalDate>? {
        var cursor = afterDate.plusDays(1)
        val limit = afterDate.plusYears(1)

        while (!cursor.isAfter(limit)) {
            if (isScheduledDate(schedule, cursor, skipDates)) {
                val p = calculateParticipantForDate(
                    schedule, participants, pattern, cursor,
                    manualAssignments, skipDates, persistedOccurrences
                )
                if (p != null) return Pair(p, cursor)
            }
            cursor = cursor.plusDays(1)
        }
        return null
    }

    /**
     * Generates upcoming turns for preview (e.g. next 7, 14, or 30 turns).
     */
    fun getUpcomingTurns(
        schedule: Schedule,
        participants: List<Participant>,
        pattern: List<RotationPatternItem>,
        fromDate: LocalDate,
        count: Int = 14,
        manualAssignments: Map<String, Long> = emptyMap(),
        skipDates: Set<String> = emptySet(),
        persistedOccurrences: Map<String, TurnOccurrence> = emptyMap()
    ): List<Pair<Participant, LocalDate>> {
        val result = mutableListOf<Pair<Participant, LocalDate>>()
        var cursor = fromDate
        val maxDays = 365

        var daysChecked = 0
        while (result.size < count && daysChecked < maxDays) {
            if (isScheduledDate(schedule, cursor, skipDates)) {
                val p = calculateParticipantForDate(
                    schedule, participants, pattern, cursor,
                    manualAssignments, skipDates, persistedOccurrences
                )
                if (p != null) {
                    result.add(Pair(p, cursor))
                }
            }
            cursor = cursor.plusDays(1)
            daysChecked++
        }
        return result
    }

    /**
     * Builds effective participant pattern. If pattern items are given, uses them;
     * otherwise defaults to sequential order of active participants.
     */
    fun buildEffectivePattern(
        participants: List<Participant>,
        pattern: List<RotationPatternItem>
    ): List<Participant> {
        val activeMap = participants.filter { it.isActive }.associateBy { it.id }
        if (pattern.isNotEmpty()) {
            val resolved = pattern.sortedBy { it.patternPosition }.mapNotNull { activeMap[it.participantId] }
            if (resolved.isNotEmpty()) return resolved
        }
        return participants.filter { it.isActive }
    }
}
