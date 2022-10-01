package com.example.mobileanimesticker.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobileanimesticker.R
import android.content.Intent
import android.os.Handler


class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro) //xml , java 소스 연결

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)

    }
}