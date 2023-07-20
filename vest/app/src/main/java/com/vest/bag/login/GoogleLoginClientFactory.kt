package com.vest.bag.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.vest.bag.login.LoginType.GoogleLogin
import com.vest.bag.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


/**
 *
 * @author xx
 * 2023/5/31 9:56
 */
class GoogleLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var googleObserver: GoogleLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var _activity: Activity

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        _activity = activity
        //注册StartActivityForResult 替代废弃的需要在activity中使用的onActivityResult
        googleObserver = GoogleLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(googleObserver)
        oneTapClient = Identity.getSignInClient(activity)
        signInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                // Your server's client ID, not your Android client ID.
                .setServerClientId(activity.getString(R.string.google_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
            .setAutoSelectEnabled(false)
            .build()

        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(activity) { result ->
                        //注意，官方的demo是使用旧的startIntentSenderForResult，而result只返回了pendingIntent，需要自己用IntentSenderRequest包装一下
                        try {
                            googleObserver.googleRequest.launch(
                                IntentSenderRequest
                                    .Builder(result.pendingIntent)
                                    .build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.d("googleLogin", "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(activity) { e ->
                        e.localizedMessage?.let { Log.d("googleLogin", it) }
                        _loginCallback?.onError(error = ErrorInfo("", e.message ?: ""))
                    }
            }

        }
    }

    inner class GoogleLifecycleObserver(private val registry: ActivityResultRegistry) :
        DefaultLifecycleObserver {
        //google配置
        lateinit var googleRequest: ActivityResultLauncher<IntentSenderRequest>

        override fun onCreate(owner: LifecycleOwner) {
            //此处代码学习androidx.activity.ComponentActivity.registerForActivityResult的实现，解决没有activity的情况下无法调用registerForActivityResult
            //注意Google有些特别，官方demo是使用startIntentSenderForResult
            googleRequest = registry.register(
                "activity_rq#" + "google_login",
                owner,
                StartIntentSenderForResult()
            ) { result ->
                result.data?.let {
                    handleResult(it)
                }

            }
        }
    }

    private fun handleResult(data: Intent) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idTokenStr = credential.googleIdToken
            val verifier: GoogleIdTokenVerifier =
                GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
                    .setAudience(Collections.singletonList(_activity.getString(R.string.google_client_id)))
                    .build();
            when {
                idTokenStr != null -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        //https://developers.google.com/identity/one-tap/android/linked-account-signin-client?hl=zh-cn
                        val idToken: GoogleIdToken = verifier.verify(idTokenStr)
                        val payload: Payload = idToken.payload
                        withContext(Dispatchers.Main) {
                            val authorizationInfo = AuthorizationInfo(
                                payload.subject ?: "", payload.email,
                                credential.givenName ?: "", credential.familyName ?: ""
                            )
                            _loginCallback?.onSuccess(
                                authorizationInfo, GoogleLogin
                            )
                        }


                    }

                }

                else -> {
                    Log.d("googleLogin", "No ID token!")
                }
            }
        } catch (e: ApiException) {
            Log.w("googleLogin", "signInResult:failed message=" + e.message)
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d("googleLogin", "One-tap dialog was closed.")
                }

                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.d("googleLogin", "One-tap encountered a network error.")
                    _loginCallback?.onError(
                        error = ErrorInfo(
                            e.statusCode.toString(),
                            e.message ?: ""
                        )
                    )
                }

                else -> {
                    _loginCallback?.onError(
                        error = ErrorInfo(
                            e.statusCode.toString(),
                            e.message ?: ""
                        )
                    )
                }
            }

        }
    }


}