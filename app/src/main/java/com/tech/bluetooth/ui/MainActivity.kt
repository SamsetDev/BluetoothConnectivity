package com.tech.bluetooth.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.tech.bluetooth.adapter.PairedAdapter
import com.tech.bluetooth.databinding.ActivityMain2Binding
import com.tech.bluetooth.modal.BleDevice
import com.tech.bluetooth.utils.BluetoothUtils
import com.tech.bluetooth.utils.OnItemClickListener

class MainActivity : AppCompatActivity() {
    private lateinit var ble:BluetoothUtils
    private val pairedDeviceAdapter by lazy {
        PairedAdapter(this)
    }
    private lateinit var binding: ActivityMain2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding.deviceRv.apply {
            setHasFixedSize(true)
            adapter = pairedDeviceAdapter
        }


        ble=BluetoothUtils.getInstance(this)
        ble.init()
        var lists = ble.getPairedDeviceList() as MutableList<BleDevice>

        var devicesList = mutableListOf<BleDevice>()
        for (device in lists){
            Log.e("TAG","  get utils list|||| "+device.name+"  and "+device.address)
            var data = BleDevice(device.name, device.address)
            devicesList.add(data)
        }
        pairedDeviceAdapter.addData(devicesList)

        pairedDeviceAdapter.setListeners(object : OnItemClickListener {
            override fun onItemClicked(view: View, item: String, position: Int) {
                ble.openOptionsDialog(item)
            }
        })


        // for forget device
        ble.setResultCallback(object : BluetoothUtils.ResultCallback {
            override fun onResult(result: BleDevice) {
               // remove ble object from list and notifyupdate
                Log.e("TAG","  call back "+result)
                pairedDeviceAdapter.remove(result)
                ble.unregisterReceiver()
            }
        })

        //ble.removeBond()
       // Don't forget to unregister the receiver when it's no longer needed
      //  ble.unregisterReceiver()

    }

}