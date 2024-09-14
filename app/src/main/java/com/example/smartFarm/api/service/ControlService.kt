package com.example.smartFarm.api.service

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ControlService {

    @POST("/control/ventilatorOn")
    fun controlVentilator(
        @Body ventilatorControl: String
    ): Call<Void>

}