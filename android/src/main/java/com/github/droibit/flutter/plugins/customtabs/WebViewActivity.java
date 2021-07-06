package com.github.droibit.flutter.plugins.customtabs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.github.droibit.flutter.plugins.customtabs.internal.Launcher;
import com.github.droibit.plugins.customtabs.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.HashMap;
import java.util.Map;


public class WebViewActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "extra.url";
    public static final String EXTRA_TITLE = "extra.title";
    public static final String EXTRA_OPTION = "extra.option";
    public static final String EXTRA_ENTER_ANIM = "extra.enter.anim";
    public static final String EXTRA_EXIT_ANIM = "extra.exit.anim";
    private WebView webView;
    private Toolbar toolbar;
    private LinearProgressIndicator progressBar;
    private int toolBarColor = Color.WHITE;
    private Map<String, Object> option = new HashMap<>();

    private int getTintColor() {
        int red = (toolBarColor >> 16) & 0xFF;
        int green = (toolBarColor >> 8) & 0xFF;
        int blue = toolBarColor & 0xFF;
        double gray = red * 0.299 + green * 0.587 + blue * 0.114;
        return gray > 186 ? Color.BLACK : Color.WHITE;
    }

    private final WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            getSupportActionBar().setTitle(s);
        }

        @Override
        public void onProgressChanged(WebView webView, int i) {
            super.onProgressChanged(webView, i);
            progressBar.setProgress(i);
        }

    };
    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
//            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
//            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
           return super.shouldOverrideUrlLoading(webView, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("http") || url.startsWith("https")) {
                return false;
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        option = (Map<String, Object>) getIntent().getSerializableExtra(EXTRA_OPTION);
        if (option != null && option.get(Launcher.KEY_OPTIONS_TOOLBAR_COLOR) != null) {
            toolBarColor = Color.parseColor((String) option.get(Launcher.KEY_OPTIONS_TOOLBAR_COLOR));
        }

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            getWindow().setStatusBarColor(toolBarColor);
        }
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (getTintColor() == Color.BLACK) {
            ui |=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);

        setContentView(R.layout.webview_activity);
        webView = findViewById(R.id.web_act_webview);
        toolbar = findViewById(R.id.web_act_toolbar);
        progressBar = findViewById(R.id.web_act_progress_bar);
        progressBar.setMax(100);
        init(title);
        configWebView(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void init(String title) {
        setSupportActionBar(toolbar);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_close_24);
        if (drawable != null) {
            DrawableCompat.setTint(drawable, getTintColor());
            toolbar.setNavigationIcon(drawable);
        }
        if (!TextUtils.isEmpty(title)) {
            getSupportActionBar().setTitle(title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setBackgroundColor(toolBarColor);
        toolbar.setTitleTextColor(getTintColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        MenuItem item = menu.findItem(R.id.shareButton);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_share_24);
        if (drawable != null) {
            DrawableCompat.setTint(drawable, getTintColor());
            item.setIcon(drawable);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.shareButton) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = webView.getTitle() + "\n" + webView.getUrl();
            String shareSubject = webView.getTitle();
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            startActivity(Intent.createChooser(sharingIntent, "Share"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configWebView(String url) {
        webView.loadUrl(url);
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(webViewClient);
        WebSettings webSettings = webView.getSettings();
        if (webSettings != null) {
            if (option != null) {
                Map<String, String> headers = (Map<String, String>) option.get(Launcher.KEY_HEADERS);
                if (headers != null && headers.containsKey("user-agent")) {
                    webSettings.setUserAgentString(headers.get("user-agent"));
                }
            }

            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
        }

    }
}

