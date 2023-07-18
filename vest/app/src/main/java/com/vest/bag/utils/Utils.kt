package com.vest.bag.utils

import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.Toast
import java.lang.reflect.Method

/**
 * @Author: xx
 * @Date: 2021/9/20 0:11
 * @Desc:
 */
fun log(log: Any?) {
    Log.e("RobustWebView-" + Thread.currentThread().name, log.toString())
}

fun showToast(msg: String) {
    Toast.makeText(ContextHolder.application, msg, Toast.LENGTH_SHORT).show()
}
