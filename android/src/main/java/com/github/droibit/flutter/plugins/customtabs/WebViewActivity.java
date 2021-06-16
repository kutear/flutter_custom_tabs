package com.github.droibit.flutter.plugins.customtabs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.droibit.plugins.customtabs.R;


public class WebViewActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "extra.url";
    private WebView webView;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    private final WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            toolbar.setTitle(s);
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
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            progressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra(EXTRA_URL);
        setContentView(R.layout.webview_activity);
        webView = findViewById(R.id.web_act_webview);
        toolbar = findViewById(R.id.web_act_toolbar);
        progressBar = findViewById(R.id.web_act_progress_bar);
        progressBar.setMax(100);
        init();
        configWebView(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    private void init() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_24);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.shareButton) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody =  webView.getTitle() + "\n" + webView.getUrl();
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
            webSettings.setJavaScriptEnabled(true);
        }
    }
}

