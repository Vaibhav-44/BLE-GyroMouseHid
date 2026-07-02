package com.example.gyromouse

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Foreground service that registers the phone as a Bluetooth HID mouse.
 */
@SuppressLint("MissingPermission")
class GyroMouseService : Service() {

    private val tag = "GyroMouseService"

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var hidDevice: BluetoothHidDevice? = null
    private lateinit var sensorProcessor: SensorProcessor

    private var hostDevice: BluetoothDevice? = null

    // -------------------------------------------------
    // SDP (Service Discovery Protocol) settings
    // -------------------------------------------------
    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "Gyro Mouse",                     // name shown on PC
        "Phone as Bluetooth Mouse",       // description
        "GyroMouseApp",                   // provider
        BluetoothHidDevice.SUBCLASS1_MOUSE,
        HidReportDescriptor.MOUSE_REPORT_DESCRIPTOR
    )

    // -------------------------------------------------
    // Service lifecycle
    // -------------------------------------------------
    override fun onCreate() {
        super.onCreate()

        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e(tag, "Bluetooth not supported")
            stopSelf()
            return
        }
        sensorProcessor = SensorProcessor(this)

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())

        sensorProcessor.start()

        // Get the HID profile proxy
        bluetoothAdapter.getProfileProxy(this, profileListener, BluetoothProfile.HID_DEVICE)
    }

    override fun onDestroy() {
        sensorProcessor.stop()
        if (hasConnectPermission()) {
            hidDevice?.unregisterApp()
        }
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -------------------------------------------------
    // BluetoothProfile.ServiceListener
    // -------------------------------------------------
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            hidDevice = null
        }
    }

    // -------------------------------------------------
    // Register the HID application
    // -------------------------------------------------
    private fun registerHidApp() {
        if (!hasConnectPermission()) {
            Log.e(tag, "Missing BLUETOOTH_CONNECT permission")
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        hidDevice?.registerApp(sdpSettings, null, null, executor, hidDeviceCallback)
            ?: Log.e(tag, "hidDevice is null")
    }

    // -------------------------------------------------
    // BluetoothHidDevice.Callback – ALL ABSTRACT METHODS
    // -------------------------------------------------
    private val hidDeviceCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(tag, "App registered = $registered")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            hostDevice = if (state == BluetoothProfile.STATE_CONNECTED) device else null
            Log.d(tag, "Connection state = $state, device = $device")
            if (state == BluetoothProfile.STATE_CONNECTED) startSendingReports()
        }

        override fun onGetReport(
            device: BluetoothDevice,
            type: Byte,
            id: Byte,
            bufferSize: Int
        ) {
            // we never provide reports on request
        }

        override fun onSetReport(
            device: BluetoothDevice,
            type: Byte,
            id: Byte,
            data: ByteArray
        ) {
            // host never writes to us
        }

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
            // ignore
        }

        override fun onInterruptData(device: BluetoothDevice, reportId: Byte, data: ByteArray) {
            // not used
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            hostDevice = null
        }
    }

    // -------------------------------------------------
    // Send sensor reports when connected
    // -------------------------------------------------
    private fun startSendingReports() {
        CoroutineScope(Dispatchers.IO).launch {
            sensorProcessor.mouseReports.collect { report ->
                val dev = hostDevice ?: return@collect
                if (hasConnectPermission()) {
                    hidDevice?.sendReport(dev, REPORT_ID_MOUSE, report)
                }
            }
        }
    }

    // -------------------------------------------------
    // Permission helper (API 31+)
    // -------------------------------------------------
    private fun hasConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true          // on API < 31 the old BLUETOOTH permission is enough
        }

    // -------------------------------------------------
    // Foreground notification
    // -------------------------------------------------
    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            "Gyro Mouse Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gyro Mouse")
            .setContentText("Move phone to control cursor")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

    // -------------------------------------------------
    // Constants
    // -------------------------------------------------
    companion object {
        private const val CHANNEL_ID = "hid_service"
        private const val NOTIF_ID = 1
        private const val REPORT_ID_MOUSE = 1
    }
}