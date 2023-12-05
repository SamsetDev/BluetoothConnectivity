package com.tech.bluetooth.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tech.bluetooth.databinding.ActivityHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class HomeActivity : AppCompatActivity() {
    private var isBluetoothEnable = false
    private var connectedPosition = -1
    private var connectedAddress = ""
    private var startTime: Long = 0
    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!

    companion object {
        private const val BATTERY_LOW_PERCENTAGE = 15
        private const val PAIRING_PIN = "1234"
        private const val BLUETOOTH_ENABLE_BUNDLE_KEY = "bluetooth_enable_bundle_key"
        private const val CONNECTED_POSITION_BUNDLE_KEY = "connected_position_bundle_key"
        private const val CONNECTED_ADDRESS_BUNDLE_KEY = "connected_address_bundle_key"
        private const val START_TIME_BUNDLE_KEY = "start_time_bundle_key"
    }

    private val btAdapter by lazy {
        //BleAccountAdapter(this)
    }

    private val bluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        if (Build.VERSION.SDK_INT >= 31) {
            bluetoothManager?.adapter
        } else {
            @Suppress("deprecation")
            BluetoothAdapter.getDefaultAdapter();
        }
    }

    private var isDeviceConnected = false

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Not needed */ }

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

    private val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    // Do something you need here
                    isDeviceConnected = true
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    isDeviceConnected = false
                    Toast.makeText(applicationContext, "Bluetooth_disconnect", Toast.LENGTH_SHORT).show()
                }

                else -> println("Default")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            //adapter = btAdapter
        }

        addClickListeners()

        /*btAdapter.setListeners(object :ItemClickListener{
            override fun onItemClicked(item: BluetoothDevice, position: Int) {
                connectDevice(item, position)
            }

        })*/

    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(bluetoothStateReceiver, filter)
    }

    private fun addClickListeners() {
        binding.btSwitch.setOnCheckedChangeListener { _, isChecked ->
            bluetoothAdapter?.let {
                isBluetoothEnable = if (!isChecked) {
                    it.disable()
                    setRecyclerViewVisibility(false)
                    binding.progressBar.isVisible = false
                    false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,))
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH,))
                    }
                    binding.progressBar.isVisible = true
                    getDeviceList(it)
                    true
                }
            }
        }

    }

    private fun startBluetoothDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryReceiver, filter)

        val discoveryStarted = bluetoothAdapter?.startDiscovery()
        if (discoveryStarted == true) {
            Log.d("Bluetooth", "Discovery started")
        } else {
            Log.e("Bluetooth", "Failed to start discovery")
        }
    }
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.e("Bluetooth", "Discovered device: ${device?.name} - ${device?.address}")

                    // Add the discovered device to your list
                    // Update your UI or perform any other desired actions
                    // Example: deviceList.add(device)
                }
            }
        }
    }

    private fun getDeviceList(bluetoothAdapter: BluetoothAdapter) {
        lifecycleScope.launch {
            val list = mutableListOf<BluetoothDevice>()
            var pairedDevices = bluetoothAdapter.bondedDevices
            pairedDevices.forEach { Log.e("TAG","  fetch devic d "+it.name) }

            withContext(Dispatchers.IO) {
                while (!bluetoothAdapter.isDiscovering) {
                    pairedDevices = bluetoothAdapter.bondedDevices
                    if (pairedDevices.isNotEmpty() || bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                        break
                    }
                }
            }
            if (pairedDevices.size > 0) {
                for (device in pairedDevices) {
                    list.add(device)
                }
            }
            binding.progressBar.isVisible = false
            setRecyclerViewVisibility(true)
           // btAdapter.addData(list)
        }
    }

    private fun setRecyclerViewVisibility(isVisible: Boolean) {
        binding.recyclerView.isVisible = isVisible
    }

    private fun connectDevice(device: BluetoothDevice, position: Int) {
        binding.progressBar.isVisible = true

        try {
            // Create a Bluetooth socket
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            // Start the pairing process
            socket.connect()
            // Pairing is successful, handle further operations
            Log.e("Bluetooth", "Pairing successful with ${device.name}")
        } catch (e: IOException) {
            binding.progressBar.isVisible = false
            Log.e("Bluetooth", "Error pairing with ${device.name}", e)
        }


        /*val uuid = UUID.randomUUID()
        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        try {
            socket.connect()
            connectedPosition = position
            connectedAddress = device.address
            val outputStream: OutputStream = socket.outputStream
            outputStream.write(PAIRING_PIN.toByteArray())
        } catch (e: IOException) {
            binding.progressBar.isVisible = false
            Log.e("TAG","  Not able to pair "+e)
            Toast.makeText(this@HomeActivity, "Error connecting to device ${device.name}", Toast.LENGTH_SHORT).show()
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        unregisterReceiver(bluetoothStateReceiver)
    }
}