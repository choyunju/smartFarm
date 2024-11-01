package com.example.smartFarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.smartFarm.databinding.ItemAutoBinding
import java.util.ArrayList


class AutoListAdapter: RecyclerView.Adapter<AutoListAdapter.ViewHolder>() {

    var autoList = mutableListOf<AutoData>()

    inner class ViewHolder(private val binding: ItemAutoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data:AutoData) {
            binding.device.text = data.device
            binding.time.text = data.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAutoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = autoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(autoList[position])
    }
}