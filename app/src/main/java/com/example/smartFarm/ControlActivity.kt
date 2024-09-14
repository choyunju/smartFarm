package com.example.smartFarm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.smartFarm.databinding.ActivityControlBinding
import okio.IOException
import java.util.UUID

class ControlActivity : AppCompatActivity() {
    private var mbinding: ActivityControlBinding? = null
    private val binding get() = mbinding!!
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val deviceAddress = "00:11:22:33:AA:BB" //연결할 Arduino의 Bluetooth 주소
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //HC-05의 UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbinding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bluetooth 연결 시도
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        connectBluetooth()

        setupClickListener()
    }

    private fun connectBluetooth() {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothSocket = device?.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            Log.d("bluetooth connect", "success")
        } catch(e: IOException) {
            Log.d("bluetooth", "fail")
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

        }

        //LED 제어
        binding.ledControl.setOnClickListener {

        }

    }

    private fun sendDataToArduino(data: String) {
        try {
            bluetoothSocket?.outputStream?.write(data.toByteArray())
        } catch(e: IOException) {
            Log.d("sendDataToArduino", "fail")
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