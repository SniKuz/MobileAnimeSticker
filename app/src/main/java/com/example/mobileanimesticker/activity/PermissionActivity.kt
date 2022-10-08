package com.example.mobileanimesticker.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mobileanimesticker.BuildConfig
import com.example.mobileanimesticker.R

class PermissionActivity : AppCompatActivity() {


    var btn_next : Button? = null
    var btn_gallery : Button? = null
    var btn_overlay : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        btn_next = findViewById<Button>(R.id.btn_permission_allow)
        btn_next!!.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        btn_gallery = findViewById(R.id.btn_gallery_permission)
        btn_gallery!!.setOnClickListener {
            checkImgPermission()
            goNext()
        }

        btn_overlay = findViewById(R.id.btn_overlay_permission)
        btn_overlay!!.setOnClickListener {
            checkOverlayPermission()
            goNext()
        }

        //check write, read permission. if not allow, exit
        //checkImgPermission()
    }


    private fun checkImgPermission() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ){
            var permissions = arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            ActivityCompat.requestPermissions(this, permissions, PermissionActivity.PERMISSON_ACCESS_ALL)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PermissionActivity.PERMISSON_ACCESS_ALL){
            if(grantResults.isNotEmpty()){
                var isAllGranted = true
                for(grant in  grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        isAllGranted = false
                        break
                    }
                }
                if(isAllGranted){
                    goNext()
                }
                else{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                    startActivity(intent)
                }
            }
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 지금 창이 오버레이 설정창이 아니라면
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                getActiveResult_overlay?.launch(intent)
            } else{
                goNext()
            }
        }
    }

    private var getActiveResult_overlay = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == RESULT_OK){

            if(!Settings.canDrawOverlays(this)){
                finish()
            } else {
                goNext()
            }
        }
        goNext()
    }

    private fun goNext(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && Settings.canDrawOverlays(this)
        )
            btn_next!!.isEnabled = true
    }



    companion object {
        private const val PERMISSON_ACCESS_ALL = 100
    }
}