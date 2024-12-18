package com.example.smartFarm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.smartFarm.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var mbinding: ActivityMainBinding? = null
    private val binding get() = mbinding!!
    private lateinit var monitoringViewModel: MonitoringViewModel
    private val handler = Handler()
    private val buffer = ByteArray(1024)
    private var bufferPosition = 0

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private val deviceName = "HC-06"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //HC-06의 UUID

    private var fanAuto = false
    private var fanStartTime: Long = 0
    private var pumpAuto = false
    private var pumpStartTime: Long = 0

    var autoDatas = ArrayList<AutoData>()

    private lateinit var sensorMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mbinding = ActivityMainBinding.inflate(layoutInflater)
        monitoringViewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]
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

        setupControlListener()
        autoDatas = arrayListOf<AutoData>()
        binding.notifications.setOnClickListener {
            val intent = Intent(this, AutoActivity::class.java)
            intent.putParcelableArrayListExtra("autoNotifications", autoDatas)
            startActivity(intent)
        }
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
                        Thread(DataReceiver()).start()
                        Log.d("bluetooth", "connected to HC-06")
                    }    catch (e: IOException) {
                        Log.d("bluetooth", "failed")
                    }

                    //LivaData를 관찰
                    monitoringViewModel.temperature.observe(this, Observer { newTemperature ->
                        binding.Temperature.text = "$newTemperature°C"
                    })
                    monitoringViewModel.humidity.observe(this, Observer { newHumidity ->
                        binding.Humidity.text = "$newHumidity%"
                    })
                    monitoringViewModel.moisture.observe(this, Observer { newMoisture ->
                        binding.Moisture.text = "$newMoisture%"
                    })
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

    //데이터를 수신하고 온도 데이터만 추출하여 LivaData에 업데이트하는 Thread
    inner class DataReceiver: Runnable {
        override fun run() {
            while(true) {
                try {
                    val bytesAvailable = inputStream!!.available()
                    if(bytesAvailable > 0) {
                        val bytes = ByteArray(bytesAvailable)
                        inputStream!!.read(bytes)

                        for(i in 0 until bytesAvailable) {
                            val b = bytes[i]
                            if(b == '\n'.toByte()) {
                                val receiveData = String(buffer, 0, bufferPosition)
                                bufferPosition = 0
                                Log.d("receiveData=", receiveData)
                                if(receiveData.contains("Fan: ON (Automatic)") && !fanAuto) {
                                    fanAuto = true
                                    fanStartTime = System.currentTimeMillis()
                                } else if(receiveData.contains("Fan: OFF (Automatic)") && fanAuto) {
                                    fanAuto = false
                                    val fanEndTime = System.currentTimeMillis()
                                    val duration = (fanEndTime - fanStartTime) / 1000 // 초 단위로 변환
                                    autoDatas.add(AutoData("Fan", duration.toString()+"초 동안 켜져 있었습니다!"))
                                }
                                if(receiveData.contains("Water Pump: ON (Automatic)") && !pumpAuto) {
                                    pumpAuto = true
                                    pumpStartTime = System.currentTimeMillis()
                                } else if(receiveData.contains("Water Pump: OFF (Automatic)") && pumpAuto) {
                                    pumpAuto = false
                                    val pumpEndTime = System.currentTimeMillis()
                                    val duration = (pumpEndTime - pumpStartTime) / 1000
                                    autoDatas.add(AutoData("Water Pump", duration.toString()+"초 동안 켜져 있었습니다!"))
                                }

                                val sensorValue = parseSensor(receiveData)

                                //LiveData를 통해 센서값 업데이트
                                handler.post {
                                    monitoringViewModel.updateSenSorData(sensorValue)
                                }
                            } else {
                                buffer[bufferPosition++] = b
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        //습도, 온도, 수분 값을 추출
        private fun parseSensor(data: String): Map<String, String> {
            val humidityStartIndex = data.indexOf("H:") + 2
            val humidityEndIndex = data.indexOf("%", humidityStartIndex)
            val tempStartIndex = data.indexOf("T:") + 2
            val tempEndIndex = data.indexOf("C", tempStartIndex)
            val moistureStartIndex = data.indexOf("S:") + 2
            val moistureEndIndex = data.indexOf("%", moistureStartIndex)

            if(humidityStartIndex != -1 && humidityEndIndex != -1) {
                sensorMap = mutableMapOf("Humidity" to data.substring(humidityStartIndex, humidityEndIndex).trim())
            } else {
                "N/A"
            }
            if(tempStartIndex != -1 && tempEndIndex != -1) {
                sensorMap["Temperature"] = data.substring(tempStartIndex, tempEndIndex).trim()
            } else {
                "N/A"
            }
            if(moistureStartIndex != -1 && moistureEndIndex != -1) {
                sensorMap["Moisture"] = data.substring(moistureStartIndex, moistureEndIndex).trim()
            } else {
                "N/A"
            }

            return sensorMap
        }
    }

    private fun setupControlListener() {
        binding.fanSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.fanFrame.setBackgroundResource(R.drawable.fill_border)
                sendData("F")
            }
            else {
                binding.fanFrame.setBackgroundResource(R.drawable.border)
                sendData("G")
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
            e.printStackTrace()
        }
    }
}