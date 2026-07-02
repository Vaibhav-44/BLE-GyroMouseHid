package com.example.gyromouse

object HidReportBuilder {

    private val poolSize = 16
    private val reports = Array(poolSize) { ByteArray(4) }
    private var index = 0

    @Synchronized
    fun buildMouseReport(buttons: Int, dx: Float, dy: Float, wheel: Int): ByteArray {
        // Mouse report: 4 bytes
        // Byte 0: Buttons (e.g., 1 for left, 2 for right)
        // Byte 1: dX (relative X movement)
        // Byte 2: dY (relative Y movement)
        // Byte 3: Wheel
        val report = reports[index]
        index = (index + 1) % poolSize

        report[0] = buttons.toByte()
        report[1] = dx.toInt().toByte()
        report[2] = dy.toInt().toByte()
        report[3] = wheel.toByte()

        return report
    }
}
