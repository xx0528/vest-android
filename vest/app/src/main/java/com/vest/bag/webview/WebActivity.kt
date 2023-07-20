package com.vest.bag.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.vest.bag.MainApplication
import com.vest.bag.R
import com.vest.bag.login.FbLogin
import com.vest.bag.login.LoginTool
import com.vest.bag.utils.log
import com.vest.bag.utils.setDirection
import com.vest.bag.utils.setFullWindow
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * @Author: xx
 * @Date: 2021/10/1 23:08
 * @Desc:
 */
class WebActivity : AppCompatActivity() {


    private var uploadMessage: ValueCallback<Uri>? = null
    var uploadMessageAboveL: ValueCallback<Array<Uri?>?>? = null

    private val fileChooseResultCode = 10000

    lateinit var content: FrameLayout
    lateinit var mWebView: WebView
    private var popWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullWindow(this)
        val orientation = MainApplication.getInstance().getData().getString("orientation")
        if (!orientation.isNullOrEmpty()) {
            setDirection(this, orientation)
        }
        setContentView(R.layout.activity_web)

        content = findViewById(R.id.rootLayout)
        mWebView = findViewById(R.id.webView)

        initSetting(mWebView)
        initJsInterface(mWebView)

        val url = intent.getStringExtra(Intent.ACTION_ATTACH_DATA)
        if (url.isNullOrEmpty())
            return

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        } else {
            mWebView.loadUrl(url)
        }

        val webClient = NewWebViewClient()
        webClient.setHandle(this)
        mWebView.webViewClient = webClient
        mWebView.webChromeClient = WebViewChromeClient()

        LoginTool.initOnCreate(this)

        FbLogin.getInstance().initFbLogin(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initSetting(webView: WebView) {
        val webSettings = webView.settings
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.javaScriptEnabled = true
        //设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true //将图片调整到适合webview的大小
        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true) // 支持缩放，默认为true。是下面那个的前提。
        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.displayZoomControls = false //隐藏原生的缩放控件

        //其他细节操作
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT //缓存相关
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.databaseEnabled = true
        webSettings.allowFileAccess = true //设置可以访问文件
        webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSettings.loadsImagesAutomatically = false //支持自动加载图片
        webSettings.defaultTextEncodingName = "utf-8"//设置编码格式
        webSettings.setSupportMultipleWindows(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webSettings.userAgentString = webSettings.userAgentString.replace("; wv".toRegex(), "")
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    private fun initJsInterface(webView: WebView) {
        val jsInterfaceStr = MainApplication.getInstance().getData().getString("jsInterface")
        if (jsInterfaceStr.isEmpty()) {
            return
        }

        val nameList = JSONArray(jsInterfaceStr)
        for (i in 0 until nameList.length()) {
            val interfaceName = nameList[i].toString()
            if (interfaceName.isNotEmpty()) {
                log("加入接口---$interfaceName")
                mWebView.addJavascriptInterface(JsInterface(this), interfaceName)
            }
        }
    }

    fun addJs() {
        val jsCodeStr = MainApplication.getInstance().getData().getString("jsCode")
        if (jsCodeStr.isNullOrEmpty())
            return
        val jsCode = JSONArray(jsCodeStr)
        for (i in 0 until jsCode.length()) {
            val codeStr = jsCode[i].toString()
            if (codeStr.isEmpty())
                continue
            mWebView.post {
                mWebView.evaluateJavascript(codeStr, null)
            }
        }
    }


    //文件选择回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        FbLogin.getInstance().onActResult(requestCode, resultCode, data);

        if (requestCode == fileChooseResultCode) { //处理返回的图片，并进行上传
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result = if (data == null || resultCode != RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
            return
        }

        val resultDataStr =
            MainApplication.getInstance().getData().getString("onActivityResultCode")
        if (resultDataStr.isNullOrEmpty())
            return
        val obj = JSONObject(resultDataStr)
        if (obj.getString("jsCode").isNotEmpty()) {
            if (resultCode == obj.getInt("resultCode")) {
                if (requestCode == obj.getInt("requestCode")) {
                    mWebView.evaluateJavascript(obj.getString("jsCode")) { }
                }
            }
        }
    }

    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != fileChooseResultCode) return
        var results: Array<Uri?>? = null
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOfNulls(clipData.itemCount)
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results[i] = item.uri
                    }
                }
                if (dataString != null) results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL?.onReceiveValue(results)
        uploadMessageAboveL = null
    }

    private fun loadUrl(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        } else {
            mWebView.loadUrl(url)
        }
    }

    fun getWebSetting(): WebSettings {
        return mWebView.settings
    }

    fun closeActivity(result: Int) {
        mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        if (result > 0) { // 异常关闭清空所有缓存
            mWebView.clearCache(true)
            mWebView.clearHistory()
            mWebView.clearFormData()
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 退出时先清理webview，防止内存泄漏
        destoryWebView(mWebView)
        System.gc()
    }

    private fun destoryWebView(webView: WebView) {
        try {
            if (webView.parent != null) {
                (webView.parent as FrameLayout).removeView(webView)
            }
            webView.removeAllViews()
            webView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (popWebView == null) {
                    if (mWebView.canGoBack()) {
                        Log.e("back", "webview back")
                        mWebView.goBack()
                    } else {
                        Log.e("back", "webview close")
                        super.onBackPressed()
                    }
                } else {
                    if (popWebView!!.canGoBack()) {
                        Log.e("back", "popWebView back")
                        popWebView!!.goBack()
                        //这里不要删，有的网页返回两次才行
                        if (!popWebView!!.canGoBack()) {
                            Log.e("back", "popWebView close -- window.closeGame")
                            mWebView.loadUrl("javascript:window.closeGame();")
                            (popWebView!!.parent as ViewGroup).removeView(popWebView)
                            popWebView!!.stopLoading()
                            popWebView = null
                        }
                    } else {
                        Log.e("back", "popWebView close -- window.closeGame")
                        mWebView.loadUrl("javascript:window.closeGame();")
                        (popWebView!!.parent as ViewGroup).removeView(popWebView)
                        popWebView!!.stopLoading()
                        popWebView = null
                    }
                }
                true
            }

            else -> super.onKeyUp(keyCode, event)
        }
    }


    fun openUrlBrowser(str: String?) {
        try {
            if (TextUtils.isEmpty(str)) {
                return
            }
            startActivity(
                Intent.parseUri(str, Intent.URI_INTENT_SCHEME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun openUrlWebView(str: String?) {
        try {
            if (TextUtils.isEmpty(str)) {
                return
            }
            startActivity(
                Intent(this, WebActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Intent.ACTION_ATTACH_DATA, str)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    inner class WebViewChromeClient : WebChromeClient() {
        override fun onCloseWindow(window: WebView?) {
            if (popWebView != null) {
                mWebView.loadUrl("javascript:window.closeGame();")
                (popWebView!!.parent as ViewGroup).removeView(popWebView)
                popWebView!!.stopLoading()
                popWebView = null
            }
            super.onCloseWindow(window)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.e(
                "onConsoleMessage",
                String.format(
                    "%s:%d\t%s",
                    consoleMessage.sourceId(),
                    consoleMessage.lineNumber(),
                    consoleMessage.message()
                )
            )
            return super.onConsoleMessage(consoleMessage)
        }

        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            Log.e("window create", "window create!!")

            popWebView = WebView(view!!.context)
            initSetting(popWebView!!)
            val webClient = NewWebViewClient()
            popWebView!!.webViewClient = webClient
            val transport = resultMsg!!.obj as WebView.WebViewTransport
            transport.webView = popWebView
            resultMsg.sendToTarget()
            content.addView(popWebView)
            Log.e("createWindow", "createWindow")
            return true
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
        }

        // 文件选择
        override fun onShowFileChooser(
            webView: WebView?,
            valueCallback: ValueCallback<Array<Uri?>?>,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            uploadMessageAboveL = valueCallback;

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "File Chooser"),
                fileChooseResultCode
            );
            return true
        }
    }

    inner class NewWebViewClient : WebViewClient() {
        private var mHandle: WebActivity? = null

        fun setHandle(handle: WebActivity?) {
            mHandle = handle
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("webLog_PageFinished ", " $url");
            if (mHandle != null && !mHandle!!.getWebSetting().loadsImagesAutomatically)
                mHandle!!.getWebSetting().loadsImagesAutomatically = true;
            addJs()
        }

        // WEB请求收到服务器的错误消息时回调
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.d(
                "webLog_error: Http ",
                " code:" + errorResponse.statusCode + " request:" + request
            )
            if (request.isForMainFrame) {
                mHandle?.closeActivity(1)
            }
        }

        override fun onReceivedSslError(
            webView: WebView?,
            sslErrorHandler: SslErrorHandler,
            sslError: SslError
        ) {
            var message = "SSL Certificate error."
            when (sslError.primaryError) {
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
            }
            message += " Do you want to continue anyway?"
//            onShowSSLError.invoke(message, sslErrorHandler)
        }
    }

}