package com.example.smartFarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smartFarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding : ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.controlBtn.setOnClickListener {
            var intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

        binding.monitoringBtn.setOnClickListener {
            var intent = Intent(this, MonitoringActivity::class.java)
            startActivity(intent)
        }
    }
}