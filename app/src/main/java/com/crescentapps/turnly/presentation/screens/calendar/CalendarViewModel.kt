package com.crescentapps.turnly.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.Participant
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.domain.engine.RotationEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayTurnIndicator(
    val scheduleId: Long,
    val scheduleName: String,
    val participantName: String,
    val participantColorHex: String,
    val status: OccurrenceStatus
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dayTurnMap: Map<LocalDate, List<DayTurnIndicator>> = emptyMap(),
    val selectedDayTurns: List<ResolvedTurn> = emptyList(),
    val availableParticipants: List<Participant> = emptyList(),
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val repository: TurnlyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthTurns(_uiState.value.currentMonth)
        observeParticipants()
    }

    private fun observeParticipants() {
        viewModelScope.launch {
            repository.allActiveParticipants.collect { pList ->
                _uiState.update { it.copy(availableParticipants = pList) }
            }
        }
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = next) }
        loadMonthTurns(next)
    }

    fun prevMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = prev) }
        loadMonthTurns(prev)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        _uiState.update { it.copy(currentMonth = currentMonth, selectedDate = today) }
        loadMonthTurns(currentMonth)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        resolveSelectedDayTurns(date)
    }

    private var cachedContext: TurnlyRepository.MonthScheduleContext? = null

    private fun loadMonthTurns(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val startDateStr = DateUtils.format(month.atDay(1))
            val endDateStr = DateUtils.format(month.atEndOfMonth())

            val context = repository.getMonthScheduleContext(startDateStr, endDateStr)
            cachedContext = context

            val dayMap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val map = mutableMapOf<LocalDate, MutableList<DayTurnIndicator>>()
                val daysInMonth = month.lengthOfMonth()
                val today = LocalDate.now()

                for (day in 1..daysInMonth) {
                    val date = month.atDay(day)
                    val dateStr = DateUtils.format(date)

                    for (schedule in context.schedules) {
                        val participants = context.participantsBySchedule[schedule.id].orEmpty()
                        val pattern = context.patternBySchedule[schedule.id].orEmpty()
                        val skips = context.skipsBySchedule[schedule.id].orEmpty()
                        val manual = context.manualBySchedule[schedule.id].orEmpty()
                        val scheduleOccs = context.occurrencesBySchedule[schedule.id].orEmpty()

                        if (RotationEngine.isScheduledDate(schedule, date, skips)) {
                            val p = RotationEngine.calculateParticipantForDate(
                                schedule, participants, pattern, date, manual, skips, scheduleOccs
                            )
                            if (p != null) {
                                val status = scheduleOccs[dateStr]?.status ?: run {
                                    if (date.isBefore(today)) OccurrenceStatus.MISSED else OccurrenceStatus.PENDING
                                }
                                map.getOrPut(date) { mutableListOf() }.add(
                                    DayTurnIndicator(
                                        scheduleId = schedule.id,
                                        scheduleName = schedule.name,
                                        participantName = p.name,
                                        participantColorHex = p.colorHex,
                                        status = status
                                    )
                                )
                            }
                        }
                    }
                }
                map
            }

            _uiState.update { it.copy(dayTurnMap = dayMap, isLoading = false) }
            resolveSelectedDayTurns(_uiState.value.selectedDate)
        }
    }

    private fun resolveSelectedDayTurns(date: LocalDate) {
        val context = cachedContext
        if (context == null) {
            viewModelScope.launch {
                val start = DateUtils.format(date.withDayOfMonth(1))
                val end = DateUtils.format(date.withDayOfMonth(date.lengthOfMonth()))
                val ctx = repository.getMonthScheduleContext(start, end)
                cachedContext = ctx
                computeSelectedDayTurns(date, ctx)
            }
        } else {
            computeSelectedDayTurns(date, context)
        }
    }

    private fun computeSelectedDayTurns(date: LocalDate, context: TurnlyRepository.MonthScheduleContext) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val resolvedList = mutableListOf<ResolvedTurn>()
            for (schedule in context.schedules) {
                val participants = context.participantsBySchedule[schedule.id].orEmpty()
                val pattern = context.patternBySchedule[schedule.id].orEmpty()
                val skips = context.skipsBySchedule[schedule.id].orEmpty()
                val manual = context.manualBySchedule[schedule.id].orEmpty()
                val scheduleOccs = context.occurrencesBySchedule[schedule.id].orEmpty()

                RotationEngine.resolveTurnForDate(
                    schedule, participants, pattern, date, manual, skips, scheduleOccs
                )?.let { resolvedList.add(it) }
            }
            _uiState.update { it.copy(selectedDayTurns = resolvedList) }
        }
    }

    fun markTurnComplete(turn: ResolvedTurn, actualAmount: Double? = null) {
        viewModelScope.launch {
            repository.markTurnComplete(
                scheduleId = turn.schedule.id,
                date = turn.date,
                participantId = turn.participant.id,
                actualAmount = actualAmount ?: turn.expectedAmount
            )
            loadMonthTurns(_uiState.value.currentMonth)
        }
    }

    fun setManualOverride(scheduleId: Long, date: String, participantId: Long) {
        viewModelScope.launch {
            repository.setManualOverride(scheduleId, date, participantId, "User manual assignment")
            loadMonthTurns(_uiState.value.currentMonth)
        }
    }

    fun skipDate(scheduleId: Long, date: String) {
        viewModelScope.launch {
            repository.addSkipDate(scheduleId, date, "User skipped date")
            loadMonthTurns(_uiState.value.currentMonth)
        }
    }
}
