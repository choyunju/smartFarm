package com.example.smartFarm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartFarm.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var mbinding: ActivityMainBinding? = null
    private val binding get() = mbinding!!

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private val deviceName = "HC-06"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //HC-06의 UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //Bluetooth 권한 체크
        if (checkPermissions()) {
            initBluetooth()
        } else {
            request()
        }

        Log.d("MainActivity","process")
        setupControlListener()
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
        } else {
            connectBluetooth()
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

    private fun setupControlListener() {
        binding.fanSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.fanFrame.setBackgroundResource(R.drawable.fill_border)
                sendData("0")
            }
            else {
                binding.fanFrame.setBackgroundResource(R.drawable.border)
                sendData("1")
            }
        }

        binding.waterSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.waterFrame.setBackgroundResource(R.drawable.fill_border)
                sendData("W")
            }
            else {
                binding.waterFrame.setBackgroundResource(R.drawable.border)
                sendData("E")
            }
        }

        binding.ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.ledFrame.setBackgroundResource(R.drawable.fill_border)
            }
            else {
                binding.ledFrame.setBackgroundResource(R.drawable.border)
            }
        }
    }

    private fun sendData(data: String) {
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