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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.example.mobileanimesticker.R
import com.example.mobileanimesticker.activity.StickerActivity
import java.io.*


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val bt_start = findViewById<View>(R.id.bt_start) as Button
        bt_start.setOnClickListener {
            startSticker()
        }
        val bt_stop = findViewById<View>(R.id.bt_stop) as Button
        bt_stop.setOnClickListener { stopService(Intent(this@MainActivity, StickerActivity::class.java)) }

        val bt_makeSticker = findViewById<View>(R.id.bt_makesticker) as Button
        bt_makeSticker.setOnClickListener {
            pickFromGallery()
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
}