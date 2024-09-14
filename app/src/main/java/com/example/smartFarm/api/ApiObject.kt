package com.example.smartFarm.api

import com.example.smartFarm.api.service.ControlService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ApiObject {
    companion object {
        private const val BASE_URL = "http://172.20.10.7/"
        private val gson: Gson = GsonBuilder().setLenient().create()
        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        fun manageControl(): ControlService {
            return retrofit.create(ControlService::class.java)
        }

    }
}