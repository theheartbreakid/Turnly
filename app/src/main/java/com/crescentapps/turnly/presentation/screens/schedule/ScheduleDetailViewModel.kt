package com.crescentapps.turnly.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.domain.engine.RotationEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleDetailUiState(
    val schedule: Schedule? = null,
    val participants: List<Participant> = emptyList(),
    val pattern: List<RotationPatternItem> = emptyList(),
    val upcomingTurns: List<Pair<Participant, LocalDate>> = emptyList(),
    val totalTurnsCount: Int = 0,
    val completedCount: Int = 0,
    val turnsPerParticipant: Map<String, Int> = emptyMap(),
    val totalRecordedAmount: Double = 0.0,
    val isLoading: Boolean = true
)

class ScheduleDetailViewModel(
    private val repository: TurnlyRepository,
    private val scheduleId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleDetailUiState())
    val uiState: StateFlow<ScheduleDetailUiState> = _uiState.asStateFlow()

    init {
        loadScheduleDetails()
    }

    private fun loadScheduleDetails() {
        viewModelScope.launch {
            combine(
                repository.getScheduleFlow(scheduleId),
                repository.getParticipantsForScheduleFlow(scheduleId),
                repository.getRotationPatternFlow(scheduleId),
                repository.getOccurrencesForScheduleFlow(scheduleId)
            ) { schedule, participants, pattern, occurrences ->
                val skips = repository.getSkipDatesFlow(scheduleId).first()
                val manual = repository.getManualAssignmentsFlow(scheduleId).first()

                if (schedule == null) return@combine null

                val skipSet = skips.map { it.date }.toSet()
                val manualMap = manual.associate { it.date to it.participantId }
                val occMap = occurrences.associateBy { it.date }

                val upcoming = RotationEngine.getUpcomingTurns(
                    schedule = schedule,
                    participants = participants,
                    pattern = pattern,
                    fromDate = LocalDate.now(),
                    count = 14,
                    manualAssignments = manualMap,
                    skipDates = skipSet,
                    persistedOccurrences = occMap
                )

                val completed = occurrences.count { it.status == OccurrenceStatus.COMPLETED }
                val counts = occurrences.groupBy { it.participantId }
                    .mapNotNull { (pId, list) ->
                        val p = participants.find { it.id == pId }
                        if (p != null) p.name to list.size else null
                    }.toMap()

                val recorded = occurrences.filter { it.status == OccurrenceStatus.COMPLETED }
                    .sumOf { it.actualAmount ?: it.expectedAmount ?: 0.0 }

                ScheduleDetailUiState(
                    schedule = schedule,
                    participants = participants,
                    pattern = pattern,
                    upcomingTurns = upcoming,
                    totalTurnsCount = occurrences.size,
                    completedCount = completed,
                    turnsPerParticipant = counts,
                    totalRecordedAmount = recorded,
                    isLoading = false
                )
            }.filterNotNull().collect { state ->
                _uiState.value = state
            }
        }
    }

    fun togglePauseSchedule() {
        val schedule = _uiState.value.schedule ?: return
        viewModelScope.launch {
            repository.setSchedulePauseState(schedule.id, !schedule.isPaused, null)
        }
    }

    fun archiveSchedule() {
        viewModelScope.launch {
            repository.archiveSchedule(scheduleId)
        }
    }

    fun deletePermanently() {
        viewModelScope.launch {
            repository.deleteSchedulePermanently(scheduleId)
        }
    }
}
