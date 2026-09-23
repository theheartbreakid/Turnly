package com.crescentapps.turnly.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.repository.TurnlyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HomeFilter {
    ALL,
    LOCAL,
    SHARED
}

data class HomeUiState(
    val todayTurns: List<ResolvedTurn> = emptyList(),
    val filteredTurns: List<ResolvedTurn> = emptyList(),
    val currentFilter: HomeFilter = HomeFilter.ALL,
    val totalPending: Int = 0,
    val totalCompleted: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: TurnlyRepository,
    private val roomRepository: com.crescentapps.turnly.data.repository.RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTurns()
    }

    private fun loadTurns() {
        viewModelScope.launch {
            repository.getTodayTurnsFlow(DateUtils.today())
                .collect { turns ->
                    val pending = turns.count { it.status == OccurrenceStatus.PENDING }
                    val completed = turns.count { it.status == OccurrenceStatus.COMPLETED }
                    _uiState.update {
                        it.copy(
                            todayTurns = turns,
                            filteredTurns = applyFilter(turns, it.currentFilter),
                            totalPending = pending,
                            totalCompleted = completed,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setFilter(filter: HomeFilter) {
        _uiState.update {
            it.copy(
                currentFilter = filter,
                filteredTurns = applyFilter(it.todayTurns, filter)
            )
        }
    }

    private fun applyFilter(turns: List<ResolvedTurn>, filter: HomeFilter): List<ResolvedTurn> {
        return when (filter) {
            HomeFilter.ALL -> turns
            HomeFilter.LOCAL -> turns.filter { !it.isShared }
            HomeFilter.SHARED -> turns.filter { it.isShared }
        }
    }

    fun markTurnComplete(turn: ResolvedTurn, actualAmount: Double? = null) {
        viewModelScope.launch {
            if (turn.isShared) {
                roomRepository.markTurnCompleteSynchronized(
                    scheduleId = turn.schedule.id,
                    date = turn.date,
                    participantId = turn.participant.id,
                    actualAmount = actualAmount ?: turn.expectedAmount
                )
            } else {
                repository.markTurnComplete(
                    scheduleId = turn.schedule.id,
                    date = turn.date,
                    participantId = turn.participant.id,
                    actualAmount = actualAmount ?: turn.expectedAmount
                )
            }
        }
    }

    fun undoCompletion(turn: ResolvedTurn) {
        viewModelScope.launch {
            repository.undoTurnCompletion(turn.schedule.id, turn.date)
        }
    }
}
