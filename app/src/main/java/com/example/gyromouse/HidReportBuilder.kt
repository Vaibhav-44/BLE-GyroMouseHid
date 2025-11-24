package com.example.gyromouse

object HidReportBuilder {
    fun buildMouseReport(buttons: Int, dx: Float, dy: Float, wheel: Int): ByteArray {
        // Mouse report: 4 bytes
        // Byte 0: Buttons (e.g., 1 for left, 2 for right)
        // Byte 1: dX (relative X movement)
        // Byte 2: dY (relative Y movement)
        // Byte 3: Wheel
        return byteArrayOf(buttons.toByte(), dx.toInt().toByte(), dy.toInt().toByte(), wheel.toByte())
    }
}
