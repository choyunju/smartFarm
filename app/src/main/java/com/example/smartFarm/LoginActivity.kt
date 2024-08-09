package com.example.smartFarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smartFarm.databinding.ActivityLoginBinding
import okhttp3.*
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private var mBinding : ActivityLoginBinding? = null
    private val binding get() = mBinding!!

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginBtn.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
            val message = binding.inputId.toString()
            sendRequest(message)
        }
    }

    private fun sendRequest(message: String) {
        val url = "http://172.20.10.7/?message=$message"

        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if(!response.isSuccessful) {
                    throw IOException("unexpected code $response")
                }

                val responseData = response.body?.string()

                runOnUiThread {
                    binding.textView2.text = responseData
                }
            }
        })
    }
}