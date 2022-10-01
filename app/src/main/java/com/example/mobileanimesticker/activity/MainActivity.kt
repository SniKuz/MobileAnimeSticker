package com.example.mobileanimesticker.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobileanimesticker.R
import android.content.Intent
import com.example.mobileanimesticker.activity.StickerActivity
import android.os.Build
import com.example.mobileanimesticker.activity.MainActivity
import android.annotation.TargetApi
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bt_start = findViewById<View>(R.id.bt_start) as Button
        bt_start.setOnClickListener {
            checkPermission()
            //startService(new Intent(MainActivity.this, AlwaysOnTopService.class));
        }
        val bt_stop = findViewById<View>(R.id.bt_stop) as Button
        bt_stop.setOnClickListener { stopService(Intent(this@MainActivity, StickerActivity::class.java)) }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 지금 창이 오버레이 설정창이 아니라면
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            } else {
                startService(Intent(this@MainActivity, StickerActivity::class.java))
            }
        } else {
            startService(Intent(this@MainActivity, StickerActivity::class.java))
        }
    }

    @TargetApi(Build.VERSION_CODES.M) // M 버전 이상 API를 타겟
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
            } else {
                startService(Intent(this@MainActivity, StickerActivity::class.java))
            }
        }
    }

    companion object {
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1 // 오버레이 권한 설정창
    }
}