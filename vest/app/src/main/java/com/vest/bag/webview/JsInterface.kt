package com.vest.bag.webview

import android.webkit.JavascriptInterface
import com.vest.bag.utils.log
import com.vest.bag.utils.showToast

/**
 * @Author: xx
 * @Date: 2021/9/21 15:08
 * @Desc:
 */
class JsInterface {

    @JavascriptInterface
    fun showToastByAndroid(log: String) {
        log("showToastByAndroid:$log")
        showToast(log)
    }

}