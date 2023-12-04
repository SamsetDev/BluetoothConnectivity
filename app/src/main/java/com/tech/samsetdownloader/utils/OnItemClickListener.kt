package com.tech.samsetdownloader.utils

import android.bluetooth.BluetoothDevice

interface OnItemClickListener {
    fun onItemClicked(item: String, position: Int)
}