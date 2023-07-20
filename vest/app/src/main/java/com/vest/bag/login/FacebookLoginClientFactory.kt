package com.vest.bag.login

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.vest.bag.login.LoginType.FacebookLogin


/**
 * Facebook 登录授权处理
 * @author xx
 * 2023/5/17 14:33
 */
class FacebookLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var facebookObserver: FacebookLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        facebookObserver = FacebookLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(facebookObserver)


        return object : ThirdPartyLoginClient {
            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                val permissions: Collection<String> = listOf("public_profile", "email")
                LoginManager.getInstance().logInWithReadPermissions(activity, permissions)
            }
        }
    }

    inner class FacebookLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //facebook配置
        lateinit var callbackManager: CallbackManager

        override fun onCreate(owner: LifecycleOwner) {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d("facebookLogin", "onCancel")
                }

                override fun onError(e: FacebookException) {
                    Log.d("facebookLogin", e.toString())
                    _loginCallback?.onError(error = ErrorInfo("", e.message ?: ""))
                }

                override fun onSuccess(result: LoginResult) {
                    Log.d("facebookLogin", result.toString())
                    // https://developers.facebook.com/docs/graph-api/overview/#fields  存取凭证
                    AccessToken.setCurrentAccessToken(result.accessToken)
                    // https://developers.facebook.com/docs/android/graph 进一步获取其他信息
                    val request = GraphRequest.newMeRequest(
                        result.accessToken
                    ) { obj, _ -> //具体返回数据https://developers.facebook.com/docs/graph-api/reference/user
                        obj?.let {
                            val id = it.getString("id")
                            val email = it.getString("email")
                            val firstName = it.getString("first_name")
                            val lastName = it.getString("last_name")
                            Log.d(
                                "authorizationInfo", AuthorizationInfo(
                                    id ?: "", email ?: "",
                                    firstName ?: "", lastName ?: ""
                                )
                                    .toString()
                            )
                            _loginCallback?.onSuccess(
                                AuthorizationInfo(
                                    id ?: "", email ?: "",
                                    firstName ?: "", lastName ?: ""
                                ), FacebookLogin
                            )

                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,first_name,last_name,email")
                    request.parameters = parameters
                    request.executeAsync()
                }
            })

        }


    }
}