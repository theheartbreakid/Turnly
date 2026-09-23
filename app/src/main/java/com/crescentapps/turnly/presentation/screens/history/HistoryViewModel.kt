package com.crescentapps.turnly.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.Participant
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.model.TurnOccurrence
import com.crescentapps.turnly.data.repository.TurnlyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryItem(
    val occurrence: TurnOccurrence,
    val scheduleName: String,
    val participantName: String,
    val participantColorHex: String,
    val currencyCode: String
)

data class HistoryUiState(
    val allItems: List<HistoryItem> = emptyList(),
    val filteredItems: List<HistoryItem> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val participants: List<Participant> = emptyList(),
    val selectedScheduleId: Long? = null,
    val selectedParticipantId: Long? = null,
    val selectedStatus: OccurrenceStatus? = null,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val missedCount: Int = 0,
    val totalRecordedAmount: Double = 0.0,
    val totalExpectedAmount: Double = 0.0,
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val repository: TurnlyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                repository.allOccurrencesFlow,
                repository.allSchedules,
                repository.allParticipants
            ) { occurrences, schedules, participants ->
                val sMap = schedules.associateBy { it.id }
                val pMap = participants.associateBy { it.id }

                val items = occurrences.map { occ ->
                    val s = sMap[occ.scheduleId]
                    val p = pMap[occ.participantId]
                    HistoryItem(
                        occurrence = occ,
                        scheduleName = s?.name ?: "Unknown Schedule",
                        participantName = p?.name ?: "Unknown Person",
                        participantColorHex = p?.colorHex ?: "#3B82F6",
                        currencyCode = s?.currencyCode ?: "INR"
                    )
                }
                Triple(items, schedules, participants)
            }.collect { (items, schedules, participants) ->
                _uiState.update { state ->
                    state.copy(
                        allItems = items,
                        schedules = schedules,
                        participants = participants,
                        isLoading = false
                    )
                }
                applyFilters()
            }
        }
    }

    fun filterBySchedule(scheduleId: Long?) {
        _uiState.update { it.copy(selectedScheduleId = scheduleId) }
        applyFilters()
    }

    fun filterByParticipant(participantId: Long?) {
        _uiState.update { it.copy(selectedParticipantId = participantId) }
        applyFilters()
    }

    fun filterByStatus(status: OccurrenceStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.allItems.filter { item ->
            val matchSchedule = state.selectedScheduleId == null || item.occurrence.scheduleId == state.selectedScheduleId
            val matchParticipant = state.selectedParticipantId == null || item.occurrence.participantId == state.selectedParticipantId
            val matchStatus = state.selectedStatus == null || item.occurrence.status == state.selectedStatus
            matchSchedule && matchParticipant && matchStatus
        }

        val completed = filtered.count { it.occurrence.status == OccurrenceStatus.COMPLETED }
        val skipped = filtered.count { it.occurrence.status == OccurrenceStatus.SKIPPED }
        val missed = filtered.count { it.occurrence.status == OccurrenceStatus.MISSED }
        val recordedAmount = filtered.filter { it.occurrence.status == OccurrenceStatus.COMPLETED }
            .sumOf { it.occurrence.actualAmount ?: it.occurrence.expectedAmount ?: 0.0 }
        val expectedAmount = filtered.sumOf { it.occurrence.expectedAmount ?: 0.0 }

        _uiState.update {
            it.copy(
                filteredItems = filtered,
                totalCount = filtered.size,
                completedCount = completed,
                skippedCount = skipped,
                missedCount = missed,
                totalRecordedAmount = recordedAmount,
                totalExpectedAmount = expectedAmount
            )
        }
    }
}
