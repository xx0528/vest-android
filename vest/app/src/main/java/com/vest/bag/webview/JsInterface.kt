package com.vest.bag.webview

import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import com.appsflyer.AppsFlyerLib
import com.vest.bag.MainApplication
import com.vest.bag.event.AdJustTool
import com.vest.bag.event.AppsFlyTool
import com.vest.bag.utils.log
import com.vest.bag.utils.setDirection
import org.json.JSONObject
import java.lang.Exception

/**
 * @Author: xx
 * @Date: 2021/9/21 15:08
 * @Desc:
 */
class JsInterface(private val activity: WebActivity){

    @JavascriptInterface
    fun jsLog(log: String) {
        Log.i("javascript log-------- ", "log -- $log")
    }

    @JavascriptInterface
    fun loadUrlOpen(url: String) {
        activity.mWebView.post { activity.mWebView.loadUrl("javascript:window.open('$url','_blank');") }
    }

    @JavascriptInterface
    fun setActivityOrientation(orientation: String) {
        if (orientation.isNotEmpty()) {
            setDirection(activity, orientation)
            return
        }
        log("传入的方向字符串不对=--$orientation")
    }


    @JavascriptInterface
    fun onCall(params: String): String? {
//        LogUtil.i("调用了 onCall -- $params")
        return try {
            if (TextUtils.isEmpty(params)) {
                return null
            }
            val jSONObj = JSONObject(params)

//            LogUtil.i("param === " + jSONObj.getString("param"))
            when (jSONObj.getString("method")) {
                "event" -> {
                    when (jSONObj.getString("eventType")) {
                        "af" -> {
                            return AppsFlyTool.onEvent(jSONObj)
                        }

                        "aj" -> {
                            return AdJustTool.onEvent(jSONObj)
                        }

                    }
                    ""
                }

                "openUrlWebview" -> {
                    val url = jSONObj.getString("url")
                    activity.openUrlWebView(url)
                    ""
                }

                "openUrlBrowser" -> {
                    val url = jSONObj.getString("url")

                    activity.openUrlBrowser(url)
                    ""
                }

                "openWindow" -> {
                    val url = jSONObj.getString("url")
                    activity.mWebView.post { activity.mWebView.loadUrl("javascript:window.open('$url','_blank');") }
                    ""
                }

                "getAppsFlyerUID" -> AppsFlyerLib.getInstance().getAppsFlyerUID(activity)
                "getSPAID" -> {
                    MainApplication.getInstance().getGoogleAdId()
                    ""
                }

                "getSPREFERRER" -> {
//                    AppUtils.getSP(this, IConstant.SP_KEY_REFERRER)
                    ""
                }

                "fbLogin" -> {
//                    val basicLogin: BasicLogin = this.basicLogin
//                    if (basicLogin != null) {
//                        basicLogin.fbLogin(webView, optString)
//                        return ""
//                    }
                    ""
                }

                "googleLogin" -> {
//                    val basicLogin2: BasicLogin = this.basicLogin
//                    if (basicLogin2 != null) {
//                        basicLogin2.googleLogin(webView, optString)
//                        return ""
//                    }
                    ""
                }

                else -> ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}