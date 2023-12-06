package com.tech.bluetooth.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.bluetooth.utils.OnItemClickListener
import com.tech.bluetooth.databinding.BluetoothItemsBinding
import com.tech.bluetooth.modal.BleDevice

class PairedAdapter(val context: Context) : RecyclerView.Adapter<PairedAdapter.BleViewHolder>() {
    private var devicesList = mutableListOf<BleDevice>()
    private lateinit var onItemClickListeners: OnItemClickListener

    public fun addData(data: MutableList<BleDevice>) {
        devicesList.clear()
        devicesList.addAll(data)
        notifyDataSetChanged()
    }

    public fun update(data: BleDevice) {
        devicesList.add(0,data)
        notifyDataSetChanged()
    }

    public fun remove(data: BleDevice) {
        devicesList.remove(data)
        notifyDataSetChanged()
    }

    public fun setListeners(listeners: OnItemClickListener) {
        onItemClickListeners = listeners
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        val binding = BluetoothItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return BleViewHolder(binding)
    }


    override fun getItemCount(): Int = devicesList.size

    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
           holder.bind(devicesList.get(position),onItemClickListeners)
    }

    class BleViewHolder(private val itemview: BluetoothItemsBinding) :
        RecyclerView.ViewHolder(itemview.root) {
        fun bind(data: BleDevice,itemclick: OnItemClickListener) {
           itemview.tvName.text=data.name
            itemview.tvName.setOnClickListener {
                itemclick.onItemClicked(itemview.tvName,data.address.toString(),adapterPosition)
            }

        }

    }
}