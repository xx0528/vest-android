package com.vest.bag.base

import android.app.Application
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import com.vest.bag.utils.log
import java.util.*

/**
 * @Author: xx
 * @Date: 2021/10/4 18:57
 * @Desc:
 * @公众号：字节数组
 */
object WebViewCacheHolder {

    private val webViewCacheStack = Stack<BaseWebView>()

    private const val CACHED_WEB_VIEW_MAX_NUM = 4

    private lateinit var application: Application

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(application: Application) {
        WebViewCacheHolder.application = application
        prepareWebView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun prepareWebView() {
        if (webViewCacheStack.size < CACHED_WEB_VIEW_MAX_NUM) {
            Looper.myQueue().addIdleHandler {
                log("WebViewCacheStack Size: " + webViewCacheStack.size)
                if (webViewCacheStack.size < CACHED_WEB_VIEW_MAX_NUM) {
                    webViewCacheStack.push(createWebView(MutableContextWrapper(application)))
                }
                false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acquireWebViewInternal(context: Context): BaseWebView {
        if (webViewCacheStack.isEmpty()) {
            return createWebView(context)
        }
        val webView = webViewCacheStack.pop()
        val contextWrapper = webView.context as MutableContextWrapper
        contextWrapper.baseContext = context
        return webView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createWebView(context: Context): BaseWebView {
        return BaseWebView(context)
    }

}