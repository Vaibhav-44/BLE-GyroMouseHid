package com.example.gyromouse

import org.junit.Test
import kotlin.system.measureNanoTime

class GyroMouseServiceBenchmarkTest {
    @Test
    fun testBenchmark() {
        // Without an android device, we can just do a very basic check comparing an boolean vs a method call
        // to show the relative improvement in the JVM
        val iterations = 10000000

        // Baseline: simulated permission check function
        var result1 = false
        val baselineTime = measureNanoTime {
            for (i in 0..iterations) {
                result1 = mockPermissionCheck()
            }
        }

        // Optimized: cached boolean
        var result2 = false
        val cachedPermission = mockPermissionCheck()
        val optimizedTime = measureNanoTime {
            for (i in 0..iterations) {
                result2 = cachedPermission
            }
        }

        println("Baseline: ${baselineTime / 1_000_000.0} ms")
        println("Optimized: ${optimizedTime / 1_000_000.0} ms")
        println("Speedup: ${baselineTime.toDouble() / optimizedTime}x")
    }

    private fun mockPermissionCheck(): Boolean {
        // Simulate a tiny bit of work for a method call compared to a direct boolean access
        val version = 33
        if (version >= 31) {
             return true
        } else {
             return true
        }
    }
}
