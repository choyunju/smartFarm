package com.example.smartFarm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.smartFarm.api.ApiObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ControlViewModel: ViewModel() {

    fun controlVentilator(
        ventilatorControl: String
    ) {
        ApiObject.manageControl().controlVentilator(ventilatorControl)
            .enqueue(object: Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    when(response.code()) {
                        200 -> { Log.d("control ventilator", ventilatorControl)}
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("control ventilator", "error")
                }

            })

    }


}