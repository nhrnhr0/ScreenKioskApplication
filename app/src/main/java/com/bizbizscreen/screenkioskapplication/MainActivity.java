package com.bizbizscreen.screenkioskapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    private static final int MAX_RETRY_COUNT = 5;
    private int retryCount = 0;

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings.canDrawOverlays(this)) {6464f8
                    // Permission granted, start the activity from the BroadcastReceiver
                    StartupOnBootUpReceiver receiver = new StartupOnBootUpReceiver();
                    receiver.startMainActivity(this);
                } else {
                    Toast.makeText(this, "Overlay permission is required for this app to work correctly", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: start");
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)) {
            // Request overlay permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
        }




        // Hide the action bar
        getSupportActionBar().hide();
            setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loadWebView("https://auth-sso.bizbiz.co.il/qr");
//        loadWebView("https://home-desktop-4173.ms-global.co.il/qr");
//        loadWebView("https://home-desktop-3012.ms-global.co.il/qr");
        Log.e(TAG, "onCreate: end");



    }

    //https://stackoverflow.com/questions/39873537/android-webview-e-chromium-error-ssl-client-socket-impl-cc-1141
    private void loadWebView(String myUrl) {

        WebView webView = findViewById(R.id.myWebView);
        ProgressDialog progressdialog = new ProgressDialog(this);
        progressdialog.setMessage("Loading screen...");
        progressdialog.setCanceledOnTouchOutside(false);


        /* JS start*/
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        /*  JS start*/

        Log.d("TAG", "loadWebView: "+myUrl);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.evaluateJavascript("window.performance.memory.jsHeapSizeLimit", null);
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
//                Log.d(TAG, cm.message());
//
//                // tooast message
//                Toast.makeText(MainActivity.this, cm.message(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        webView.loadUrl(myUrl);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!progressdialog.isShowing()) {
                    Log.d("  TAG", "Started: ");
                    progressdialog.show();
                }
            }

            public void onPageFinished(WebView view, String url) {
                if (progressdialog.isShowing()) {
                    progressdialog.dismiss();
                    Log.d("TAG", "Finished: ");
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (progressdialog.isShowing()) {
                    progressdialog.dismiss();
                    Log.e("TAG", "Err: ");
                }

                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    Log.d(TAG, "Retrying... Attempt: " + retryCount);
                    Toast.makeText(MainActivity.this, "Failed to load page after " + retryCount + " attempts", Toast.LENGTH_LONG).show();
                    view.loadUrl(failingUrl);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load page after " + MAX_RETRY_COUNT + " attempts", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}