package com.vest.bag.login

import com.vest.bag.login.AuthorizationInfo
import com.vest.bag.login.ErrorInfo
import com.vest.bag.login.LoginType

/**
 *
 * @author xx
 * 2023/5/19 13:47
 */
interface LoginCallback {

    fun onSuccess(result: AuthorizationInfo, type: LoginType)

    fun onError(error: ErrorInfo)
}