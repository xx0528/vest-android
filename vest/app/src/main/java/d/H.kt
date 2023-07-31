package d

import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import com.appsflyer.AppsFlyerLib
import a.F
import sw.ef.gh.event.AdJustTool
import sw.ef.gh.event.AppsFlyTool
import sw.ef.gh.login.FbLogin
import sw.ef.gh.login.LoginTool
import sw.ef.gh.login.LoginType
import sw.ef.gh.utils.JSKey
import sw.ef.gh.utils.log
import sw.ef.gh.utils.setDirection
import org.json.JSONObject
import java.lang.Exception

/**
 * @Author: xx
 * @Date: 2021/9/21 15:08
 * @Desc:
 */
class H(private val activity: G) {

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
                    return F.getInstance().getGoogleAdId()
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