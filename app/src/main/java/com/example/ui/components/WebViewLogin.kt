package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewLogin(
    provider: String, // "OpenAI", "Anthropic", or "Ollama"
    onDismiss: () -> Unit,
    onTokenCaptured: (provider: String, token: String, cookies: String, userAgent: String, userId: String) -> Unit
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val tokenCaptured = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var isFreshLogin by remember { mutableStateOf(true) }
    var currentUrl by remember {
        mutableStateOf(
            when (provider) {
                "Anthropic" -> "https://claude.ai/login"
                "Ollama" -> "https://ollama.com/settings"
                else -> "https://chatgpt.com/"
            }
        )
    }

    val titleLoginClaude = stringResource(R.string.inicia_sesion_claude)
    val titleLoginOllama = stringResource(R.string.inicia_sesion_ollama)
    val titleLoginChatgpt = stringResource(R.string.inicia_sesion_chatgpt)
    val subtitleLogin = stringResource(R.string.inicia_sesion_subtitulo)
    val cdCerrar = stringResource(R.string.cd_cerrar)
    val cdRecargar = stringResource(R.string.cd_recargar)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (provider) {
                                "Anthropic" -> titleLoginClaude
                                "Ollama" -> titleLoginOllama
                                else -> titleLoginChatgpt
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitleLogin,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = cdCerrar)
                    }
                },
                actions = {
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = cdRecargar)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewInstance = this
                        
                        // Enable cookie manager
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        
                        // Set standard Chrome-like desktop or mobile User-Agent to avoid issues
                        val standardUserAgent = "Mozilla/5.0 (Linux; Android 13; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                        settings.userAgentString = standardUserAgent

                        // Set javascript bridge interface
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onTokenFound(token: String, userAgent: String) {
                                post {
                                    val cookies = CookieManager.getInstance().getCookie("https://chatgpt.com") ?: ""
                                    if (token.trim().isNotEmpty() && cookies.trim().isNotEmpty()) {
                                        if (tokenCaptured.compareAndSet(false, true)) {
                                            onTokenCaptured("OpenAI", token, cookies, userAgent, "")
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onClaudeAuthFound(orgId: String) {
                                post {
                                    val cookies = CookieManager.getInstance().getCookie("https://claude.ai") ?: ""
                                    val sessionKeyRegex = "sessionKey=([^;\\s]+)".toRegex()
                                    val match = sessionKeyRegex.find(cookies)
                                    val sessionKey = match?.groupValues?.get(1) ?: ""

                                    if (sessionKey.isNotEmpty() && orgId.isNotEmpty()) {
                                        if (tokenCaptured.compareAndSet(false, true)) {
                                            onTokenCaptured("Anthropic", sessionKey, cookies, settings.userAgentString ?: "", orgId)
                                        }
                                    }
                                }
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoadingPage = true
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoadingPage = false
                                
                                if (provider == "Anthropic") {
                                    val jsInjection = """
                                        (function() {
                                            if (!window.claudePolling) {
                                                window.claudePolling = setInterval(function() {
                                                    if (window.AndroidBridge) {
                                                        const fetchFn = window.fetch || window.originalFetch;
                                                        if (fetchFn) {
                                                            fetchFn('/api/organizations')
                                                                .then(r => r.json())
                                                                .then(data => {
                                                                    if (data && data.length > 0 && data[0].uuid) {
                                                                        window.AndroidBridge.onClaudeAuthFound(data[0].uuid);
                                                                    }
                                                                })
                                                                .catch(err => console.log('Claude org fetch error:', err));
                                                        }
                                                    }
                                                }, 2000);
                                            }
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(jsInjection, null)
                                } else {
                                    // Inject session extraction and AJAX hook for ChatGPT
                                    val jsInjection = """
                                        (function() {
                                            // 1. Overriding Fetch API to catch tokens
                                            if (!window.originalFetch) {
                                                window.originalFetch = window.fetch;
                                                window.fetch = async function(...args) {
                                                    const url = args[0];
                                                    const options = args[1] || {};
                                                    let authHeader = null;
                                                    if (options.headers) {
                                                        if (options.headers instanceof Headers) {
                                                            authHeader = options.headers.get('authorization');
                                                        } else if (typeof options.headers === 'object') {
                                                            authHeader = options.headers['authorization'] || options.headers['Authorization'];
                                                        }
                                                    }
                                                    if (authHeader && authHeader.startsWith('Bearer ')) {
                                                        if (window.AndroidBridge) {
                                                            window.AndroidBridge.onTokenFound(authHeader, window.navigator.userAgent);
                                                        }
                                                    }
                                                    return window.originalFetch.apply(this, args);
                                                };
                                            }

                                            // 2. Overriding XMLHttpRequest to catch tokens
                                            if (!window.originalXhrSend) {
                                                const originalOpen = XMLHttpRequest.prototype.open;
                                                XMLHttpRequest.prototype.open = function(method, url, ...args) {
                                                    this._url = url;
                                                    return originalOpen.apply(this, [method, url, ...args]);
                                                };
                                                
                                                const originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
                                                XMLHttpRequest.prototype.setRequestHeader = function(header, value) {
                                                    if (header.toLowerCase() === 'authorization' && value.startsWith('Bearer ')) {
                                                        if (window.AndroidBridge) {
                                                            window.AndroidBridge.onTokenFound(value, window.navigator.userAgent);
                                                        }
                                                    }
                                                    return originalSetRequestHeader.apply(this, [header, value]);
                                                };
                                            }

                                            // 3. Polling next-auth session endpoint
                                            if (!window.sessionPollingInterval) {
                                                window.sessionPollingInterval = setInterval(function() {
                                                    if (window.AndroidBridge) {
                                                        window.originalFetch('/api/auth/session')
                                                            .then(r => r.json())
                                                            .then(data => {
                                                                if (data && data.accessToken) {
                                                                    window.AndroidBridge.onTokenFound(data.accessToken, window.navigator.userAgent);
                                                                }
                                                            })
                                                            .catch(err => console.log('Session fetch error:', err));
                                                    }
                                                }, 2000);
                                            }
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(jsInjection, null)
                                }

                                if (provider == "Ollama") {
                                    val cookies = CookieManager.getInstance().getCookie("https://ollama.com") ?: ""
                                    if (cookies.contains("session=") && url?.contains("/settings") == true) {
                                        post {
                                            if (tokenCaptured.compareAndSet(false, true)) {
                                                onTokenCaptured("Ollama", "", cookies, settings.userAgentString ?: "", "")
                                            }
                                        }
                                    }
                                }
                            }

                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                                if (provider == "Anthropic") {
                                    val urlStr = request?.url?.toString() ?: ""
                                    if (urlStr.contains("claude.ai/api/organizations/") && !urlStr.contains("api/organizations/chat")) {
                                        val regex = "api/organizations/([a-f0-9\\-]+)".toRegex()
                                        val matchResult = regex.find(urlStr)
                                        val orgId = matchResult?.groupValues?.get(1)
                                        if (orgId != null) {
                                            post {
                                                val cookies = CookieManager.getInstance().getCookie("https://claude.ai") ?: ""
                                                val sessionKeyRegex = "sessionKey=([^;\\s]+)".toRegex()
                                                val match = sessionKeyRegex.find(cookies)
                                                val sessionKey = match?.groupValues?.get(1) ?: ""
                                                if (sessionKey.isNotEmpty()) {
                                                    if (tokenCaptured.compareAndSet(false, true)) {
                                                        onTokenCaptured("Anthropic", sessionKey, cookies, view?.settings?.userAgentString ?: "", orgId)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (provider == "OpenAI") {
                                    // Also intercept authorization headers if present in headers
                                    val headers = request?.requestHeaders
                                    val authHeader = headers?.get("Authorization") ?: headers?.get("authorization")
                                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                        post {
                                            val cookies = CookieManager.getInstance().getCookie("https://chatgpt.com") ?: ""
                                            if (tokenCaptured.compareAndSet(false, true)) {
                                                onTokenCaptured("OpenAI", authHeader, cookies, view?.settings?.userAgentString ?: "", "")
                                            }
                                        }
                                    }
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }

                        // Load initial URL — clear all web cookies on fresh login so
                        // a second account of the same provider does not auto-login
                        // to the previous session. Reload button does NOT touch the flag.
                        val loginUrl = when (provider) {
                            "Anthropic" -> "https://claude.ai/login"
                            "Ollama" -> "https://ollama.com/settings"
                            else -> "https://chatgpt.com/auth/login"
                        }
                        if (isFreshLogin) {
                            CookieManager.getInstance().removeAllCookies { _ ->
                                loadUrl(loginUrl)
                                isFreshLogin = false
                            }
                        } else {
                            loadUrl(loginUrl)
                        }
                    }
                }
            )

            // Destroy the WebView only when the composable leaves composition.
            // Keying this effect by webViewInstance can dispose the previous effect
            // immediately after the factory assigns the newly created WebView, which
            // destroys the active WebView before the login page finishes loading.
            DisposableEffect(Unit) {
                onDispose {
                    webViewInstance?.destroy()
                }
            }

            if (isLoadingPage) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
