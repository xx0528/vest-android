package com.vest.bag.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.vest.bag.utils.log
import com.vest.bag.webview.JsInterface
import java.io.File

/**
 * @Author: xx
 * @Date: 2021/9/20 22:45
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
interface WebViewListener {

    fun onProgressChanged(webView: BaseWebView, progress: Int) {

    }

    fun onReceivedTitle(webView: BaseWebView, title: String) {

    }

    fun onPageFinished(webView: BaseWebView, url: String) {

    }

}

@RequiresApi(Build.VERSION_CODES.O)
class BaseWebView(context: Context, attributeSet: AttributeSet? = null) :
    WebView(context, attributeSet) {

    private val baseCacheDir by lazy {
        File(context.cacheDir, "webView")
    }

    private val databaseCachePath by lazy {
        File(baseCacheDir, "databaseCache").absolutePath
    }

    private val appCachePath by lazy {
        File(baseCacheDir, "appCache").absolutePath
    }

    var hostLifecycleOwner: LifecycleOwner? = null

    var webViewListener: WebViewListener? = null

    private val mWebChromeClient = object : WebChromeClient() {

        override fun onProgressChanged(webView: WebView, newProgress: Int) {
            super.onProgressChanged(webView, newProgress)
            log("onProgressChanged-$newProgress")
            webViewListener?.onProgressChanged(this@BaseWebView, newProgress)
        }

        override fun onReceivedTitle(webView: WebView, title: String?) {
            super.onReceivedTitle(webView, title)
            log("onReceivedTitle-$title")
            webViewListener?.onReceivedTitle(this@BaseWebView, title ?: "")
        }

        override fun onJsAlert(
            webView: WebView,
            url: String?,
            message: String?,
            result: JsResult
        ): Boolean {
            log("onJsAlert: $webView $message")
            return super.onJsAlert(webView, url, message, result)
        }

        override fun onJsConfirm(
            webView: WebView,
            url: String?,
            message: String?,
            result: JsResult
        ): Boolean {
            log("onJsConfirm: $url $message")
            return super.onJsConfirm(webView, url, message, result)
        }

        override fun onJsPrompt(
            webView: WebView,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            log("onJsPrompt: $url $message $defaultValue")
            return super.onJsPrompt(webView, url, message, defaultValue, result)
        }
    }

    private val mWebViewClient = object : WebViewClient() {

        private var startTime = 0L

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return true
        }

        override fun onPageStarted(webView: WebView, url: String?, favicon: Bitmap?) {
            super.onPageStarted(webView, url, favicon)
            startTime = System.currentTimeMillis()
        }

        override fun onPageFinished(webView: WebView, url: String?) {
            super.onPageFinished(webView, url)
            log("onPageFinished-$url")
            webViewListener?.onPageFinished(this@BaseWebView, url ?: "")
            log("onPageFinished duration： " + (System.currentTimeMillis() - startTime))
        }

        override fun onReceivedSslError(
            webView: WebView,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            log("onReceivedSslError-$error")
            super.onReceivedSslError(webView, handler, error)
        }

    }

    init {
        webViewClient = mWebViewClient
        webChromeClient = mWebChromeClient
        initWebViewSettings(this)
        addJavascriptInterface(JsInterface(), "android")
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            log(
                "setDownloadListener: $url \n" +
                        "$userAgent \n " +
                        " $contentDisposition \n" +
                        " $mimetype \n" +
                        " $contentLength"
            )
        }
    }

    fun toLoadUrl(url: String, cookie: String) {
        val mCookieManager = CookieManager.getInstance()
        mCookieManager?.setCookie(url, cookie)
        mCookieManager?.flush()
        loadUrl(url)
    }

    fun toGoBack(): Boolean {
        if (canGoBack()) {
            goBack()
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(webView: WebView) {
        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadsImagesAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.setSupportMultipleWindows(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        settings.setUserAgentString(settings.userAgentString.replace("; wv".toRegex(), ""))
        settings.allowContentAccess = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.safeBrowsingEnabled = false

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        log("onAttachedToWindow : $context")
        (hostLifecycleOwner ?: findLifecycleOwner(context))?.let {
            addHostLifecycleObserver(it)
        }
    }

    private fun findLifecycleOwner(context: Context): LifecycleOwner? {
        if (context is LifecycleOwner) {
            return context
        }
        if (context is MutableContextWrapper) {
            val baseContext = context.baseContext
            if (baseContext is LifecycleOwner) {
                return baseContext
            }
        }
        return null
    }

    private fun addHostLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        log("addLifecycleObserver")
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                onHostResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                onHostPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onHostDestroy()
            }
        })
    }

    private fun onHostResume() {
        log("onHostResume")
        onResume()
    }

    private fun onHostPause() {
        log("onHostPause")
        onPause()
    }

    private fun onHostDestroy() {
        log("onHostDestroy")
        release()
    }

    private fun release() {
        hostLifecycleOwner = null
        webViewListener = null
        webChromeClient = null
        (parent as? ViewGroup)?.removeView(this)
        destroy()
    }

}