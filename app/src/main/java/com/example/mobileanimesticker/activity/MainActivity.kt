package com.example.mobileanimesticker.activity


import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mobileanimesticker.R
import com.example.mobileanimesticker.activity.StickerActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log("start")

        //check write, read permission. if not allow, exit
        checkImgPermission()


        val bt_start = findViewById<View>(R.id.bt_start) as Button
        bt_start.setOnClickListener {
            checkOverlayPermission()
        }
        val bt_stop = findViewById<View>(R.id.bt_stop) as Button
        bt_stop.setOnClickListener { stopService(Intent(this@MainActivity, StickerActivity::class.java)) }

        val bt_makeSticker = findViewById<View>(R.id.bt_makesticker) as Button
        bt_makeSticker.setOnClickListener {
            pickFromGallery()
        }
    }


    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 지금 창이 오버레이 설정창이 아니라면
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                intent.putExtra(REQUESTCODE, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                getActiveResult_overlay?.launch(intent)

            } else {
                startSticker()
            }
        } else {
            startSticker()
        }
    }

    //for start StickerActivity Scene
    private fun startSticker(){
        val file = File(filesDir.toString() + "/stickerpath")
        if(!file.exists()){
            Toast.makeText(this, "스티커를 세팅해주세요", Toast.LENGTH_LONG).show()
        } else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(Intent(this, StickerActivity::class.java))
            }else{
                startService(Intent(this@MainActivity, StickerActivity::class.java))
            }
        }
    }

    //<ImgPermissionGet>
    //For Img Permission Get in First Time app running
    private fun checkImgPermission() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ){
            var permissions = arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            ActivityCompat.requestPermissions(this, permissions, PERMISSON_ACCESS_ALL)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSON_ACCESS_ALL){
            if(grantResults.isNotEmpty()){
                for(grant in  grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED) System.exit(0)
                }
            }
        }
    }
    //</ImgPermissionGet>


    private var getActiveResult_overlay = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == RESULT_OK){
            val data : Intent ?= result.data
            val requestCode = result.data?.getStringExtra(REQUESTCODE)

            if(!Settings.canDrawOverlays(this)){
                finish()
            } else {
                startSticker()
            }
            log("overlay")
        }
    }

    private var getActiveResult_Img = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK){
            var uri : Uri? = result?.data?.data
            var path : String? = getRealPathFromURI(uri!!)

            var outputFile : FileOutputStream = openFileOutput("stickerpath", MODE_PRIVATE)
            outputFile.write(path?.toByteArray())
            outputFile.close()
            showText("스티커가 변경되었습니다.", 2000)
        }
    }


    //About Get Img form gallery
    private fun pickFromGallery(){
        var intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        getActiveResult_Img?.launch(intent)
    }


    private fun getRealPathFromURI(uri : Uri) : String {
        var buildName = Build.MANUFACTURER
        if (buildName.equals("Xiaomi")) {
            return uri.path!!
        }

        var columnIndex = 0
        var proj = arrayOf(MediaStore.Images.Media.DATA)
        var cursor = contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }

    fun log(log : String){
        Log.d("Log", log)
    }
    fun showText(text : String, time : Int){
        Toast.makeText(this@MainActivity, text, time).show()
    }





    companion object {
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1 // 오버레이 권한 설정창
        private const val REQ_GALLERY = 2
        private const val PERMISSON_ACCESS_ALL = 100
        const val REQUESTCODE = "requestCode"
    }

}