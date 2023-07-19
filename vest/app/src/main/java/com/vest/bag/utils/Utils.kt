package com.vest.bag.utils

import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vest.bag.MainApplication
import java.lang.reflect.Method

/**
 * @Author: xx
 * @Date: 2021/9/20 0:11
 * @Desc:
 */
fun log(log: Any?) {
    Log.e("MJB-" + Thread.currentThread().name, log.toString())
}

fun showToast(msg: String) {
    Toast.makeText(MainApplication.getInstance(), msg, Toast.LENGTH_SHORT).show()
}

fun setDirection(activity: AppCompatActivity, orientation: String) {
    if (orientation == "portrait") {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } else if (orientation == "sensorLandscape") {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun setFullWindow(activity: AppCompatActivity) {
    activity.window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    val lp = activity.window.attributes
    lp.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) lp.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

    activity.window.attributes = lp
    activity.window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
}