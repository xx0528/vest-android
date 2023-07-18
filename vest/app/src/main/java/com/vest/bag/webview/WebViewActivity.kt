package com.vest.bag.webview

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vest.bag.R
import com.vest.bag.base.BaseWebView
import com.vest.bag.base.WebViewCacheHolder
import com.vest.bag.base.WebViewListener
import com.vest.bag.utils.showToast

/**
 * @Author: xx
 * @Date: 2021/10/1 23:08
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class WebViewActivity : AppCompatActivity() {

    private val webViewContainer by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer)
    }

    private val tvTitle by lazy {
        findViewById<TextView>(R.id.tvTitle)
    }

    private val tvProgress by lazy {
        findViewById<TextView>(R.id.tvProgress)
    }

    private val url1 = "https://jebet.vip/?id=87236413"

    private val url2 = "https://www.bilibili.com/"

    private val url3 =
        "https://p26-passport.byteacctimg.com/img/user-avatar/6019f80db5be42d33c31c98adaf3fa8c~300x300.image"

    private lateinit var webView: BaseWebView

    private val webViewListener = object : WebViewListener {
        override fun onProgressChanged(webView: BaseWebView, progress: Int) {
            tvProgress.text = progress.toString()
        }

        override fun onReceivedTitle(webView: BaseWebView, title: String) {
            tvTitle.text = title
        }

        override fun onPageFinished(webView: BaseWebView, url: String) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        webView = WebViewCacheHolder.acquireWebViewInternal(this)
        webView.webViewListener = webViewListener
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webViewContainer.addView(webView, layoutParams)
        findViewById<View>(R.id.tvBack).setOnClickListener {
            onBackPressed()
        }
        findViewById<View>(R.id.btnOpenUrl1).setOnClickListener {
            webView.loadUrl(url1)
        }
        findViewById<View>(R.id.btnOpenUrl2).setOnClickListener {
            webView.loadUrl(url2)
        }
        findViewById<View>(R.id.btnOpenUrl3).setOnClickListener {
            webView.toLoadUrl(url3, "")
        }
        findViewById<View>(R.id.btnReload).setOnClickListener {
            webView.reload()
        }
        findViewById<View>(R.id.btnOpenHtml).setOnClickListener {
            webView.loadUrl("""file:/android_asset/javascript.html""")
        }
        findViewById<View>(R.id.btnCallJsByAndroid).setOnClickListener {
            val parameter = "\"xxxsss\""
            webView.evaluateJavascript(
                "javascript:callJsByAndroid(${parameter})"
            ) {
                showToast("evaluateJavascript: $it")
            }
//            webView.loadUrl("javascript:callJsByAndroid(${parameter})")
        }
        findViewById<View>(R.id.btnShowToastByAndroid).setOnClickListener {
            webView.loadUrl("javascript:showToastByAndroid()")
        }
        findViewById<View>(R.id.btnCallJsPrompt).setOnClickListener {
            webView.loadUrl("javascript:callJsPrompt()")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        if (webView.toGoBack()) {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        WebViewCacheHolder.prepareWebView()
    }

}