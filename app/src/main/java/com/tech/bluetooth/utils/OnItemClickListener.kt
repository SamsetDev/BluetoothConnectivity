package com.tech.bluetooth.utils

import android.view.View

interface OnItemClickListener {
    fun onItemClicked(view: View,item: String, position: Int)
}