package com.crescentapps.turnly

import com.crescentapps.turnly.core.model.RoomInvitePayload
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.core.util.RoomCodeGenerator
import org.junit.Assert.*
import org.junit.Test

class RoomSyncUnitTest {

    @Test
    fun testRoomCodeGeneration() {
        val code = RoomCodeGenerator.generateCode()
        assertEquals(6, code.length)
        assertTrue(RoomCodeGenerator.isValidCode(code))

        // Check that confusing characters are never present
        assertFalse(code.contains("0"))
        assertFalse(code.contains("O"))
        assertFalse(code.contains("1"))
        assertFalse(code.contains("I"))
        assertFalse(code.contains("5"))
        assertFalse(code.contains("S"))
    }

    @Test
    fun testRoomCodeNormalization() {
        assertEquals("X7K9P2", RoomCodeGenerator.normalizeCode("x7k9p2"))
        assertEquals("X7K9P2", RoomCodeGenerator.normalizeCode(" x7 k9 p2 "))
    }

    @Test
    fun testQRCodeDeepLinkSerialization() {
        val payload = RoomInvitePayload(
            version = 1,
            roomId = "room_12345",
            roomCode = "X7K9P2",
            roomName = "Family Turns",
            ownerName = "Dad",
            inviteToken = "token_abc"
        )

        val link = QRCodeUtils.createInviteDeepLink(payload)
        assertTrue(link.startsWith("turnly://room/"))

        val parsed = QRCodeUtils.parseInvitePayload(link)
        assertNotNull(parsed)
        assertEquals(payload.roomId, parsed?.roomId)
        assertEquals(payload.roomCode, parsed?.roomCode)
        assertEquals(payload.roomName, parsed?.roomName)
        assertEquals(payload.ownerName, parsed?.ownerName)
    }
}
