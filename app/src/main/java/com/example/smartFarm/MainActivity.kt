package com.example.smartFarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.smartFarm.databinding.ActivityMainBinding
import okhttp3.OkHttp
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private lateinit var controlViewModel: ControlViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controlViewModel = ViewModelProvider(this)[ControlViewModel::class.java]

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.switchVentilator.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                val text = "/ventilatorOn"
                sendControl(text)
            }
        }
    }

    private fun sendControl(text: String) {
        controlViewModel.controlVentilator(text)
    }
}