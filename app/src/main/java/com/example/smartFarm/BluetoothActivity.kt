package com.example.smartFarm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartFarm.databinding.ActivityBluetoothBinding
import java.io.IOException
import java.util.UUID

class BluetoothActivity : AppCompatActivity() {
    private var mbinding: ActivityBluetoothBinding? = null
    private val binding get() = mbinding!!

    //Bluetooth
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    //private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //HC-05의 UUID
    private val uuid: UUID = UUID.fromString("8CE255C0-200A-11E0-AC64-0800200C9A66")  //스마트폰 - 스마트폰 끼리의 UUID
    private var pairedDevices: Set<BluetoothDevice>? = null
    private lateinit var bluetoothDevice: BluetoothDevice

    private val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        mbinding = ActivityBluetoothBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(Build.VERSION.SDK_INT >= 31) {
            if(permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                Log.d("permissions", "권한 확인")
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {  permission ->
        permission.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
        }
    }
}