package com.example.gyromouse

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.GestureDetector
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class SensorProcessor(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var gyroX = 0f; private var gyroY = 0f; private var gyroZ = 0f
    private var accelX = 0f; private var accelY = 0f; private var accelZ = 0f
    private var mouseX = 0f; private var mouseY = 0f  // Relative position
    private val alpha = 0.98f  // Complementary filter (gyro-heavy for low latency)

    // Flows for reports (use in Service)
    private val _mouseReports = MutableSharedFlow<ByteArray>(extraBufferCapacity = 1)
    val mouseReports = _mouseReports.asSharedFlow()

    // Gesture detector for touch
    private lateinit var gestureDetector: GestureDetector

    init {
        CoroutineScope(Dispatchers.Default).launch {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                    // Swipe: Send wheel or drag
                    val wheel = if (velocityY > 1000) -1 else if (velocityY < -1000) 1 else 0
                    _mouseReports.tryEmit(HidReportBuilder.buildMouseReport(0, 0, 0, wheel))
                    return true
                }
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Tap: Left click
                    _mouseReports.tryEmit(HidReportBuilder.buildMouseReport(1, 0, 0, 0))  // Button 1
                    _mouseReports.tryEmit(HidReportBuilder.buildMouseReport(0, 0, 0, 0))  // Release
                    return true
                }
                // Add onLongPress for right-click, etc.
            })
        }
    }

    fun start() {
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (gyro != null) {
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)  // 100Hz
        }
        if (accel != null) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Not yet implemented
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
            }
        }
        updateMousePosition()
    }

    private fun updateMousePosition() {
        // Complementary filter: angle = alpha * (angle + gyro * dt) + (1 - alpha) * accel_angle
        // dt ~ 10ms at 100Hz; simplify to deltas
        val dt = 0.01f  // Approximate
        val accelMagnitude = sqrt(accelX*accelX + accelY*accelY + accelZ*accelZ)
        if (accelMagnitude > 10) return  // Ignore if accelerating hard (gesture?)

        // Gyro to relative movement (sensitivity tunable)
        val sensitivity = 0.5f  // Pixels per deg/s
        mouseX = mouseX * 0.8f + (gyroY * sensitivity * dt) * 0.2f
        mouseY = mouseY * 0.8f + (-gyroX * sensitivity * dt) * 0.2f

        // Detect gyro gestures (e.g., quick shake for swipe)
        if (gyroZ.absoluteValue > 5f) {  // Z-rotation threshold for horizontal swipe
            _mouseReports.tryEmit(HidReportBuilder.buildMouseReport(0, 0f, 0f, if (gyroZ > 0) 1 else -1))
        } else {
            _mouseReports.tryEmit(HidReportBuilder.buildMouseReport(0, mouseX, mouseY, 0))
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event)

    fun stop() = sensorManager.flush(this)
}