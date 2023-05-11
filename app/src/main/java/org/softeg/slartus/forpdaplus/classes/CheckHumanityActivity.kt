package org.softeg.slartus.forpdaplus.classes

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.softeg.slartus.forpdaplus.databinding.ActivityCheckHumanityBinding
import ru.slartus.http.Http
import ru.slartus.http.Http.Companion.instance

/**
 * Created by slinkin on 18.12.13.
 */
class CheckHumanityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckHumanityBinding
    private val url = "https://4pda.to/"
    override fun onCreate(saveInstance: Bundle?) {
        super.onCreate(saveInstance)
        binding = ActivityCheckHumanityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.webView.setup()
        instance.cookieStore.clearCloudFlareCookies()

        mobileProcess()
        showHelloDialog()
    }

    private fun mobileProcess() {
        checkProcess(false) {
            desktopProcess()
        }
    }

    private fun desktopProcess() {
        checkProcess(true) {
            showSuccessDialog()
        }
    }

    private fun checkProcess(desktop: Boolean, onSuccess: () -> Unit) {
        CookieManager.getInstance().removeAllCookie()
        binding.webView.settings.userAgentString =
            if (desktop) Http.FULL_USER_AGENT else instance.userAgent
        binding.webView.webViewClient = CheckHumanityWebViewClient(desktop, onSuccess)
        binding.webView.loadUrl(url)
    }

    private fun showHelloDialog() {
        AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle("Проверка, что вы не робот")
            .setMessage(
                "Проверка отобразится дважды - для мобильной и для десктопной версии\n" +
                        "Не нажимайте ничего, кроме чекбокса подтверждения, что вы человек\n" +
                        "Экран закроется автоматически после успешной проверки"
            )
            .setPositiveButton("Понял", null)
            .create()
            .show()
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle("Проверка, что вы не робот")
            .setMessage("Проверка прошла успешно!")
            .setPositiveButton("ОК", null)
            .setOnDismissListener { finish() }
            .create()

        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false
    }

    companion object {
        @JvmStatic
        var active: Boolean = false

        @JvmStatic
        val lock: Any = Any()
    }
}


@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup() {
    settings.javaScriptEnabled = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        CookieManager.getInstance().acceptThirdPartyCookies(this)
    }
    CookieManager.getInstance().setAcceptCookie(true)
}

private class CheckHumanityWebViewClient(private val desktop: Boolean, private val checked: () -> Unit) :
    WebViewClient() {

    private val keys = listOf("cf_clearance")
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        val cookieHeader: String? = CookieManager.getInstance().getCookie(url)

        val cookies = cookieHeader?.split(";")?.associate { cookie ->
            val (key, value) = cookie.split("=")
            key.trim() to value.trim()
        } ?: emptyMap()
        cookies
            .filter { it.key in keys }
            .forEach { cookie ->
                if (desktop)
                    instance.cookieStore.addCloudFlareDesktop(cookie.key, cookie.value)
                else
                    instance.cookieStore.addCloudFlare(cookie.key, cookie.value)
            }
        if (keys.all { it in cookies.keys })
            checked()
    }
}