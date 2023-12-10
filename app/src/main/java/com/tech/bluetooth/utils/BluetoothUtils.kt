package com.tech.bluetooth.utils

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.tech.bluetooth.modal.BleDevice
import com.tech.bluetooth.utils.CommonUtils.BLUETOOTH_PERMISSION_DESC
import com.tech.bluetooth.utils.CommonUtils.NON_PAIRED

//@SuppressLint("StaticFieldLeak")
object BluetoothUtils {
    private lateinit var context: Activity
    private lateinit var bluetoothManager:BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    fun init(context:Activity) {
        this.context=context
        bluetoothManager=context.getSystemService(BluetoothManager::class.java)
        if (Build.VERSION.SDK_INT >= 31) {
            bluetoothAdapter=bluetoothManager.adapter
        } else {
            @Suppress("deprecation")
            bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }

    }

    public fun getPairedDeviceList() : MutableList<BleDevice> {
        var devicesList = mutableListOf<BleDevice>()
        devicesList.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (CommonUtils.checkPermission(context, BLUETOOTH_CONNECT)) {
                if (CommonUtils.checkPermission(context, BLUETOOTH_SCAN)) {
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
                    CommonUtils.getPermission(context, BLUETOOTH_SCAN,
                        BLUETOOTH_PERMISSION_DESC
                    )
                }
            } else {
                CommonUtils.getPermission(context, BLUETOOTH_CONNECT, BLUETOOTH_PERMISSION_DESC)
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
        return devicesList
    }

    private fun connectDevice(address: String) {
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
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