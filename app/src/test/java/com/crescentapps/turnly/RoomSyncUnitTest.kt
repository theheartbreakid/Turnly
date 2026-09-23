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

    @Test
    fun testExactRoomCodeExtraction() {
        val expectedCode = "X7K9P2"
        val payload = RoomInvitePayload(
            version = 1,
            roomId = "room_9999",
            roomCode = expectedCode,
            roomName = "Apartment 4B",
            ownerName = "Mohsin",
            inviteToken = "tok_xyz"
        )

        // 1. Deep link round-trip
        val deepLink = QRCodeUtils.createInviteDeepLink(payload)
        val extractedFromDeepLink = QRCodeUtils.extractRoomCode(deepLink)
        assertEquals(expectedCode, extractedFromDeepLink)

        // 2. Direct code entry / raw QR string
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("X7K9P2"))
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("x7k9p2"))
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("  X7K9P2  "))
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode(" x7 k9 p2 "))

        // 3. Query param format
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("https://turnly.app/join?code=X7K9P2"))
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("turnly://room?code=x7k9p2"))

        // 4. HTTP link format with base64
        val httpLink = "https://turnly.app/room/" + deepLink.removePrefix("turnly://room/")
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode(httpLink))

        // 5. Direct path format
        assertEquals(expectedCode, QRCodeUtils.extractRoomCode("turnly://room/X7K9P2"))
    }
}
