package sw.ef.gh.login

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import sw.ef.gh.login.LoginType.LinkedInLogin

/**
 * LinkedIn 登录授权处理
 * @author xx
 * 2023/5/22 10:04
 */
class LinkedInLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var linkedInObserver: LinkedInLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        linkedInObserver = LinkedInLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(linkedInObserver)
        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                val intent = Intent(activity, LinkedInAuthorizationActivity::class.java)
                linkedInObserver.twitterRequest.launch(intent)
            }

        }
    }

    inner class LinkedInLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //twitter配置
        lateinit var twitterRequest: ActivityResultLauncher<Intent>

        override fun onCreate(owner: LifecycleOwner) {
            twitterRequest = registry.register("activity_rq#" + "linkedin_login", owner, StartActivityForResult()) { result ->
                result.data?.let {
                    val authorizationInfo = it.getParcelableExtra<AuthorizationInfo>("authorizationInfo")
                    if (authorizationInfo != null) {
                        _loginCallback?.onSuccess(authorizationInfo, LinkedInLogin)
                        return@let
                    }
                    val errorInfo = it.getParcelableExtra<ErrorInfo>("authorizationInfo")
                    if (errorInfo != null) {
                        _loginCallback?.onError(errorInfo)
                    }

                }
            }
        }
    }
}