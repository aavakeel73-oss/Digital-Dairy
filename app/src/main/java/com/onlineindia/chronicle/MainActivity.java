
package com.onlineindia.chronicle;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.CookieManager;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    private static final String HOME =
        "file:///android_asset/index.html";

    private static final String LOGIN_URL =
        "https://chronicle-diary-23cb2.web.app/";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);

        CookieManager.getInstance()
            .setAcceptCookie(true);

        webView.addJavascriptInterface(
            new ChronicleBridge(),
            "ChronicleAndroid"
        );

        webView.setWebChromeClient(
            new WebChromeClient() {

                @Override
                public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                    if (fileCallback != null) {
                        fileCallback.onReceiveValue(null);
                    }

                    fileCallback = callback;

                    try {
                        startActivityForResult(
                            params.createIntent(), 1001
                        );
                        return true;
                    } catch (Exception e) {
                        fileCallback = null;
                        return false;
                    }
                }
            }
        );

        webView.setWebViewClient(
            new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                    Uri uri = request.getUrl();
                    String scheme = uri.getScheme();
                    String host = uri.getHost();

                    if ("https".equals(scheme)
                            && host != null
                            && (host.equals("accounts.google.com")
                            || host.equals("oauth2.googleapis.com"))) {

                        openBrowser(uri.toString());
                        return true;
                    }

                    if ("http".equals(scheme)
                            || "https".equals(scheme)
                            || "file".equals(scheme)) {
                        return false;
                    }

                    try {
                        startActivity(new Intent(
                            Intent.ACTION_VIEW, uri
                        ));
                    } catch (Exception e) {
                        Toast.makeText(
                            MainActivity.this,
                            "लिंक नहीं खुल पाया",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                    return true;
                }
            }
        );

        webView.loadUrl(HOME);
    }

    private void openBrowser(String url) {
        try {
            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            );
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                this,
                "ब्राउज़र नहीं खुल पाया",
                Toast.LENGTH_SHORT
            ).show();
        }
    }

    public class ChronicleBridge {

        @JavascriptInterface
        public void openGoogleLogin() {
            runOnUiThread(() ->
                openBrowser(LOGIN_URL)
            );
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {

        super.onActivityResult(
            requestCode, resultCode, data
        );

        if (requestCode == 1001
                && fileCallback != null) {

            Uri[] result =
                WebChromeClient.FileChooserParams
                    .parseResult(resultCode, data);

            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface(
                "ChronicleAndroid"
            );
            webView.destroy();
        }
        super.onDestroy();
    }
}
