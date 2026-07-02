package com.example.gyromouse

import org.junit.Test
import kotlin.math.sqrt
import kotlin.system.measureNanoTime

class SensorProcessorBenchmarkTest {

    @Test
    fun benchmarkSqrtVsSquared() {
        val iterations = 10_000_000
        val accelX = 5.0f
        val accelY = 6.0f
        val accelZ = 7.0f

        // Warmup
        var dummy = 0.0f
        var dummyBool = false
        for (i in 0..10000) {
            val accelMagnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
            if (accelMagnitude > 10) dummy += 1

            val accelMagnitudeSquared = accelX * accelX + accelY * accelY + accelZ * accelZ
            if (accelMagnitudeSquared > 100) dummyBool = !dummyBool
        }

        // Measure sqrt
        val timeSqrt = measureNanoTime {
            for (i in 0..iterations) {
                val accelMagnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
                if (accelMagnitude > 10) dummy += 1
            }
        }

        // Measure squared
        val timeSquared = measureNanoTime {
            for (i in 0..iterations) {
                val accelMagnitudeSquared = accelX * accelX + accelY * accelY + accelZ * accelZ
                if (accelMagnitudeSquared > 100) dummyBool = !dummyBool
            }
        }

        println("Benchmark Results for " + iterations + " iterations:")
        println("sqrt() method: " + (timeSqrt / 1_000_000.0) + " ms")
        println("squared method: " + (timeSquared / 1_000_000.0) + " ms")
        println("Improvement: " + ((timeSqrt - timeSquared) / timeSqrt.toDouble() * 100) + "%")
    }
}
