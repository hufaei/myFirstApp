package com.example.lifelab.feature.weblab.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.R

@Composable
fun WebLabRoute(
    contentPadding: PaddingValues,
    onClose: () -> Unit = {},
    viewModel: WebLabViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WebLabScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onClose = onClose,
    )
}

@Composable
fun WebLabScreen(
    contentPadding: PaddingValues,
    uiState: WebLabUiState,
    onEvent: (WebLabUiEvent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val genericErrorMessage = stringResource(R.string.web_lab_error_generic)

    BackHandler(enabled = uiState.canGoBack) {
        onEvent(WebLabUiEvent.WebBackRequested)
    }

    LaunchedEffect(uiState.refreshRequested) {
        if (uiState.refreshRequested) {
            webView?.reload()
            onEvent(WebLabUiEvent.RefreshConsumed)
        }
    }

    LaunchedEffect(uiState.webBackRequested) {
        if (uiState.webBackRequested) {
            webView?.goBack()
            onEvent(WebLabUiEvent.WebBackConsumed(webView?.canGoBack() == true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        WebLabTopBar(
            isLoading = uiState.isLoading,
            backLabel = stringResource(
                if (uiState.canGoBack) {
                    R.string.web_lab_back
                } else {
                    R.string.common_back
                },
            ),
            onBack = {
                if (uiState.canGoBack) {
                    onEvent(WebLabUiEvent.WebBackRequested)
                } else {
                    onClose()
                }
            },
            onRefresh = { onEvent(WebLabUiEvent.RefreshRequested) },
        )
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = LifeLabWebViewClient(
                            context = context,
                            onEvent = onEvent,
                            genericErrorMessage = genericErrorMessage,
                        )
                        loadUrl(WebLabDefaults.START_URL)
                    }.also { createdWebView ->
                        webView = createdWebView
                    }
                },
                update = { currentWebView ->
                    webView = currentWebView
                    onEvent(WebLabUiEvent.BackStateChanged(currentWebView.canGoBack()))
                },
            )
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            uiState.errorMessage?.let { message ->
                WebLabErrorOverlay(
                    message = message,
                    onRetry = { onEvent(WebLabUiEvent.RefreshRequested) },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }
}

@Composable
private fun WebLabTopBar(
    isLoading: Boolean,
    backLabel: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WebLabIconButton(
            tooltip = backLabel,
            enabled = true,
            onClick = onBack,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backLabel,
            )
        }
        Text(
            text = stringResource(R.string.web_lab_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
        )
        if (isLoading) {
            CircularProgressIndicator()
        }
        WebLabIconButton(
            tooltip = stringResource(R.string.web_lab_refresh),
            enabled = true,
            onClick = onRefresh,
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.web_lab_refresh),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebLabIconButton(
    tooltip: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(text = tooltip)
            }
        },
        state = rememberTooltipState(),
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
        ) {
            icon()
        }
    }
}

@Composable
private fun WebLabErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.web_lab_unavailable),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.web_lab_retry))
        }
    }
}

private class LifeLabWebViewClient(
    private val context: Context,
    private val onEvent: (WebLabUiEvent) -> Unit,
    private val genericErrorMessage: String,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val uri = request?.url ?: return false
        if (uri.host.equals(WebLabDefaults.ALLOWED_HOST, ignoreCase = true)) {
            return false
        }

        openExternal(uri)
        return true
    }

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?,
    ) {
        onEvent(WebLabUiEvent.PageStarted(url ?: WebLabDefaults.START_URL))
        onEvent(WebLabUiEvent.BackStateChanged(view.canGoBack()))
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        onEvent(WebLabUiEvent.PageFinished(url ?: view.url.orEmpty()))
        onEvent(WebLabUiEvent.BackStateChanged(view.canGoBack()))
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request?.isForMainFrame == true) {
            onEvent(WebLabUiEvent.PageFailed(error?.description?.toString() ?: genericErrorMessage))
            onEvent(WebLabUiEvent.BackStateChanged(view?.canGoBack() == true))
        }
    }

    private fun openExternal(uri: Uri) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
