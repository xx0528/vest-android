package com.vest.bag

import android.app.Application
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import org.json.JSONObject
import java.io.IOException

class MainApplication : Application(){

    private lateinit var data: JSONObject

    companion object {
        private lateinit var instance: MainApplication

        fun getInstance(): MainApplication {
            return instance
        }
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun setData(data: JSONObject) {
        this.data = data
    }

    fun getData(): JSONObject {
        return this.data
    }

    fun getGoogleAdId(): String? {
        try {
            val info: AdvertisingIdClient.Info = AdvertisingIdClient.getAdvertisingIdInfo(this)
            return info.id
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        }
        return ""
    }
}