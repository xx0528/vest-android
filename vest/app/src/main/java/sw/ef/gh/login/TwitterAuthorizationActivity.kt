package sw.ef.gh.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kf.ab.cd.R
import sw.ef.gh.utils.getFirstAndLastName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

/**
 *
 * https://developer.twitter.com/zh-cn/docs/authentication/overview
 * @author xx
 * 2023/5/18 16:43
 */
class TwitterAuthorizationActivity : AppCompatActivity() {
    private val wvLogin: WebView by lazy {
        findViewById(R.id.wv_login)
    }
    lateinit var twitter: Twitter
    var accToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
        setupViews()
        initSDK()
    }

    private fun initSDK() {
        getRequestToken()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupViews() {
        wvLogin.apply {
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = TwitterWebViewClient()
        }
        wvLogin.settings.javaScriptEnabled = true
    }

    private fun getRequestToken() {
        lifecycleScope.launch(Dispatchers.Default) {
            val builder = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(getString(R.string.twitter_consumer_key))
                .setOAuthConsumerSecret(getString(R.string.twitter_consumer_secret))
                .setIncludeEmailEnabled(true)
            val config = builder.build()
            val factory = TwitterFactory(config)
            twitter = factory.instance
            try {
                val requestToken = twitter.oAuthRequestToken
                withContext(Dispatchers.Main) {
                    setupTwitterWebView(requestToken.authorizationURL)
                }
            } catch (e: Exception) {
                Log.e("ERROR: ", e.toString())
            }
        }
    }

    private fun setupTwitterWebView(url: String) {
        wvLogin.loadUrl(url)
    }

    inner class TwitterWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.toString().startsWith(getString(R.string.login_redirect_url))) {
                handleUrl(request.url.toString())
                return true
            }
            return false
        }

        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)
            val oauthVerifier = uri.getQueryParameter("oauth_verifier") ?: ""
            lifecycleScope.launch(Dispatchers.Main) {
                accToken =
                    withContext(Dispatchers.IO) { twitter.getOAuthAccessToken(oauthVerifier) }
                getUserProfile()
            }
        }
    }

    suspend fun getUserProfile() {
        var usr: User? = null
        try {
            usr = withContext(Dispatchers.IO) { twitter.verifyCredentials() }
        } catch (e: TwitterException) {
            val intent = Intent()
            val errorInfo = ErrorInfo(e.errorCode.toString(), e.errorMessage)
            intent.putExtra("errorInfo", errorInfo)
            setResult(RESULT_OK, intent)
            finish()
        }
        usr?.let {
            val intent = Intent()
            val names = usr.name?.getFirstAndLastName() ?: Pair("", "")
            val authorizationInfo = AuthorizationInfo(usr.id.toString(), usr.email ?: "", names.first, names.second)
            intent.putExtra("authorizationInfo", authorizationInfo)
            setResult(RESULT_OK, intent)
            finish()
        }
    }


}