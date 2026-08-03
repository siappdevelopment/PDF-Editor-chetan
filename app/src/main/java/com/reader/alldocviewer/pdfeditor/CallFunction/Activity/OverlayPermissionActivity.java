package com.reader.alldocviewer.pdfeditor.CallFunction.Activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.transition.Explode;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.ui.activities.permission.PermissionActivity;

import org.jetbrains.annotations.Nullable;

public class OverlayPermissionActivity extends AppCompatActivity {

    public static final String EXTRA_HOST_TASK_ID = "extra_host_task_id";
    private static final long CHECK_INTERVAL_MS = 500L;

    private Window window;
    private WindowManager.LayoutParams layoutParams;
    private ConstraintLayout overlay_root;
    private final Handler overlayHandler = new Handler(Looper.getMainLooper());
    private boolean grantHandled = false;
    private final Runnable overlayChecker = new Runnable() {
        @Override
        public void run() {
            if (grantHandled) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(OverlayPermissionActivity.this)) {
                returnToApp();
                return;
            }
            overlayHandler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_permission_guide);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        overlay_root = findViewById(R.id.overlay_root);
        window = getWindow();
        window.addFlags(7078560);
        window.setSoftInputMode(2);
        setFinishOnTouchOutside(true);
        int animEnter = R.anim.optin_slide_up;
        overridePendingTransition(animEnter, 0);
        window.setEnterTransition(new Explode());
        window.setExitTransition(new Explode());
        overridePendingTransition(animEnter, 0);

        layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.x = 1;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.dimAmount = 0.2f;
        window.setAttributes(layoutParams);

        overlay_root.setOnClickListener(v -> OverlayPermissionActivity.this.finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            returnToApp();
        } else {
            startOverlayChecker();
        }
    }

    @Override
    protected void onPause() {
        stopOverlayChecker();
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        finish();
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 132) {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            if (Settings.canDrawOverlays(this)) {
                bundle.putString("PermissionStatus", "Allowed");
            } else {
                bundle.putString("PermissionStatus", "Denied");
            }
            firebaseAnalytics.logEvent("OverlayPermissionReport", bundle);
        }
    }

    private void startOverlayChecker() {
        stopOverlayChecker();
        overlayHandler.post(overlayChecker);
    }

    private void stopOverlayChecker() {
        overlayHandler.removeCallbacks(overlayChecker);
    }

    private void returnToApp() {
        if (grantHandled) {
            return;
        }
        grantHandled = true;
        stopOverlayChecker();

        Intent intent = new Intent(this, PermissionActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );
        intent.putExtra(PermissionActivity.EXTRA_OVERLAY_GRANTED, true);
        startActivity(intent);

        int hostTaskId = getIntent().getIntExtra(EXTRA_HOST_TASK_ID, -1);
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (activityManager != null && hostTaskId != -1) {
                activityManager.moveTaskToFront(hostTaskId, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        } catch (Exception ignored) {
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        stopOverlayChecker();
        super.onDestroy();
        overridePendingTransition(0, R.anim.optin_slide_down);
    }
}
