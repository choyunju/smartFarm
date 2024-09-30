package com.example.smartFarm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartFarm.databinding.ActivityControlBinding
import okio.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class ControlActivity : AppCompatActivity() {
    private var mbinding: ActivityControlBinding? = null
    private val binding get() = mbinding!!
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    //    private val deviceAddress = "00:11:22:33:AA:BB" //연결할 Arduino의 Bluetooth 주소
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private val deviceName = "HC-06"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //HC-05의 UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbinding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bluetooth 연결 시도
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (checkPermissions()) {
            initBluetooth()
        } else {
            request()
        }
        connectBluetooth()
        Log.d("controlActivity","process")
        setupClickListener()
    }

    private fun checkPermissions(): Boolean{
        Log.d("checkpermissions", "yes")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }

        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun request() {
        Log.d("request", "yes")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }



    private fun initBluetooth() {
        Log.d("initbluetooth", "yes")
        if(bluetoothAdapter == null) {
            Log.d("Bluetooth", "not supported in this device")
        } else if(!bluetoothAdapter!!.isEnabled){
            Log.d("bluetooth", "please enable Bluetooth")
        }
        return
    }

    private fun connectBluetooth() {
        Log.d("connectBluetooth", "yes")
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices ?: emptySet()

            for(device in pairedDevices) {
                if(device.name == deviceName) {
                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                        bluetoothSocket?.connect()
                        outputStream = bluetoothSocket?.outputStream
                        inputStream = bluetoothSocket?.inputStream
                        Log.d("bluetooth", "connected to HC-06")
                    }    catch (e: IOException) {
                        Log.d("bluetooth", "failed")
                    }
                }

            }
            Log.d("bluetooth connect", "success")
        } catch(e: IOException) {
            Log.d("bluetooth", "fail")
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBluetooth()
            } else {
                Log.d("Bluetooth","permissions are required")
            }
        }
    }

    private fun setupClickListener() {
        //환풍기 제어
        binding.fanControl.setOnClickListener {
            val text = binding.fanControl.text.toString()
            if(text == "ON") {
                binding.fanControl.text = "OFF"
                sendDataToArduino("0")
            } else if(text == "OFF") {
                binding.fanControl.text = "ON"
                sendDataToArduino("1")
            }
        }

        //물 공급 제어
        binding.waterControl.setOnClickListener {
            val text = binding.waterControl.text.toString()
            if(text == "ON") {
                binding.waterControl.text = "OFF"
                sendDataToArduino("E")
            } else if(text == "OFF") {
                binding.waterControl.text = "ON"
                sendDataToArduino("W")
            }
        }

        //LED 제어
        binding.ledControl.setOnClickListener {

        }

    }

    private fun sendDataToArduino(data: String) {
//        try {
//            bluetoothSocket?.outputStream?.write(data.toByteArray())
//        } catch(e: IOException) {
//            Log.d("sendDataToArduino", "fail")
//        }
        if(bluetoothSocket!= null && outputStream != null) {
            try {
                outputStream?.write(data.toByteArray())
                Log.d("data send", "success $data")
            } catch (e: IOException) {
                Log.d("data send", "fail")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.d("socket close", "fail")
        }
    }
}