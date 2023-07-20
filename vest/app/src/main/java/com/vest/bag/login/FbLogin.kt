package com.vest.bag.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.Sharer
import com.facebook.share.model.ShareContent
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.vest.bag.utils.log
import org.json.JSONException
import org.json.JSONObject


class FbLogin {
    private lateinit var mActivity: Activity
    private var mCallbackManager: CallbackManager? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: FbLogin

        fun getInstance(): FbLogin {
            return instance
        }
    }

    init {
        instance = this
    }

    // 构造函数
    fun initFbLogin(activity: Activity) {
        // 初始化回调管理器和分享对话框
        mActivity = activity
        mCallbackManager = CallbackManager.Factory.create()
        log("初始化 facebook")
    }

    // 登陆函数
    fun login(): String {
        var resultString = "{\"status\":0, \"msg\":\"login failed\"}" // 默认返回登录失败的结果
        log("login facebook -----------------------1")
        val permissions: Collection<String> = listOf("public_profile", "email")
        LoginManager.getInstance().logInWithReadPermissions(mActivity, permissions)
        LoginManager.getInstance()
            .registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // 登录成功
                    val accessToken = loginResult.accessToken
                    val request = GraphRequest.newMeRequest(accessToken,
                        GraphJSONObjectCallback { `object`, response ->
                            try {
                                log("login facebook -----------------------3")
                                val jsObj = response.jsonObject
                                if (jsObj != null) {
                                    val jsName = jsObj.getString("name")
                                }
                                if (`object` == null) {
                                    resultString = String.format(
                                        "{\"status\":0, \"msg\":\"login error accessToken = %s responseError = %s\"}",
                                        accessToken.token,
                                        response.error.toString()
                                    )
                                    log("login facebook ---4 error -- $resultString")
                                    return@GraphJSONObjectCallback
                                }
                                val name = `object`.getString("name")
                                val id = `object`.getString("id")
                                val pictureUrl =
                                    `object`.getJSONObject("picture").getJSONObject("data")
                                        .getString("url")
                                val email = `object`.getString("email")

                                // 构建JSON字符串
                                val json = JSONObject()
                                json.put("status", 1)
                                json.put("name", name)
                                json.put("id", id)
                                json.put("picture", pictureUrl)
                                json.put("email", email)
                                resultString = json.toString()
                                log("login facebook -----------------------5")
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        })
                    log("login facebook -----------------------2")
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,picture")
                    request.parameters = parameters
                    //                request.executeAndWait(); // 等待回调完成
                    request.executeAsync() // 这里要改成异步
                }

                override fun onCancel() {
                    // 用户取消登录
                    log("login facebook -- onCancel -----------------------")
                    resultString = "{\"status\":0, \"msg\":\"user cancel\"}"
                }

                override fun onError(e: FacebookException) {
                    // 登录出错
                    log("login facebook -- onError -----------------------$e")
                    resultString =
                        String.format("{\"status\":0, \"msg\":\"login error = %s\"}", e.toString())
                }
            })
        while (resultString == "{\"status\":0, \"msg\":\"login failed\"}") {
            try {
                Thread.sleep(100) // 阻塞线程等待结果
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        log("login facebook -----------------------5")
        return resultString
    }

    // 分享函数
    fun share(title: String?, link: String?, details: String?): String {
        var resultStr = "{\"status\":0, \"msg\":\"share failed\"}" // 默认返回分享失败的结果
        val shareContent: ShareContent<*, *> = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(link))
            .setShareHashtag(
                ShareHashtag.Builder()
                    .setHashtag(title).build()
            )
            .setQuote(details)
            .build()
        val dialog = ShareDialog(mActivity)
        dialog.registerCallback(mCallbackManager, object : FacebookCallback<Sharer.Result?> {
            override fun onSuccess(res: Sharer.Result?) {
                // 分享成功
                resultStr = "1"
            }

            override fun onCancel() {
                // 用户取消分享
                resultStr = "2"
            }

            override fun onError(error: FacebookException) {
                // 分享出错
                resultStr = "3"
            }
        })
        dialog.show(shareContent) // 显示分享对话框
        while (resultStr == "{\"status\":0, \"msg\":\"share failed\"}") {
            try {
                Thread.sleep(100) // 阻塞线程等待结果
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return resultStr
    }

    // 在Activity的onActivityResult中调用回调管理器的onActivityResult方法
    fun onActResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log("helper - onActResult --------------requestCode = $requestCode resultCode = $resultCode")
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
    }
}