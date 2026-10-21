
package com.onlineindia.chronicle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private FrameLayout root;

    private static final int BG =
            Color.rgb(250, 249, 246);

    private static final int NOTIFICATION_REQUEST = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();

        root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        webView = new WebView(this);
        webView.setBackgroundColor(BG);

        root.addView(webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        setContentView(root);

        configureInsets();
        configureWebView();
        requestNotificationPermission();
    }

    private void configureSystemBars() {

        Window window = getWindow();

        window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        );

        window.getDecorView().setBackgroundColor(BG);

        if (Build.VERSION.SDK_INT < 35) {
            window.setStatusBarColor(BG);
            window.setNavigationBarColor(BG);
        }

        if (Build.VERSION.SDK_INT >= 30) {

            WindowInsetsController controller =
                    window.getInsetsController();

            if (controller != null) {

                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }

        } else {

            int flags = 0;

            if (Build.VERSION.SDK_INT >= 23) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            if (Build.VERSION.SDK_INT >= 26) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void configureInsets() {

        if (Build.VERSION.SDK_INT >= 35) {

            root.setOnApplyWindowInsetsListener(
                    (view, insets) -> {

                        Insets bars = insets.getInsets(
                                WindowInsets.Type.systemBars()
                                | WindowInsets.Type.displayCutout()
                        );

                        root.setPadding(
                                bars.left,
                                bars.top,
                                bars.right,
                                bars.bottom
                        );

                        return insets;
                    }
            );

            root.requestApplyInsets();
        }
    }

    private void configureWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);

        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(
                new ReminderBridge(this),
                "ChronicleAndroid"
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                Uri uri = request.getUrl();
                String scheme = uri.getScheme();

                if ("file".equals(scheme)
                        || "about".equals(scheme)) {
                    return false;
                }

                if ("https".equals(scheme)
                        || "http".equals(scheme)) {

                    try {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW, uri
                        );
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }

                    return true;
                }

                return true;
            }
        });

        webView.loadUrl(
                "file:///android_asset/index.html"
        );
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_REQUEST
                );
            }
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
            webView = null;
        }

        super.onDestroy();
    }
}
