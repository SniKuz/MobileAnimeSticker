package com.example.mobileanimesticker.activity


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import com.bumptech.glide.Glide
import com.example.mobileanimesticker.R
import kotlinx.coroutines.Delay
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Integer.max
import java.lang.Integer.min


class StickerActivity : Service() {
    var windowManager: WindowManager? = null
    var mView: View? = null

    var params: WindowManager.LayoutParams? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    var stickerWidth : Int? = null
    var stickerHeight : Int? = null
    var imgRatio : Int? = null
    var isHeightBigger : Boolean? = null

    override fun onCreate() {
        super.onCreate()
        var valueUTF : String?

        val file = File(filesDir.toString() + "/stickerpath")
        val reader = FileReader(file)
        val buffer = BufferedReader(reader)
        valueUTF = buffer.readLine()
        buffer.close()

        var option : BitmapFactory.Options = getBitMapSize(valueUTF)
        stickerHeight = option.outHeight
        stickerWidth = option.outWidth
        isHeightBigger = if(option.outHeight > option.outWidth) true else false
        imgRatio = max(stickerHeight!!, stickerWidth!!) / min(stickerHeight!!, stickerWidth!!)
        log(stickerHeight.toString())
        log(stickerWidth.toString())
        log(imgRatio.toString())

        //<ForeGround Service> upper Andorid O ver
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel()
//            val notification = NotificationCompat.Builder(this, TAG)
//                .setContentTitle("Anime Sticker")
//                .setContentText("Anime Sticker is running")
//                .setSmallIcon(R.drawable.logo)
//                .build()
//            startForeground(NOTI_ID, notification) 0)
            var notificationIntent = Intent(this, MainActivity::class.java)
            var pendingIntent = PendingIntent.getActivity(this, PENDING_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            val remoteViews = RemoteViews(
                packageName,
                R.layout.foreground_notification
            )
            val notification = NotificationCompat.Builder(this, TAG)
                .setSound(null)
                .setSmallIcon(android.R.color.transparent)
                .setContentTitle("Anime Sticker")
                .setContent(remoteViews) //setting xml in notification
                .setContentIntent(pendingIntent) //setting pendingIntent for if touch, work intent
                .setAutoCancel(true)
                .build()
            startForeground(NOTI_ID, notification)
        }
        //</ForeGround>

        val inflate = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams( /*ViewGroup.LayoutParams.MATCH_PARENT*/
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  //Android O버전 부터 TYPE_SYSTEM_ALERT이 Deprecated되서  TYPE_APPLICATION_OVERLAY로 변경
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params!!.gravity = Gravity.LEFT or Gravity.TOP
        mView = inflate.inflate(R.layout.activity_sticker, null)


        mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height = if(isHeightBigger!!) 400 * imgRatio!! else 400
        mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width = if(isHeightBigger!!) 400  else 400 * imgRatio!!
        //GIF ON code. under lucyAnimation is sprite anime
        Glide.with(this)
            .load(valueUTF)
            .error(R.drawable.logo)
            .into(mView!!.findViewById(R.id.imageView))

        val button = mView!!.findViewById<View>(R.id.imageView) as ImageView
        button.setOnTouchListener(mViewTouchListener)
        windowManager!!.addView(mView, params)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 서비스가 호출될 때마다 실행
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            stopForeground(true)
        }

        if (windowManager != null) {
            if (mView != null) {
                windowManager!!.removeView(mView)
                mView = null
            }
            windowManager = null
        }
    }

    //Manifest stopWithTask="false" + this
    //When app is killed by swiping in android, kill service
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }


    //TouchListner 관련 코드
    //중요!!
    val _handler = Handler(Looper.getMainLooper())

    private var mTouchX = 0f
    private var mTouchY = 0f
    private var multiTouchDistanceStart = 600f
    private var multiTouchDistanceEnd = 0f
    private var mViewX = 0
    private var mViewY = 0
    private val mViewTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(event.pointerCount ==1){
                    mTouchX = event.rawX
                    mTouchY = event.rawY
                    mViewX = params!!.x
                    mViewY = params!!.y
                } else if(event.pointerCount == 2 ){
                    multiTouchDistanceStart = getDistance(event)
                }

            }
            MotionEvent.ACTION_MOVE -> {
                if(event.pointerCount == 1){
                    val x = (event.rawX - mTouchX).toInt()
                    val y = (event.rawY - mTouchY).toInt()
                    params!!.x = mViewX + x
                    params!!.y = mViewY + y
                    windowManager!!.updateViewLayout(mView, params)
                } else if(event.pointerCount == 2){
                    multiTouchDistanceEnd = getDistance(event)
                    if(multiTouchDistanceEnd > multiTouchDistanceStart){
                        if(isHeightBigger!!){
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height <= 1000 )
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height * 1.1f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height <= 1000)
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width * 1.1f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width
                        }
                        else{
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width <= 1000 )
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height * 1.1f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width <= 1000)
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width * 1.1f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width
                        }

                    } else if(multiTouchDistanceEnd < multiTouchDistanceStart){
                        if(isHeightBigger!!){
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height >= 250 )
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height * 0.9f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height >= 250)
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width * 0.9f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width
                        }
                        else{
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width >= 250 )
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height * 0.9f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.height
                            mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width =
                                if(mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width >= 250)
                                    (mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width * 0.9f).toInt() else mView?.findViewById<View>(R.id.imageView)?.layoutParams!!.width
                        }
                    }
                }
                    windowManager!!.updateViewLayout(mView, params)
//                    mView?.layoutParams = params
                }//횟수가아니라 거리에따라서 크기조정으로바꾸고
                // 연속적으로 보여주는거는 위에거 보내서 주고 > 살짝 부자연스럽게 됨 뚝 뚝 끊김
                //너무 살짝만 움직여도 확확작아짐 그냥 차이만 나면 바로 해버리니까.
            MotionEvent.ACTION_POINTER_UP -> {
                Handler(Looper.getMainLooper()).postDelayed({
                }, 500)
            }
        }
        true
    }

    fun getDistance(e : MotionEvent) : Float {
        var x = e.getX(0) - e.getX(1).toDouble()
        var y = e.getY(0) - e.getY(1)
        return Math.sqrt(x*x + y*y).toFloat()
    }


    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                TAG,
                "Anime Sticker",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Sticker Anime Tests"

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                notificationChannel
            )
        }
    }

    fun getBitMapSize(imgPath : String) : BitmapFactory.Options{
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imgPath, options)
        return options
    }

    fun log(msg : String){
        Log.d("테스트", msg)
    }
    fun msg(msg : String){
        Toast.makeText(this@StickerActivity, msg, Toast.LENGTH_SHORT).show()
    }



    companion object {
        private const val NOTI_ID = 3
        private const val TAG = "[Sticker Service]"
        const val PENDING_CODE = 4
    }
}
