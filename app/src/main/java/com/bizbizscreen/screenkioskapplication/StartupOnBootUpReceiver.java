package com.bizbizscreen.screenkioskapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bizbizscreen.screenkioskapplication.MainActivity;

public class StartupOnBootUpReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupOnBootUpReceiver";
    private static final int REQUEST_OVERLAY_PERMISSION = 5469; // Arbitrary request code

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        Toast.makeText(context, "Intent Action: " + intent.getAction(), Toast.LENGTH_LONG).show();

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(context)) {
                // Request overlay permission
                Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(overlayIntent);
            } else {
                // Start the activity
                startMainActivity(context);
            }
        }
    }

    public void startMainActivity(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(i);
        Log.d(TAG, "onReceive: Starting MainActivity done");
    }
}
