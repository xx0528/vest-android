package com.vest.bag.webview

import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import com.appsflyer.AppsFlyerLib
import com.vest.bag.MainApplication
import com.vest.bag.event.AdJustTool
import com.vest.bag.event.AppsFlyTool
import com.vest.bag.login.FbLogin
import com.vest.bag.login.LoginTool
import com.vest.bag.login.LoginType
import com.vest.bag.utils.JSKey
import com.vest.bag.utils.log
import com.vest.bag.utils.setDirection
import org.json.JSONObject
import java.lang.Exception

/**
 * @Author: xx
 * @Date: 2021/9/21 15:08
 * @Desc:
 */
class JsInterface(private val activity: WebActivity) {

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
            when (jSONObj.getString(JSKey.Method)) {
                JSKey.Event -> {
                    when (jSONObj.getString(JSKey.EventType)) {
                        JSKey.EventAF -> {
                            return AppsFlyTool.onEvent(jSONObj)
                        }

                        JSKey.EventAJ -> {
                            return AdJustTool.onEvent(jSONObj)
                        }

                    }
                    ""
                }

                JSKey.OpenUrlWebview -> {
                    val url = jSONObj.getString(JSKey.Url)
                    activity.openUrlWebView(url)
                    ""
                }

                JSKey.OpenUrlBrowser -> {
                    val url = jSONObj.getString(JSKey.Url)
                    activity.openUrlBrowser(url)
                    ""
                }

                JSKey.OpenWindow -> {
                    val url = jSONObj.getString(JSKey.Url)
                    activity.mWebView.post { activity.mWebView.loadUrl("javascript:window.open('$url','_blank');") }
                    ""
                }

                JSKey.GetAppsFlyerUID -> AppsFlyerLib.getInstance().getAppsFlyerUID(activity)
                JSKey.GetSPAID -> {
                    return MainApplication.getInstance().getGoogleAdId()
                }

                JSKey.GetSPREFERRER -> {
//                    AppUtils.getSP(this, IConstant.SP_KEY_REFERRER)
                    ""
                }

                JSKey.Login -> {
                    val loginTypeStr = jSONObj.getString(JSKey.LoginType)
                    val bWait = jSONObj.getBoolean(JSKey.IsWaitForResult)
                    var loginType: LoginType = LoginType.FacebookLogin
                    when(loginTypeStr) {
                        JSKey.FbLogin -> {
                            loginType = LoginType.FacebookLogin
                        }
                        JSKey.GoogleLogin -> {
                            loginType = LoginType.GoogleLogin
                        }
                        JSKey.TwitterLogin -> {
                            loginType = LoginType.TwitterLogin
                        }
                        JSKey.LinkedInLogin -> {
                            loginType = LoginType.LinkedInLogin
                        }
                    }
                    return LoginTool.login(activity, loginType, bWait)
                    ""
                }

                JSKey.FbLogin -> {
                    return FbLogin.getInstance().login()
                }

                JSKey.FbShare -> {
                    val title = jSONObj.getString(JSKey.ShareTitle)
                    val link = jSONObj.getString(JSKey.ShareLink)
                    val details = jSONObj.getString(JSKey.ShareDetails)
                    return FbLogin.getInstance().share(title, link, details)
                }

                JSKey.GoogleLogin -> {
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