package com.crescentapps.turnly.data.network

import com.crescentapps.turnly.core.model.MemberPresence
import com.crescentapps.turnly.core.model.SyncEvent
import kotlinx.coroutines.flow.Flow

interface RealtimeSyncClient {
    val connectionState: Flow<Boolean>
    val incomingEvents: Flow<SyncEvent>

    suspend fun connect(roomId: String, memberId: String, displayName: String)
    suspend fun disconnect()
    suspend fun sendEvent(event: SyncEvent): Boolean
    suspend fun updatePresence(presence: MemberPresence)
}
