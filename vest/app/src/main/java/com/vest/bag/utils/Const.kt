package com.vest.bag.utils

import android.text.TextUtils
import org.json.JSONObject

object Const {
    const val LINK_URL = "http://game-config-aa.oss-us-west-1.aliyuncs.com/test"
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
    const val isOpen = "isOpen"
    const val TAG = "MJB-------"
    const val TAGAF = "MJB--AppsFly-----"
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
