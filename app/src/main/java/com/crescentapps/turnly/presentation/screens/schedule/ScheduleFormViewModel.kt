package com.crescentapps.turnly.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.presentation.theme.LiquidColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ParticipantInput(
    val id: Long = 0,
    val name: String,
    val nickname: String? = null,
    val colorHex: String = "#3B82F6"
)

data class CreateScheduleUiState(
    val currentStep: Int = 1,
    // Step 1: Basic Info
    val name: String = "",
    val description: String = "",
    val type: ScheduleType = ScheduleType.GENERAL,
    val currencyCode: String = "INR",
    val defaultAmount: String = "",
    val startDate: String = DateUtils.todayString(),
    // Step 2: People
    val participants: List<ParticipantInput> = emptyList(),
    // Step 3: Rotation & Frequency
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int = 1,
    val selectedWeekdays: List<Int> = listOf(1, 3, 5), // Mon, Wed, Fri by default for custom
    val dayOfMonth: Int = 1,
    val isCustomPattern: Boolean = false,
    val customPatternParticipantIds: List<Long> = emptyList(),
    val overrideStrategy: OverrideStrategy = OverrideStrategy.SINGLE_DATE_ONLY,
    val shareWithRoom: Boolean = false,
    val selectedRoomId: Long? = null,
    val availableRooms: List<P2PRoom> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false
)

class ScheduleFormViewModel(
    private val repository: TurnlyRepository,
    private val roomRepository: com.crescentapps.turnly.data.repository.RoomRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    init {
        roomRepository?.let { roomRepo ->
            viewModelScope.launch {
                roomRepo.allRooms.collect { rooms ->
                    _uiState.update { it.copy(availableRooms = rooms, selectedRoomId = it.selectedRoomId ?: rooms.firstOrNull()?.id) }
                }
            }
        }
    }

    fun updateShareWithRoom(enabled: Boolean) = _uiState.update { it.copy(shareWithRoom = enabled) }
    fun updateSelectedRoomId(roomId: Long) = _uiState.update { it.copy(selectedRoomId = roomId) }

    fun updateName(name: String) = _uiState.update { it.copy(name = name, error = null) }
    fun updateDescription(desc: String) = _uiState.update { it.copy(description = desc) }
    fun updateType(type: ScheduleType) = _uiState.update { it.copy(type = type) }
    fun updateCurrency(currency: String) = _uiState.update { it.copy(currencyCode = currency) }
    fun updateDefaultAmount(amount: String) = _uiState.update { it.copy(defaultAmount = amount) }
    fun updateStartDate(date: String) = _uiState.update { it.copy(startDate = date) }

    fun addParticipant(name: String, nickname: String?, colorHex: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Participant name cannot be empty") }
            return
        }
        val current = _uiState.value.participants
        if (current.any { it.name.trim().equals(name.trim(), ignoreCase = true) }) {
            _uiState.update { it.copy(error = "Participant with this name already exists") }
            return
        }
        val nextColor = colorHex.ifBlank {
            LiquidColors.ParticipantPalette[current.size % LiquidColors.ParticipantPalette.size]
        }
        val newP = ParticipantInput(name = name.trim(), nickname = nickname?.trim(), colorHex = nextColor)
        _uiState.update { it.copy(participants = current + newP, error = null) }
    }

    fun removeParticipant(index: Int) {
        val current = _uiState.value.participants.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(participants = current) }
        }
    }

    fun updateFrequencyType(type: FrequencyType) = _uiState.update { it.copy(frequencyType = type) }
    fun updateFrequencyInterval(interval: Int) = _uiState.update { it.copy(frequencyInterval = interval.coerceAtLeast(1)) }
    fun toggleWeekday(dayOfWeek: Int) {
        val current = _uiState.value.selectedWeekdays.toMutableList()
        if (current.contains(dayOfWeek)) {
            if (current.size > 1) current.remove(dayOfWeek)
        } else {
            current.add(dayOfWeek)
            current.sort()
        }
        _uiState.update { it.copy(selectedWeekdays = current) }
    }

    fun updateDayOfMonth(day: Int) = _uiState.update { it.copy(dayOfMonth = day.coerceIn(1, 31)) }
    fun updateOverrideStrategy(strategy: OverrideStrategy) = _uiState.update { it.copy(overrideStrategy = strategy) }

    fun nextStep(): Boolean {
        val state = _uiState.value
        when (state.currentStep) {
            1 -> {
                if (state.name.isBlank()) {
                    _uiState.update { it.copy(error = "Schedule name is required") }
                    return false
                }
                _uiState.update { it.copy(currentStep = 2, error = null) }
                return true
            }
            2 -> {
                if (state.participants.size < 2) {
                    _uiState.update { it.copy(error = "Add at least 2 participants") }
                    return false
                }
                _uiState.update { it.copy(currentStep = 3, error = null) }
                return true
            }
        }
        return false
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1), error = null) }
    }

    fun saveSchedule() {
        val state = _uiState.value
        if (state.participants.size < 2) {
            _uiState.update { it.copy(error = "Add at least 2 participants") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                // 1. Save or retrieve participants
                val participantIds = mutableListOf<Long>()
                for (pInput in state.participants) {
                    val pDomain = Participant(
                        name = pInput.name,
                        nickname = pInput.nickname,
                        colorHex = pInput.colorHex
                    )
                    val id = repository.saveParticipant(pDomain)
                    participantIds.add(id)
                }

                // 2. Build Schedule entity
                val schedule = Schedule(
                    name = state.name.trim(),
                    description = state.description.trim(),
                    type = state.type,
                    currencyCode = state.currencyCode,
                    defaultAmount = state.defaultAmount.toDoubleOrNull(),
                    frequencyType = state.frequencyType,
                    frequencyInterval = state.frequencyInterval,
                    selectedWeekdays = state.selectedWeekdays,
                    dayOfMonth = state.dayOfMonth,
                    startDate = state.startDate,
                    overrideStrategy = state.overrideStrategy
                )

                // 3. Save Schedule with participants and sequential pattern
                val newScheduleId = repository.saveScheduleWithDetails(
                    schedule = schedule,
                    participantIds = participantIds,
                    patternParticipantIds = participantIds
                )

                // 4. Optionally share with selected P2P Room
                if (state.shareWithRoom && state.selectedRoomId != null && roomRepository != null) {
                    roomRepository.shareScheduleWithRoom(
                        roomId = state.selectedRoomId,
                        localScheduleId = newScheduleId,
                        shareHistory = false
                    )
                }

                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage ?: "Failed to save schedule") }
            }
        }
    }
}
