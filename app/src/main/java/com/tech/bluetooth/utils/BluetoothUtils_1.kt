package com.tech.bluetooth.utils

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.tech.bluetooth.modal.BleDevice
import com.tech.bluetooth.utils.CommonUtils.BLUETOOTH_PERMISSION_DESC
import com.tech.bluetooth.utils.CommonUtils.NON_PAIRED
import java.lang.ref.WeakReference


class BluetoothUtils_1 private constructor(context: Activity) {

    private lateinit var bluetoothManager:BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var resultCallback: ResultCallback? = null
    private val contextRef: WeakReference<Activity> = WeakReference(context)

    interface ResultCallback {
        fun onResult(result: BleDevice)
    }
    companion object {
        @Volatile
        private var instance: BluetoothUtils_1? = null
        fun getInstance(context: Activity): BluetoothUtils_1 {
            return instance ?: synchronized(this) {
                instance ?: BluetoothUtils_1(context).also { instance = it }
            }
        }

    }

    fun getContext(): Activity? {
        return contextRef.get()
    }
    fun init() {
        getContext()?.let {
            bluetoothManager=it.getSystemService(BluetoothManager::class.java)
            if (Build.VERSION.SDK_INT >= 31) {
                bluetoothAdapter=bluetoothManager.adapter
            } else {
                @Suppress("deprecation")
                bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
            }
        }
    }

    public fun getPairedDeviceList() : MutableList<BleDevice> {
        var devicesList = mutableListOf<BleDevice>()
        devicesList.clear()
        getContext()?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (CommonUtils.checkPermission(it, BLUETOOTH_CONNECT)) {
                    if (CommonUtils.checkPermission(it, BLUETOOTH_SCAN)) {
                        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
                        if (pairedDevices.isNotEmpty()) {
                            for (device in pairedDevices) {
                                var data = BleDevice(device.name, device.address)
                                devicesList.add(data)
                            }
                            return devicesList
                        } else {
                            var data = BleDevice(NON_PAIRED, NON_PAIRED)
                            devicesList.add(data)
                            return devicesList
                        }
                    } else {
                        CommonUtils.getPermission(it, BLUETOOTH_SCAN,
                            BLUETOOTH_PERMISSION_DESC
                        )
                    }
                } else {
                    CommonUtils.getPermission(it, BLUETOOTH_CONNECT, BLUETOOTH_PERMISSION_DESC)
                }
            } else {
                val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
                if (pairedDevices.isNotEmpty()) {
                    for (device in pairedDevices) {
                        var data = BleDevice(device.name, device.address)
                        devicesList.add(data)
                    }
                    return devicesList
                } else {
                    var data = BleDevice(NON_PAIRED, NON_PAIRED)
                    devicesList.add(data)
                    return devicesList
                }
            }
        }
        return devicesList
    }

    private fun connectDevice(address: String) {
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        getContext()?.let {
            if (CommonUtils.checkPermission(it, BLUETOOTH_CONNECT)){
                try {
                    val bondState = device.bondState
                    when (bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            try {
                                device.createBond()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        BluetoothDevice.BOND_BONDED -> {}
                        BluetoothDevice.BOND_BONDING -> {}
                    }
                } catch (e: Exception) {
                    Log.e("TAG","Pairing failed: ${e.message}")
                }
        }
       }
    }

    public fun openOptionsDialog(item: String) {
        val builder = getContext()?.let { AlertDialog.Builder(it) }

        builder?.setTitle("Bluetooth")
            ?.setPositiveButton("Connect/Disconnect") { dialog, which ->
                connectDevice(item)
            }
            ?.setNegativeButton("Forget") { dialog, which ->
                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(item)
                removeBond(device)
            }
            ?.setNeutralButton("Settings") { dialog, which ->
                // call settings activity/fragments
            }
            ?.setIcon(android.R.drawable.ic_dialog_alert)
            ?.show()
    }


    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            getContext()?.registerReceiver(mReceiver, filter)
        } catch (e: Exception) {
            Log.e("TAG", "Removing bond has been failed. ${e.message}")
        }
    }
    fun setResultCallback(callback: ResultCallback) {
        resultCallback = callback
    }
    // Unregister the BroadcastReceiver when it's no longer needed
    fun unregisterReceiver() {
        if (getContext()!=null && mReceiver!=null){
            getContext()?.unregisterReceiver(mReceiver)
            Log.e("TAG", "Removing broadcast reciver ")

        }
    }
   val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            }else{
                if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    if (device != null && bondState == BluetoothDevice.BOND_NONE) {
                        var data = BleDevice(device?.name, device?.address)
                        Log.e("tag"," else device data "+data.name)
                        resultCallback?.onResult(data)
                    }else if (device != null && device?.bondState != BluetoothDevice.BOND_BONDED) {
                        var data = BleDevice(device?.name, device?.address)
                        Log.e("tag"," else device data "+data.name)
                        resultCallback?.onResult(data)
                    }
                }
            }

        }
    }

}