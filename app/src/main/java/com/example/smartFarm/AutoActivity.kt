package com.example.smartFarm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartFarm.databinding.ActivityAutoBinding

class AutoActivity : AppCompatActivity() {
    private var mbinding: ActivityAutoBinding? = null
    private val binding get() = mbinding!!
    private lateinit var adapter: AutoListAdapter

    val autoDatas = ArrayList<AutoData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mbinding = ActivityAutoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        intent?.let {
            val list = it.getParcelableArrayListExtra<AutoData>("autoNotifications")
            if(list != null) {
                autoDatas.addAll(list)
            }
        }

        initAutoList()
    }

    private fun initAutoList() {
        val adapter = AutoListAdapter()
        adapter.autoList = autoDatas
        binding.autoList.adapter = adapter
        binding.autoList.layoutManager = LinearLayoutManager(this)
    }
}