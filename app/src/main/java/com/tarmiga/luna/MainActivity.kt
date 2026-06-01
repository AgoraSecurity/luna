package com.tarmiga.luna

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarmiga.luna.data.LunaDatabase
import com.tarmiga.luna.ui.onboarding.OnboardingScreen
import com.tarmiga.luna.ui.onboarding.OnboardingViewModel
import com.tarmiga.luna.ui.theme.LunaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        val database = LunaDatabase.getDatabase(this)
        val prefs = getSharedPreferences("luna_prefs", Context.MODE_PRIVATE)

        setContent {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.provideFactory(database, prefs)
            )
            val onboardingCompleted = remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }

            LunaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!onboardingCompleted.value) {
                        OnboardingScreen(
                            viewModel = onboardingViewModel,
                            onFinished = {
                                onboardingCompleted.value = true
                            }
                        )
                    } else {
                        WebViewScreen(
                            url = "file:///android_asset/index.html",
                            modifier = Modifier.padding(innerPadding),
                            database = database,
                            prefs = prefs,
                            notificationHelper = notificationHelper
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(
    url: String,
    modifier: Modifier = Modifier,
    database: LunaDatabase,
    prefs: android.content.SharedPreferences,
    notificationHelper: NotificationHelper
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                // Add the native bridge
                addJavascriptInterface(
                    LunaBridge(database.cycleDao(), prefs, notificationHelper),
                    "LunaNative"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        canGoBack = view?.canGoBack() ?: false
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("LunaJS", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                        return true
                    }
                }

                loadUrl(url)
                webView = this
            }
        },
        update = {
            webView = it
        },
        modifier = modifier.fillMaxSize()
    )
}
