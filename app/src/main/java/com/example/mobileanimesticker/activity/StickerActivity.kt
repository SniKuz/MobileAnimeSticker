package com.example.mobileanimesticker.activity


import android.app.Service
import android.media.MediaPlayer
import android.content.Intent
import android.os.IBinder
import com.example.mobileanimesticker.R
import android.graphics.PixelFormat
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.util.Log
import android.view.*
import com.example.mobileanimesticker.activity.StickerActivity
import android.view.View.OnTouchListener
import android.widget.ImageView

class StickerActivity : Service() {
    var mp: MediaPlayer? = null
    var windowManager: WindowManager? = null
    var mView: View? = null

    //    MultiTouchListner multiTouchListner = new MultiTouchListner(this);
    var params: WindowManager.LayoutParams? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val inflate = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //<BGM>
        mp = MediaPlayer.create(this, R.raw.test_bgm)
        mp!!.setLooping(true)
        //</BGM>
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
        //        final TextView textView = (TextView) mView.findViewById(R.id.textView); //필요 없어서 삭제
        val button = mView!!.findViewById<View>(R.id.imageView) as ImageView
        //        button.setOnTouchListener(multiTouchListner);
        button.setOnTouchListener(mViewTouchListener)

        //animation put in(now lucy)
        button.setImageResource(R.drawable.lucy_animation)
        val lucyAnimation = button.drawable as AnimationDrawable
        lucyAnimation.start()
        windowManager!!.addView(mView, params)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 서비스가 호출될 때마다 실행
        mp!!.start() // 노래 시작
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (windowManager != null) {
            if (mView != null) {
                windowManager!!.removeView(mView)
                mView = null
            }
            windowManager = null
        }
        mp!!.stop()
    }

    //TouchListner 관련 코드
    //중요!!
    var myService = this
    val _handler = Handler()
    var _longPressed = Runnable {
//        Log.i("info", "it work")
        myService.stopSelf()
    }
    private var mTouchX = 0f
    private var mTouchY = 0f
    private var mViewX = 0
    private var mViewY = 0
    private val mViewTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchX = event.rawX
                mTouchY = event.rawY
                mViewX = params!!.x
                mViewY = params!!.y
                _handler.postDelayed(_longPressed, LONG_PRESS_TIME.toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                val x = (event.rawX - mTouchX).toInt()
                val y = (event.rawY - mTouchY).toInt()
                params!!.x = mViewX + x
                params!!.y = mViewY + y
                windowManager!!.updateViewLayout(mView, params)
                _handler.postDelayed(_longPressed, LONG_PRESS_TIME.toLong())
            }
            MotionEvent.ACTION_UP -> _handler.removeCallbacks(_longPressed)
        }
        true
    }

    companion object {
        var LONG_PRESS_TIME = 4000 //miliseconds
    }
}