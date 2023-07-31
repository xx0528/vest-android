package sw.ef.gh.event

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.appsflyer.AFInAppEventParameterName
import com.facebook.FacebookSdk.getApplicationContext
import a.F
import sw.ef.gh.utils.jsonToMap
import sw.ef.gh.utils.log
import org.json.JSONObject


object AdJustTool {
    fun init(ajToken: String) {
        log( "init AdJust------------$ajToken")

        val config = AdjustConfig(F.getInstance(), ajToken, AdjustConfig.ENVIRONMENT_PRODUCTION)
        config.setLogLevel(LogLevel.VERBOSE)
        // Set attribution delegate.
        config.setOnAttributionChangedListener { attribution ->
            log("Attribution callback called!")
            log("Attribution: $attribution")
        }
        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener { eventSuccessResponseData ->
            log("Event success callback called!")
            log("Event success data: $eventSuccessResponseData")
        }
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener { eventFailureResponseData ->
            log("Event failure callback called!")
            log("Event failure data: $eventFailureResponseData")
        }

        config.setOnSessionTrackingSucceededListener { sessionSuccessResponseData ->
            log("Session success callback called!")
            log("Session success data: $sessionSuccessResponseData")
        }

        config.setOnSessionTrackingFailedListener { sessionFailureResponseData ->
            log("Session failure callback called!")
            log("Session failure data: $sessionFailureResponseData")
        }

        config.setOnDeeplinkResponseListener { deeplink ->
            log("Deferred deep link callback called!")
            log("Deep link URL: $deeplink")
            true
        }

        config.setSendInBackground(true)
        Adjust.onCreate(config)
        F.getInstance().registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
        Adjust.getGoogleAdId(
            getApplicationContext()
        ) { s -> log("Google AD ID --------- $s") }
    }

    fun onEvent(jSONObj : JSONObject) : String {
        try {
            val eventName = jSONObj.getString("eventName")
            val map = jsonToMap(jSONObj.getString("param"))
            val revenue = jSONObj.getDouble("amount")
            val currency = jSONObj.getString("currency")


            val event = AdjustEvent(eventName)

            if (revenue > 0.0) {
                map[AFInAppEventParameterName.REVENUE] = revenue
                event.setRevenue(revenue, currency);
            }
            if (map.isNotEmpty()) {
                for (paramKey in ArrayList<String>(map.keys)) {
                    event.addCallbackParameter(paramKey, map[paramKey].toString())
                }
            }
            Adjust.trackEvent(event)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }


    private class AdjustLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
    }
}
