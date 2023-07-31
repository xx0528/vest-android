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
import kf.ab.cd.R
import sw.ef.gh.utils.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


/**
 *
 * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin
 * @author xx
 * 2023/5/19 13:38
 */
class LinkedInAuthorizationActivity : AppCompatActivity() {
    private val wvLogin: WebView by lazy {
        findViewById(R.id.wv_login)
    }

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
            webViewClient = LinkedInWebViewClient()
        }
        wvLogin.settings.javaScriptEnabled = true
    }

    private fun getRequestToken() {
        val state = "linkedin" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val authUrl =
            Const.LINKEDIN_AUTHURL + "?response_type=code&client_id=" + getString(R.string.linkedin_client_id) + "&scope=" + Const.LINKEDIN_SCOPE + "&state=" + state + "&redirect_uri=" + getString(
                R.string.login_redirect_url
            )
        wvLogin.loadUrl(authUrl)

    }

    inner class LinkedInWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.toString().startsWith(getString(R.string.login_redirect_url))) {
                handleUrl(request.url.toString())
                return true
            }
            return false
        }

        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)
            if (url.contains("code")) {
                val linkedinCode = uri.getQueryParameter("code") ?: ""
                linkedInRequestForAccessToken(linkedinCode)
            }
        }
    }

    private fun linkedInRequestForAccessToken(linkedinCode: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val grantType = "authorization_code"
            val postParams =
                "grant_type=" + grantType + "&code=" + linkedinCode + "&redirect_uri=" + getString(R.string.login_redirect_url) +
                        "&client_id=" + getString(R.string.linkedin_client_id) + "&client_secret=" + getString(R.string.linkedin_client_secret)
            val url = URL(Const.LINKEDIN_TOKENURL)
            val httpsURLConnection = withContext(Dispatchers.IO) { url.openConnection() as HttpsURLConnection }
            httpsURLConnection.apply {
                requestMethod = "POST"
                setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"

                )
                doInput = true
                doOutput = true
            }
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            withContext(Dispatchers.IO) {
                outputStreamWriter.write(postParams)
                outputStreamWriter.flush()
            }
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }
            val jsonObject = JSONObject(response)
            val accessToken = jsonObject.getString("access_token")
            val authorizationInfo = withContext(Dispatchers.IO) {
                (accessToken)
                val baseAuthorizationInfo = async { requestUserInfo(accessToken) }
                val emailInfo = async { requestUserEmail(accessToken) }
                val baseAuthorization = baseAuthorizationInfo.await()
                val email = emailInfo.await()
                baseAuthorization.email = email
                baseAuthorization
            }
            withContext(Dispatchers.Main) {
                val intent = Intent()
                intent.putExtra("authorizationInfo", authorizationInfo)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun requestUserEmail(accessToken: String): String {
        val url =
            URL("${Const.LINKEDIN_EMAIL}?q=members&projection=(elements*(handle~))&oauth2_access_token=$accessToken")
        val httpsURLConnection = url.openConnection() as HttpsURLConnection
        httpsURLConnection.apply {
            requestMethod = "GET"
            doInput = true
            doOutput = false
        }
        val response = httpsURLConnection.inputStream.bufferedReader().use { it.readText() }
        Log.d("email:", JSONObject(response).toString())
        return getUserEmail(JSONObject(response))

    }

    private fun requestUserInfo(accessToken: String): AuthorizationInfo {
        val url =
            URL("${Const.LINKEDIN_ME}?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token=$accessToken")
        val httpsURLConnection = url.openConnection() as HttpsURLConnection
        httpsURLConnection.apply {
            requestMethod = "GET"
            doInput = true
            doOutput = false
        }
        val response = httpsURLConnection.inputStream.bufferedReader()
            .use { it.readText() }

        val userInfoObject = JSONObject(response)
        return getUserBaseInfo(userInfoObject)
    }

    private fun getUserBaseInfo(userInfoObject: JSONObject): AuthorizationInfo {
        val id = getUserId(userInfoObject)
        val firstName = if (userInfoObject.has("localizedFirstName")) {
            userInfoObject.getString("localizedFirstName")
        } else {
            getUserName(userInfoObject, "firstName")
        }
        val lastName = if (userInfoObject.has("localizedLastName")) {
            userInfoObject.getString("localizedLastName")
        } else {
            getUserName(userInfoObject, "lastName")
        }

        return AuthorizationInfo(id, "", firstName, lastName)
    }

    private fun getUserName(userInfoObject: JSONObject, nameKey: String): String {
        val firstName: String
        val firstNameObj = userInfoObject.getJSONObject(nameKey)
        val localizedObj = firstNameObj.getJSONObject("localized")
        val preferredLocaleObj = firstNameObj.getJSONObject("preferredLocale")
        firstName = localizedObj.getString(preferredLocaleObj.getString("language") + "_" + preferredLocaleObj.getString("country"))
        return firstName
    }

    private fun getUserId(userInfoObject: JSONObject): String {
        handleIsErrorInfo(userInfoObject)
        return if (userInfoObject.has("id")) {
            userInfoObject.getString("id")
        } else {
            ""
        }
    }

    private fun getUserEmail(userEmailObject: JSONObject): String {
        handleIsErrorInfo(userEmailObject)
        return userEmailObject.getJSONArray("elements").getJSONObject(0)?.getJSONObject("handle~")?.getString("emailAddress") ?: ""
    }

    /**
     * 参考官方文档处理错误信息
     * https://learn.microsoft.com/en-us/linkedin/shared/api-guide/concepts/error-handling?context=linkedin%2Fcontext
     */
    private fun handleIsErrorInfo(errorObject: JSONObject) {
        val errStatus = errorObject.getString("status") ?: ""
        if (errStatus.isNotEmpty() && !(errStatus.startsWith("2"))) {//有返回状态码且不是200
            val intent = Intent()
            val errorInfo = ErrorInfo(errStatus, errorObject.getString("message") ?: "")
            intent.putExtra("errorInfo", errorInfo)
            setResult(RESULT_OK, intent)
            finish()
        }
    }


}