package com.tech.bluetooth.ui

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.READ_PHONE_STATE
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tech.bluetooth.utils.CommonUtils
import com.tech.bluetooth.utils.CommonUtils.BLUETOOTH_PERMISSION_DESC
import com.tech.bluetooth.utils.CommonUtils.DEVICE_NAME
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_CONNECTION_LOST
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_DEVICE_NAME
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_READ
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_STATE_CHANGE
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_TOAST
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_UNABLE_CONNECT
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_WRITE
import com.tech.bluetooth.utils.CommonUtils.NON_PAIRED
import com.tech.bluetooth.utils.CommonUtils.NO_NEW_DEVICE
import com.tech.bluetooth.utils.CommonUtils.TOAST
import com.tech.bluetooth.R
import com.tech.bluetooth.adapter.NewDeviceAdapter
import com.tech.bluetooth.adapter.PairedAdapter
import com.tech.bluetooth.databinding.ActivityMainBinding
import com.tech.bluetooth.modal.BleDevice
import com.tech.bluetooth.utils.BluetoothServices
import com.tech.bluetooth.utils.OnItemClickListener


class MyDeviceListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
    }

    private lateinit var bleService :BluetoothServices

    private val pairedDeviceAdapter by lazy {
        PairedAdapter(this)
    }
    private val newDeviceAdapter by lazy {
        NewDeviceAdapter(this)
    }

    private val bluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }

    private val mBtAdapter by lazy {
        if (Build.VERSION.SDK_INT >= 31) {
            bluetoothManager.adapter
        } else {
            @Suppress("deprecation")
            BluetoothAdapter.getDefaultAdapter();
        }
    }

    private lateinit var binding: ActivityMainBinding

    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()


    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.e("TAG"," You enable ")
        startgettingDevices()
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else true

        if (canEnableBluetooth) {
            enableBluetoothLauncher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setResult(Activity.RESULT_CANCELED)

        binding.btnScan.setOnClickListener {
            doDiscovery()
            //it.visibility = View.GONE
        }

        binding.deviceListRv.apply {
            setHasFixedSize(true)
            adapter = pairedDeviceAdapter
        }

        pairedDeviceAdapter.setListeners(object : OnItemClickListener {
            override fun onItemClicked(view: View, item: String, position: Int) {
                openOptionsDialog(item)
            }
        })

        binding.newDeviceListRv.apply {
            setHasFixedSize(true)
            adapter = newDeviceAdapter
        }
        newDeviceAdapter.setListeners(object : OnItemClickListener {
            override fun onItemClicked(view: View, item: String, position: Int) {
                openOptionsDialog(item)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,))
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH))
        }
    }

    private fun openOptionsDialog(item: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Bluetooth")
            .setPositiveButton("Connect/Disconnect") { dialog, which ->
                itemClick(item)
            }
            .setNegativeButton("Forget") { dialog, which ->
                val device: BluetoothDevice = mBtAdapter.getRemoteDevice(item)
                removeBond(device)
            }
            .setNeutralButton("Settings") { dialog, which ->
                // call settings activity/fragments
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)

            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(mReceiver, filter)
        } catch (e: Exception) {
            Log.e("TAG", "Removing bond has been failed. ${e.message}")
        }
    }

    private fun startgettingDevices(){
        bleService=BluetoothServices(this@MyDeviceListActivity,mHandler)

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

       // val filterFinished = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        //registerReceiver(mReceiver, filterFinished)
            getPairedDeviceList()

    }
    private fun getPairedDeviceList(){
        var devicesList = mutableListOf<BleDevice>()
        devicesList.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (CommonUtils.checkPermission(this, BLUETOOTH_CONNECT)) {
                if (CommonUtils.checkPermission(this, BLUETOOTH_SCAN)) {
                    val pairedDevices: Set<BluetoothDevice> = mBtAdapter.bondedDevices
                    if (pairedDevices.isNotEmpty()) {
                        Log.e("TAG"," paired device ")
                        binding.tvPairedDevice.visibility = View.VISIBLE
                        for (device in pairedDevices) {
                            var data = BleDevice(device.name, device.address)
                            devicesList.add(data)
                        }
                        pairedDeviceAdapter.addData(devicesList)
                    } else {
                        var data = BleDevice(NON_PAIRED, NON_PAIRED)
                        devicesList.add(data)
                        pairedDeviceAdapter.addData(devicesList)
                    }
                } else {
                    CommonUtils.getPermission(this@MyDeviceListActivity, BLUETOOTH_SCAN,
                        BLUETOOTH_PERMISSION_DESC
                    )
                }
            } else {
                CommonUtils.getPermission(this, BLUETOOTH_CONNECT, BLUETOOTH_PERMISSION_DESC)
            }
        } else {
            val pairedDevices: Set<BluetoothDevice> = mBtAdapter.bondedDevices
            if (pairedDevices.isNotEmpty()) {

                binding.tvPairedDevice.visibility = View.VISIBLE
                for (device in pairedDevices) {
                    Log.e("TAG"," else paired device "+device.name)
                    var data = BleDevice(device.name, device.address)
                    devicesList.add(data)
                }
                pairedDeviceAdapter.addData(devicesList)
            } else {
                var data = BleDevice(NON_PAIRED, NON_PAIRED)
                devicesList.add(data)
                pairedDeviceAdapter.addData(devicesList)
            }
        }
    }

    private fun itemClick(address: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_CONNECT)) {
                if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_SCAN)) {
                    if (CommonUtils.checkPermission(this@MyDeviceListActivity, READ_PHONE_STATE)) {
                        mBtAdapter.cancelDiscovery()
                        if (address != NON_PAIRED && address != NO_NEW_DEVICE) {
                            val intent = Intent()
                            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)
                            //setResult(Activity.RESULT_OK, intent)
                            //finish()
                            connectDevice(address)
                        }
                    }else{
                        CommonUtils.getPermission(
                            this@MyDeviceListActivity,
                            READ_PHONE_STATE,
                            BLUETOOTH_PERMISSION_DESC
                        )
                    }
                } else {
                    CommonUtils.getPermission(
                        this@MyDeviceListActivity,
                        BLUETOOTH_SCAN,
                        BLUETOOTH_PERMISSION_DESC
                    )
                }
            } else {
                CommonUtils.getPermission(
                    this@MyDeviceListActivity,
                    BLUETOOTH_CONNECT,
                    BLUETOOTH_PERMISSION_DESC
                )
            }
        } else {
            mBtAdapter.cancelDiscovery()
            if (address != NON_PAIRED && address != NO_NEW_DEVICE) {
                val intent = Intent()
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address)
               // setResult(Activity.RESULT_OK, intent)
               // finish()
                connectDevice(address)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_CONNECT)) {
            if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_SCAN)) {
                if (mBtAdapter != null) {
                    mBtAdapter.cancelDiscovery()
                }
            }
        }
        unregisterReceiver(mReceiver)
    }

    private fun doDiscovery() {

        setProgressBarIndeterminateVisibility(true)
        setTitle(R.string.ble_scanning)

        binding.tvNewDevice.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_CONNECT)) {
                if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_SCAN)) {
                    if (mBtAdapter.isDiscovering) {
                        mBtAdapter.cancelDiscovery()
                    }
                    newDeviceAdapter.clearData()
                    mBtAdapter.startDiscovery()
                } else {
                    CommonUtils.getPermission(this, BLUETOOTH_SCAN, BLUETOOTH_PERMISSION_DESC)
                }
            } else {
                CommonUtils.getPermission(this, BLUETOOTH_CONNECT, BLUETOOTH_PERMISSION_DESC)
            }
        } else {
            if (mBtAdapter.isDiscovering) {
                mBtAdapter.cancelDiscovery()
            }
            newDeviceAdapter.clearData()
            if (CommonUtils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                mBtAdapter.startDiscovery()
            } else {
                CommonUtils.getPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    "Location Permission"
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("TAG", "  Permission on result " + requestCode + "  and " + grantResults[0])
        if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
             if (mBtAdapter.isEnabled){
                 startgettingDevices()
             }else{
                 enableBluetoothLauncher.launch(
                     Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                 )
             }
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (CommonUtils.checkPermission(this@MyDeviceListActivity, BLUETOOTH_CONNECT)) {
                    // When discovery finds a device
                    if (BluetoothDevice.ACTION_FOUND == action) {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        // If it's already paired, skip it, because it's been listed already
                        if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                            var data = BleDevice(device?.name, device?.address)
                            newDeviceAdapter.addData(data)
                        }

                        // When discovery is finished, change the Activity title
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                        setTitle("Select Device")
                        if (newDeviceAdapter.itemCount == 0) {
                            val data = BleDevice(NON_PAIRED, NON_PAIRED)
                            newDeviceAdapter.addData(data)
                        }
                    }else if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                        if (device != null && bondState == BluetoothDevice.BOND_NONE) {
                            var data = BleDevice(device?.name, device?.address)
                            Log.e("tag"," if device data "+data.name)
                            // The device is unpaired, update your device list here
                            //updateDeviceList()
                        }
                    }

                } else {
                    CommonUtils.getPermission(
                        this@MyDeviceListActivity,
                        BLUETOOTH_CONNECT,
                        BLUETOOTH_PERMISSION_DESC
                    )
                }
            } else {
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                        var data = BleDevice(device?.name, device?.address)
                        newDeviceAdapter.addData(data)
                    }
                    // When discovery is finished, change the Activity title
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    setTitle("Select_device")
                    if (newDeviceAdapter.itemCount == 0) {
                        val data = BleDevice(NON_PAIRED, NON_PAIRED)
                        newDeviceAdapter.addData(data)
                    }
                } else if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    if (device != null && bondState == BluetoothDevice.BOND_NONE) {
                        var data = BleDevice(device?.name, device?.address)
                        Log.e("tag"," else device data "+data.name)
                        pairedDeviceAdapter.remove(data)
                        // The device is unpaired, update your device list here
                        //updateDeviceList()
                    }else if (device != null && device?.bondState != BluetoothDevice.BOND_BONDED) {
                        var data = BleDevice(device?.name, device?.address)
                        Log.e("tag"," else device data "+data.name)
                        pairedDeviceAdapter.remove(data)
                        // The device is unpaired, update your device list here
                        //updateDeviceList()
                    }
                }

            }
        }
    }

    private fun connectDevice(address: String) {
        Log.e("TAG"," Connectionning...... ")
        val device: BluetoothDevice = mBtAdapter.getRemoteDevice(address)
        //bleService.connect(device)
            try {
                val bondState = device.bondState
                Log.e("TAG","Device is state "+bondState)
                when (bondState) {
                    BluetoothDevice.BOND_NONE -> {
                        Log.e("TAG","Device is state boded "+bondState)
                       try {
                           device.createBond()
                           var data = BleDevice(device?.name, device?.address)
                           pairedDeviceAdapter.update(data)
                           Log.e("TAG","Device is bonded "+data.name)
                       }catch (e:Exception){
                           e.printStackTrace()
                       }
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        Log.e("TAG","Device is already paired.")}
                    BluetoothDevice.BOND_BONDING -> {

                        Log.e("TAG","Device is in the process of pairing. ")}
                }
            } catch (e: Exception) {
                Log.e("TAG","Pairing failed: ${e.message}")
            }
     }

  }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothServices.STATE_CONNECTED -> {
                            Log.e("TAG"," Device connected ")
                        }
                        BluetoothServices.STATE_CONNECTING -> {
                           Log.e("TAG"," Device connectiong ")
                        }
                        BluetoothServices.STATE_LISTEN, BluetoothServices.STATE_NONE -> {
                            Log.e("TAG"," Device not connect ")
                        }
                    }
                }
                MESSAGE_WRITE -> {
                    // Handle MESSAGE_WRITE
                }
                MESSAGE_READ -> {
                    // Handle MESSAGE_READ
                }
                MESSAGE_DEVICE_NAME -> {
                    // Save the connected device's name
                    var mConnectedDeviceName = msg.data.getString(DEVICE_NAME) ?: ""
                    Log.e("TAG"," Device connected with "+mConnectedDeviceName)
                }
                MESSAGE_TOAST -> {
                    Log.e("TAG"," Device tost "+msg.data.getString(TOAST))
                }
                MESSAGE_CONNECTION_LOST -> {
                    Log.e("TAG"," Device tost device_connection_lost ")
                }
                MESSAGE_UNABLE_CONNECT -> {
                    Log.e("TAG"," Device tost unable_to_connect_device ")
                }
            }
        }
    }


