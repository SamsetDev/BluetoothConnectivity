package com.tech.bluetooth.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object CommonUtils {


    val NON_PAIRED="No matching devices"
    val NO_NEW_DEVICE="No device found"
    var PERMISSION_REQUEST_CODE = 105
    val BLUETOOTH_PERMISSION_DESC="This app needs the Bluetooth permission, please accept to use bluetooth functionality"

    @kotlin.jvm.JvmField
    val MESSAGE_STATE_CHANGE = 1
    @kotlin.jvm.JvmField
    val MESSAGE_READ = 2
    @kotlin.jvm.JvmField
    var MESSAGE_WRITE: Int=1
    @kotlin.jvm.JvmField
    val MESSAGE_DEVICE_NAME = 4
    @kotlin.jvm.JvmField
    val MESSAGE_TOAST = 5
    @kotlin.jvm.JvmField
    val MESSAGE_CONNECTION_LOST = 6
    @kotlin.jvm.JvmField
    val MESSAGE_UNABLE_CONNECT = 7

    /** */ // Key names received from the BluetoothService Handler
    var DEVICE_NAME = "device_name"
    var TOAST = "toast"
    fun checkPermission(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    fun getPermission(activity: Activity, permission: String, permissionExplanation: String, permissionRequestCode: Int) {
        if (checkPermission(activity, permission)) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showRationalPermission(activity, permission, permissionExplanation, permissionRequestCode)
        } else {
            // Returns true if permission is denied by the user for the first time and sets it to true in permission result
            ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionRequestCode)
        }
    }
    fun getPermission(activity: Activity, permission: String, permissionExplanation: String) {
        if (checkPermission(activity, permission)) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showRationalPermission(activity, permission, permissionExplanation, PERMISSION_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }
    private fun showRationalPermission(activity: Activity, permission: String, permissionExplanation: String, permissionRequestCode: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(permissionExplanation)
        builder.setPositiveButton("Ok") { dialog, which ->
            ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionRequestCode)
        }
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}