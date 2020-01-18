package awais.backworddictionary.customweb;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import awais.backworddictionary.R;

public final class WebViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        final String url = getIntent().getStringExtra("extra.url");
        setTitle(url);

        final WebView webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(getPackageName().startsWith("awais"));

        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}