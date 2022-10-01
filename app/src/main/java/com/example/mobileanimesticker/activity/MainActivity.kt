package com.example.mobileanimesticker.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobileanimesticker.R
import android.content.Intent
import com.example.mobileanimesticker.activity.StickerActivity
import android.os.Build
import com.example.mobileanimesticker.activity.MainActivity
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check write, read permission. if not allow, exit
        checkImgPermission()




        val bt_start = findViewById<View>(R.id.bt_start) as Button
        bt_start.setOnClickListener {
            checkOverlayPermission()
            //startService(new Intent(MainActivity.this, AlwaysOnTopService.class));
        }
        val bt_stop = findViewById<View>(R.id.bt_stop) as Button
        bt_stop.setOnClickListener { stopService(Intent(this@MainActivity, StickerActivity::class.java)) }

        val bt_makeSticker = findViewById<View>(R.id.bt_makesticker) as Button
        bt_makeSticker.setOnClickListener {
            var imgIntent = Intent(Intent.ACTION_PICK)
            imgIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            imgIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            imgIntent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, IMG_GET)
        }
    }


    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 지금 창이 오버레이 설정창이 아니라면
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
//                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)

            } else {
                startService(Intent(this@MainActivity, StickerActivity::class.java))
            }
        } else {
            startService(Intent(this@MainActivity, StickerActivity::class.java))
        }
    }

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
        else if(resultCode == RESULT_OK && resultCode == IMG_GET){
            var list = ArrayList<Uri>()
            list.clear()
            if(data?.clipData != null){
                val count = data.clipData!!.itemCount
                for(i in 0 until  count){
                    val img = data.clipData!!.getItemAt(i).uri
                    list.add(img)
                }
            }
            else{
                data?.data?.let {uri ->
                    val img : Uri? = data?.data
                    if(img != null){
                        list.add(img)
                    }
                }
            }
        }

    }




    companion object {
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1 // 오버레이 권한 설정창
        private const val IMG_GET = 2
        private const val PERMISSON_ACCESS_ALL = 100
    }
}