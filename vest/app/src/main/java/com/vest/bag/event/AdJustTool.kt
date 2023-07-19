package com.vest.bag.event

import android.text.TextUtils
import android.util.Log
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.vest.bag.MainApplication
import com.vest.bag.utils.Const
import com.vest.bag.utils.jsonToMap
import com.vest.bag.utils.log
import org.json.JSONObject


object AdJustTool {
    fun init(ajToken: String) {
        Log.i(Const.TAG, "init AdJust------------$ajToken")
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onEvent(jSONObj : JSONObject) : String {
        try {
            val eventName = jSONObj.getString("eventName")
            val map = jsonToMap(jSONObj.getString("param"))
            val revenue = jSONObj.getDouble("amount")
            val currency = jSONObj.getString("currency")

            if (!TextUtils.isEmpty(currency)) {
                map[AFInAppEventParameterName.CURRENCY] = currency
                map[AFInAppEventParameterName.PURCHASE_CURRENCY] = currency
            }
            if (revenue > 0.0) {
                map[AFInAppEventParameterName.REVENUE] = revenue
            }

            log("event = $eventName  map = $jSONObj")

            AppsFlyerLib.getInstance().logEvent(MainApplication.getInstance(), eventName, map, object :
                AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.i(Const.TAGAF, "Event sent successfully")
                }

                override fun onError(errorCode: Int, errorDesc: String) {
                    Log.i(
                        Const.TAGAF, "Event failed to be sent:\n" +
                                "Error code: " + errorCode + "\n"
                                + "Error description: " + errorDesc
                    )
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
