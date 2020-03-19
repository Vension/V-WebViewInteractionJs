package kv.android.webInteractionJs.app

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
        initListener()
    }

    private fun initWebView() {
        webView = WebView(this).apply {
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                blockNetworkImage = false
            }

            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            addJavascriptInterface(JsObject(),"android")
            loadUrl("file:///android_asset/test.html")
            layoutWeb.addView(this)
        }
    }

    private fun initListener() {
        text_java_js.setOnClickListener {
            webView?.loadUrl("javascript:javaToCallback('我来自Java')")
        }
        text_java_evaJs.setOnClickListener {
            webView?.evaluateJavascript("javascript:javaToJsWith('我来自Java')"){
                text_show.text = it
            }
        }
    }

    fun setTextShow(str: String){
        text_show.text = str
    }

    override fun onDestroy() {
        if (webView != null) {
            webView?.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null)
            webView?.tag = null
            webView?.clearHistory()
            (webView?.parent as ViewGroup).removeView(webView)
            webView?.destroy()
            webView = null
        }
        super.onDestroy()
    }

    inner class JsObject {
        @JavascriptInterface
        fun JsToJavaInterface(msg : String){
            runOnUiThread {
                setTextShow("from JavaInterface: $msg")
            }
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val uri = Uri.parse(url)
            //一般根据scheme(协议格式) & authority(协议名) 判断
            // url = "js://jstojava?arg1=1&arg2=2"
            if (uri.scheme == "js") {
                if (uri.authority == "jsToJava") {
                    val param1 = uri.getQueryParameter("arg1")
                    val param2 = uri.getQueryParameter("arg2")
                    setTextShow("arg1=$param1,arg2=$param2")
                }
                return true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    inner class MyWebChromeClient : WebChromeClient(){
        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            val uri = Uri.parse(message)
            if (uri.scheme.equals("js")){
                if (uri.authority.equals("jsToJava")){
                    val param3 = uri.getQueryParameter("arg3")
                    val param4 = uri.getQueryParameter("arg4")
                    setTextShow("arg3=$param3,arg4=$param4")
                    result?.confirm("我来自onJsPrompt")
                }
            }
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }
    }
}
