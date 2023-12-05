package com.tech.bluetooth.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.tech.bluetooth.utils.CommonUtils.DEVICE_NAME
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_DEVICE_NAME
import com.tech.bluetooth.utils.CommonUtils.MESSAGE_TOAST
import com.tech.bluetooth.utils.CommonUtils.TOAST
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothServices(private val context: Context, private val handler: Handler) {

    companion object {
        private const val TAG = "BluetoothService"
        private const val NAME = "ZJPrinter"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
    }

    private var mAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mState = STATE_NONE
    private val mHandler: Handler = handler
    private var mAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    var ErrorMessage = "No_Error_Message"

    @Synchronized
    private fun setState(state: Int) {
        Log.e(TAG, "setState() $mState -> $state")
        mState = state
        mHandler.obtainMessage(CommonUtils.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
    }

    @Synchronized
    fun getState(): Int {
        return mState
    }

    @Synchronized
    fun start() {
        Log.e(TAG, "start")
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }
        if (mAcceptThread == null) {
            mAcceptThread = AcceptThread()
            mAcceptThread?.start()
        }
        setState(STATE_LISTEN)
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.e(TAG, "connect to: $device")
        if (mState == STATE_CONNECTING) {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
        setState(STATE_CONNECTING)
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        Log.e(TAG, "connected")
        mConnectThread?.cancel()
        mConnectThread = null
        mConnectedThread?.cancel()
        mConnectedThread = null
        mAcceptThread?.cancel()
        mAcceptThread = null
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()
        val msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(DEVICE_NAME, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)
        setState(STATE_CONNECTED)
    }

    @Synchronized
    fun stop() {
        Log.e(TAG, "stop")
        setState(STATE_NONE)
        mConnectThread?.cancel()
        mConnectThread = null
        mConnectedThread?.cancel()
        mConnectedThread = null
        mAcceptThread?.cancel()
        mAcceptThread = null
    }

    fun write(out: ByteArray) {
        var r: ConnectedThread?
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        r?.write(out)
    }

    private fun connectionFailed() {
        setState(STATE_LISTEN)
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    private fun connectionLost() {
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString("TOAST", "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mAdapter?.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            var socket: BluetoothSocket? = null
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket?.accept()
                } catch (e: IOException) {
                    break
                }
                if (socket != null) {
                    synchronized(this@BluetoothServices) {
                        when (mState) {
                            STATE_LISTEN, STATE_CONNECTING -> connected(socket, socket.remoteDevice)
                            STATE_NONE, STATE_CONNECTED -> {
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }

    private inner class ConnectThread(private val mmDevice: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mmDevice.createRfcommSocketToServiceRecord(MY_UUID)
        }

        override fun run() {
            mAdapter?.cancelDiscovery()
            try {
                mmSocket?.connect()
            } catch (e: IOException) {
                connectionFailed()
                try {
                    mmSocket?.close()
                } catch (e2: IOException) {
                    // Ignore
                }
                return
            }
            synchronized(this@BluetoothServices) {
                mConnectThread = null
            }
            connected(mmSocket!!, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }

    private inner class ConnectedThread(private val mmDevice: BluetoothSocket) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        private var mmInStream: InputStream? = null
        private var mmOutStream: OutputStream? = null

        init {
            Log.e(TAG, "create ConnectedThread")
            mmSocket = mmDevice
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmDevice.inputStream
                tmpOut = mmDevice.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }


        override fun run() {
            Log.e(TAG, "BEGIN mConnectedThread")
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    val buffer = ByteArray(256)
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    if (bytes > 0) {
                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(CommonUtils.MESSAGE_READ, bytes, -1, buffer).sendToTarget()
                    } else {
                        Log.e(TAG, "disconnected")
                        connectionLost()
                        //add by chongqing jinou
                        if (mState != STATE_NONE) {
                            Log.e(TAG, "disconnected")
                            // Start the service over to restart listening mode
                            this@BluetoothServices.start()
                        }
                        break
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()

                    //add by chongqing jinou
                    if (mState != STATE_NONE) {
                        // Start the service over to restart listening mode
                        this@BluetoothServices.start()
                    }
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                mmOutStream?.write(buffer)
                mmOutStream?.flush()
                if (buffer.size > 3000) //
                {
                    val readata = ByteArray(1)
                    SPPReadTimeout(readata, 1, 5000)
                }
                Log.e("BTPWRITE", buffer.toString())
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(CommonUtils.MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        private fun SPPReadTimeout(Data: ByteArray, DataLen: Int, Timeout: Int): Boolean {
            for (i in 0 until Timeout / 5) {
                try {
                    if (mmInStream!!.available() >= DataLen) {
                        return try {
                            mmInStream!!.read(Data, 0, DataLen)
                            true
                        } catch (e: IOException) {
                            ErrorMessage = e.message.toString()
                            false
                        }
                    }
                } catch (e: IOException) {
                    ErrorMessage = e.message.toString()
                    return false
                }
                try {
                    sleep(5L)
                } catch (e: InterruptedException) {
                    ErrorMessage = e.message.toString()
                    return false
                }
            }
            ErrorMessage = "Not able to connect time out "
            return false
        }


        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }

    }

}
