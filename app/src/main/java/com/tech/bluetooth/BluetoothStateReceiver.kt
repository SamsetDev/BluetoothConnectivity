package com.tech.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.tech.bluetooth.ui.MyDeviceListActivity

class BluetoothStateReceiver : BroadcastReceiver() {
    private var isEnable:Boolean=false
    override fun onReceive(context: Context?, intent: Intent?) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            handleBluetoothState(context, state)
        }
    }
    private fun handleBluetoothState(context: Context?, state: Int) {
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                isEnable=false
            }
            BluetoothAdapter.STATE_ON -> {
                isEnable=true
            }
        }
        val resultReceiver = getResultReceiver(context)
        resultReceiver?.send(Activity.RESULT_OK, Bundle().apply { putBoolean("data", isEnable) })
    }
    private fun getResultReceiver(context: Context?): ResultReceiver? {
        // Get the ResultReceiver from the activity
        if (context is MyDeviceListActivity) {
            //return context.resultReceivers
        }
        return null
    }

}