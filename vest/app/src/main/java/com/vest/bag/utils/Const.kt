package com.vest.bag.utils

import android.text.TextUtils
import org.json.JSONObject

object Const {
    const val LINK_URL = "http://game-fiverr-slots.oss-ap-southeast-3.aliyuncs.com/config.json"
    const val AF_KEY = "YcQCMtATkHdwH4nHUq3igV"
    const val ADJUST_TOKEN = "xvssr72ru0ow"
    const val SP_KEY_REFERRER = "referrer"
    const val SP_KEY_AID = "aid"
    const val APP_KEY_ID = "mjb0718"
    const val AdjustToken = "AdjustToken"
    const val AFKey = "AFKey"
    const val Orientation = "Orientation"
    const val JSInterfaceName = "JSInterfaceName"
    const val URL = "url"
    const val IsOpen = "isOpen"
    const val TAG = "MJB-------"
    const val TAGAF = "MJB--AppsFly-----"

    const val LINKEDIN_AUTHURL = "https://www.linkedin.com/oauth/v2/authorization"
    const val LINKEDIN_TOKENURL = "https://www.linkedin.com/oauth/v2/accessToken"
    const val LINKEDIN_SCOPE = "r_liteprofile%20r_emailaddress"
    const val LINKEDIN_ME = "https://api.linkedin.com/v2/me"
    const val LINKEDIN_EMAIL = "https://api.linkedin.com/v2/emailAddress"

}

object JSKey {
    const val Method : String = "method"
    const val Event : String = "event"
    const val EventType : String = "eventType"
    const val EventAF : String = "af"
    const val EventAJ : String = "aj"
    const val Url : String = "url"
    const val OpenUrlWebview : String = "openUrlWebview"
    const val OpenUrlBrowser : String = "openUrlBrowser"
    const val OpenWindow : String = "openWindow"
    const val GetAppsFlyerUID : String = "getAppsFlyerUID"
    const val GetSPAID : String = "getSPAID"
    const val GetSPREFERRER : String = "getSPREFERRER"

    const val Login : String = "login"
    const val LoginType : String = "loginType"
    const val IsWaitForResult: String = "isWaitForResult"
    const val FbLogin : String = "fbLogin"
    const val FbShare : String = "fbShare"
    const val ShareTitle : String = "title"
    const val ShareLink : String = "link"
    const val ShareDetails : String = "details"
    const val GoogleLogin : String = "googleLogin"
    const val TwitterLogin : String = "twitterLogin"
    const val LinkedInLogin : String = "linkedInLogin"



}



fun jsonToMap(str: String): MutableMap<String, Any> {
    val hashMap: HashMap<String, Any> = HashMap()
    try {
        if (!TextUtils.isEmpty(str)) {
            val jSONObject = JSONObject(str)
            val keys = jSONObject.keys()
            while (keys.hasNext()) {
                val next = keys.next()
                hashMap[next] = jSONObject[next]
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return hashMap
}
