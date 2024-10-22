package com.example.smartFarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MonitoringViewModel: ViewModel() {
    private var _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> get() = _temperature

    private var _humidity = MutableLiveData<String>()
    val humidity: LiveData<String> get() = _humidity

    private var _moisture = MutableLiveData<String>()
    val moisture: LiveData<String> get() = _moisture

    fun updateTemperature(newTemperature: String) {
        _temperature.value = newTemperature
    }

    fun updateHumidity(newHumidity: String) {
        _humidity.value = newHumidity
    }

    fun updateMoisture(newMoisture: String) {
        _moisture.value = newMoisture
    }

    fun updateSenSorData(sensorValue: Map<String, String>) {
        val newHumidity = sensorValue["Humidity"]
        val newTemperature = sensorValue["Temperature"]
        val newMoisture = sensorValue["Moisture"]
        _humidity.value = newHumidity
        _temperature.value = newTemperature
        _moisture.value = newMoisture
    }
}