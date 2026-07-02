import kotlin.system.measureTimeMillis
import kotlin.random.Random

object HidReportBuilder {
    fun buildMouseReport(buttons: Int, dx: Float, dy: Float, wheel: Int): ByteArray {
        return byteArrayOf(buttons.toByte(), dx.toInt().toByte(), dy.toInt().toByte(), wheel.toByte())
    }

    private val report = ByteArray(4)
    fun buildMouseReportOptimized(buttons: Int, dx: Float, dy: Float, wheel: Int): ByteArray {
        report[0] = buttons.toByte()
        report[1] = dx.toInt().toByte()
        report[2] = dy.toInt().toByte()
        report[3] = wheel.toByte()
        return report
    }
}

fun main() {
    println("Warming up...")
    var dummy = 0
    for (i in 1..1000000) {
        val r = HidReportBuilder.buildMouseReport(1, Random.nextFloat(), Random.nextFloat(), 1)
        dummy += r[0]
        val r2 = HidReportBuilder.buildMouseReportOptimized(1, Random.nextFloat(), Random.nextFloat(), 1)
        dummy += r2[0]
    }

    val iters = 50000000

    // Test original
    System.gc()
    val t1 = measureTimeMillis {
        for (i in 1..iters) {
            val r = HidReportBuilder.buildMouseReport(1, 10f, 10f, 1)
            dummy += r[0]
        }
    }
    println("Original: ${t1}ms")

    // Test optimized
    System.gc()
    val t2 = measureTimeMillis {
        for (i in 1..iters) {
            val r = HidReportBuilder.buildMouseReportOptimized(1, 10f, 10f, 1)
            dummy += r[0]
        }
    }
    println("Optimized: ${t2}ms")
    println(dummy)
}
