package com.vest.bag.login

import android.content.Context
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.vest.bag.MainApplication
import com.vest.bag.login.LoginType.FacebookLogin
import com.vest.bag.login.LoginType.GoogleLogin
import com.vest.bag.login.LoginType.LinkedInLogin
import com.vest.bag.login.LoginType.TwitterLogin
import com.vest.bag.utils.JSKey
import com.vest.bag.utils.log
import com.vest.bag.webview.WebActivity


/**
 * 把所有登录逻辑一起实现，如果单独使用，自己可以各自单独初始化和调用login方法
 * @author xx
 * 2023/5/19 11:23
 */
object LoginTool {
    private var googleLoginClient: ThirdPartyLoginClient? = null
    private var facebookLoginClient: ThirdPartyLoginClient? = null
    private var twitterLoginClient: ThirdPartyLoginClient? = null
    private var linkedInLoginClient: ThirdPartyLoginClient? = null

    /**
     * 由于很多第三方需要获取startActivityForResult结果，但是为了不在具体调用方硬编码onActivityResult，这里使用新的Activity Results API来处理，
     * Activity Results API要求协议需要在在OnCreate中定义
     */
    fun initOnCreate(activity: ComponentActivity) {

        val data = MainApplication.getInstance().getData()
        if (data.getBoolean(JSKey.FbLogin)) {
            facebookLoginClient = FacebookLoginClientFactory().create(activity)
        }

        if (data.getBoolean(JSKey.GoogleLogin)) {
            googleLoginClient = GoogleLoginClientFactory().create(activity)
        }

        if (data.getBoolean(JSKey.TwitterLogin)) {
            twitterLoginClient = TwitterLoginClientFactory().create(activity)
        }

        if (data.getBoolean(JSKey.LinkedInLogin)) {
            linkedInLoginClient = LinkedInLoginClientFactory().create(activity)
        }
        //默认使用谷歌原生方案
        //googleLoginClient = GoogleFirebaseLoginClientFactory().create(activity)
    }

    fun login(context: WebActivity, loginType: LoginType, isWaitForResult: Boolean = true) : String {
        var resultStr = ""

        onLogin(context,
            FacebookLogin,object : LoginCallback {
                override fun onSuccess(result: AuthorizationInfo, type: LoginType) {
                    Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show()
                    resultStr = result.toString()
                    if (!isWaitForResult) {
                        //模拟
                        val js = "javascript:paramLogin($resultStr)"
                        var result = ""
                        context.mWebView.evaluateJavascript(js){}
                    }
                }

                override fun onError(error: ErrorInfo) {
                    Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                    resultStr = error.toString()
                    //不需要等， 利用回调获取返回结果的 可以直接回调个具体的函数，这个具体的函数逻辑要在之前就注册好，
                    // 在js函数里再回调需要返回的函数
                    if (!isWaitForResult) {
                        //模拟
                        val js = "javascript:paramLogin($resultStr)"
                        var result = ""
                        context.mWebView.evaluateJavascript(js){}
                    }
                }

            })

        //需要等 直接返回结果的 要经过js处理后再给出去
        if (isWaitForResult) {
            //先等获取数据
            while (resultStr.isEmpty()) {
                try {
                    Thread.sleep(100) // 阻塞线程等待结果
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            //再等去js里处理完返回来结果

            val js = "javascript:paramLogin($resultStr)"
            var result: String = ""
            context.mWebView.evaluateJavascript(js) {
                log("getPayId onReceiveValue $it")
                result = it
            }
            while (result.isEmpty()) {
                try {
                    Thread.sleep(100) // 阻塞线程等待结果
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return result
        }
        return ""
    }


    fun onLogin(context: Context, loginType: LoginType, loginCallback: LoginCallback) {
        when (loginType) {
            FacebookLogin -> {
                if (isInstalledPackage(context, "com.facebook.katana")) {
                    facebookLoginClient?.login(loginCallback)
                } else {
                    Toast.makeText(context, "Please install the facebook app first.", Toast.LENGTH_SHORT).show()
                }
            }

            GoogleLogin -> {
                if (isGMSAvailable(context)) {
                    googleLoginClient?.login(loginCallback)
                } else {
                    Toast.makeText(context, "Google Play services is not available.", Toast.LENGTH_SHORT).show()
                }

            }

            TwitterLogin -> {
                twitterLoginClient?.login(loginCallback)
            }

            LinkedInLogin -> {
                linkedInLoginClient?.login(loginCallback)
            }
        }

    }

    fun loginFacebook(context: Context, loginCallback: LoginCallback) {

        if (isInstalledPackage(context, "com.facebook.katana")) {
            facebookLoginClient?.login(loginCallback)
        } else {
            Toast.makeText(context, "Please install the Facebook app first", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginGoogle(context: Context, loginCallback: LoginCallback) {
        if (isGMSAvailable(context)) {
            googleLoginClient?.login(loginCallback)
        } else {
            Toast.makeText(context, "Google Play services is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginLinkedIn(loginCallback: LoginCallback) {
        linkedInLoginClient?.login(loginCallback)
    }

    fun loginTwitter(loginCallback: LoginCallback) {
        twitterLoginClient?.login(loginCallback)
    }

    private fun isInstalledPackage(context: Context, packageName: String): Boolean {
        val packageInfos = context.packageManager.getInstalledPackages(0)
        if (packageInfos.isEmpty() || packageName.isEmpty()) {
            return false
        }
        val packageInfo = packageInfos.find { it.packageName.equals(packageName) }
        return packageInfo != null
    }

    private fun isGMSAvailable(context: Context): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }
}