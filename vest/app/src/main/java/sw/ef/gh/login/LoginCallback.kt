package sw.ef.gh.login

import sw.ef.gh.login.AuthorizationInfo
import sw.ef.gh.login.ErrorInfo
import sw.ef.gh.login.LoginType

/**
 *
 * @author xx
 * 2023/5/19 13:47
 */
interface LoginCallback {

    fun onSuccess(result: AuthorizationInfo, type: LoginType)

    fun onError(error: ErrorInfo)
}