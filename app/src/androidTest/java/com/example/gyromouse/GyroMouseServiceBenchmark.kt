package com.example.gyromouse

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime

@RunWith(AndroidJUnit4::class)
class GyroMouseServiceBenchmark {

    @Test
    fun benchmarkPermissionCheck() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Warmup
        for (i in 0..1000) {
            hasConnectPermission(context)
        }

        val iterations = 100000
        val time = measureNanoTime {
            for (i in 0..iterations) {
                hasConnectPermission(context)
            }
        }

        println("Benchmark: Permission check took ${time / iterations} ns per call")
    }

    private fun hasConnectPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
}
