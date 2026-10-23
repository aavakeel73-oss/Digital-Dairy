package com.onlineindia.chronicle;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends Activity {
    private static final int GOOGLE_SIGN_IN_REQUEST = 9007;
    private final android.os.Handler uiHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final class GoogleLoginBridge {
        @android.webkit.JavascriptInterface public void start() {
            uiHandler.post(() -> startNativeGoogleLogin());
        }
    }
    private void startNativeGoogleLogin() {
        // Firebase project Web OAuth client ID (client_type=3), NOT Android OAuth client.
        String webClientId = "791391618508-jkrpk0agnbnqoinkcn3uc37f9bsqhivi.apps.googleusercontent.com";
        com.google.android.gms.auth.api.signin.GoogleSignInOptions options =
            new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId).requestEmail().build();
        com.google.android.gms.auth.api.signin.GoogleSignInClient client =
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, options);
        client.signOut().addOnCompleteListener(this, task ->
            startActivityForResult(client.getSignInIntent(), GOOGLE_SIGN_IN_REQUEST));
    }
    private void deliverGoogleResult(String token, String error) {
        try {
            String script = "window.dispatchEvent(new CustomEvent('chronicle-native-google-result',{detail:{token:"+
                org.json.JSONObject.quote(token == null ? "" : token)+",error:"+
                org.json.JSONObject.quote(error == null ? "" : error)+"}}));";
            webView.evaluateJavascript(script, null);
        } catch (Exception ignored) { }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != GOOGLE_SIGN_IN_REQUEST) return;
        try {
            com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task =
                com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            String token = task.getResult(com.google.android.gms.common.api.ApiException.class).getIdToken();
            if (token == null || token.isEmpty()) deliverGoogleResult(null, "Google ID token नहीं मिला। Firebase SHA-1 और OAuth configuration जाँचें।");
            else deliverGoogleResult(token, null);
        } catch (com.google.android.gms.common.api.ApiException ex) {
            deliverGoogleResult(null, "Google Sign-In error " + ex.getStatusCode() + ". SHA-1, package name और Firebase OAuth सेटिंग जाँचें।");
        }
    }

    private WebView webView;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(250,249,246));
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) c.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.rgb(250,249,246));
        }
        webView = new WebView(this);
        // Prevent the WebView from drawing beneath status/navigation bars on Android 15+.
        if (Build.VERSION.SDK_INT >= 35) {
            webView.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new ReminderBridge(this), "ChronicleAndroid");
        webView.addJavascriptInterface(new GoogleLoginBridge(), "ChronicleGoogleNative");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme()) || "about".equals(uri.getScheme())) return false;
                if ("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch(Exception ignored) {}
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }
    @Override public void onBackPressed() {
        if (webView == null) { super.onBackPressed(); return; }
        webView.evaluateJavascript("(function(){return !!(window.chronicleHandleBack && window.chronicleHandleBack());})()", result -> {
            if (!"true".equals(result)) {
                if (webView.canGoBack()) webView.goBack();
                else finish();
            }
        });
    }
}
