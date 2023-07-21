package com.vest.bag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.vest.bag.event.*
import com.vest.bag.utils.*
import com.vest.bag.webview.WebActivity
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    var imageViewBg: ImageView? = null
    var imageViewLogo: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullWindow(this)
        setContentView(R.layout.activity_main)

        this.imageViewBg = findViewById(R.id.img_bg_id)
        this.imageViewLogo = findViewById(R.id.img_logo_id)

        checkOpen(Const.LINK_URL)
    }

    private fun checkOpen(connectURL: String) {
        HttpUtil.sendGetRequest(
            connectURL,
            object : HttpCallbackListener {
                override fun onFinish(response: ByteArray?) {
                    val json = String(response!!, StandardCharsets.UTF_8)
                    log(json)
                    val mjbData = JSONObject(json)

                    if (!mjbData.getBoolean("isOpen")) {
                        log("not open 。。")
                        openGame()
                        return
                    }

                    if (mjbData.getString("url").isNullOrEmpty()) {
                        log("no url -- 。。")
                        openGame()
                        return
                    }

                    MainApplication.getInstance().setData(mjbData)
                    openWeb(mjbData)
                    return
                }

                override fun onError(e: Exception?) {
                    Log.e(Const.TAG, e!!.message!!)
                    openGame()
                }
            })
    }

    fun openGame() {

    }

    fun openWeb(mjbData: JSONObject) {

        log("afkey --- " + mjbData.getString("afKey"))
        if (!mjbData.getString("afKey").isNullOrEmpty()) {
            AppsFlyTool.init(mjbData.getString("afKey"))
        }

        log("ajToken " + mjbData.getString("ajToken"))
        if (!mjbData.getString("ajToken").isNullOrEmpty()) {
            AdJustTool.init(mjbData.getString("ajToken"))
        }

        val intent = Intent(this, WebActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.ACTION_ATTACH_DATA, mjbData.getString("url"))
        startActivity(intent)
    }
}