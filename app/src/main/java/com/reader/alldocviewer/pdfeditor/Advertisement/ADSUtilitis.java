package com.reader.alldocviewer.pdfeditor.Advertisement;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdValue;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.reader.alldocviewer.pdfeditor.BuildConfig;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.utils.DefaultPermissionButtonAd;


public class ADSUtilitis {

    private static final long MASSAGE_BOX_FULL_TIMEOUT_MS = 15_000L;
    private static final Handler massageBoxFullHandler = new Handler(Looper.getMainLooper());
    private static Runnable massageBoxFullTimeoutRunnable;

    public static Dialog MassageBoxFull;
    public static Dialog InternetMassageBox;
    public static Dialog WarningMassageBox;
    public static No_debug NoDebug;
    public static no_internet NoInternet;
    public static Dialog MassageBox;

    public static void prepareGoogleFullScreenAd(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        com.reader.alldocviewer.pdfeditor.utils.Utils.applyGoogleFullScreenAdWindow(activity);
    }

    public static void restoreAfterGoogleFullScreenAd(Context context) {
        if (context instanceof Activity) {
            restoreAfterGoogleFullScreenAd((Activity) context);
        }
    }

    public static void restoreAfterGoogleFullScreenAd(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        com.reader.alldocviewer.pdfeditor.utils.Utils.restoreSystemBarsAfterGoogleFullScreenAd(activity);
    }

    public static void MassageBoxFull(Context context) {
        try {
            if (MassageBoxFull != null && MassageBoxFull.isShowing()) {
                return;
            }

            cancelMassageBoxFullTimeout();
            MassageBoxFull = new Dialog(context);
            MassageBoxFull.requestWindowFeature(Window.FEATURE_NO_TITLE);
            MassageBoxFull.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            MassageBoxFull.setContentView(R.layout.loader_progressbar_massage_box);
            MassageBoxFull.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            MassageBoxFull.setCancelable(false);
            MassageBoxFull.show();
            scheduleMassageBoxFullTimeout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void MassageBoxFullDismiss() {
        try {
            cancelMassageBoxFullTimeout();
            if (MassageBoxFull != null && MassageBoxFull.isShowing()) {
                MassageBoxFull.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scheduleMassageBoxFullTimeout() {
        massageBoxFullTimeoutRunnable = () -> {
            massageBoxFullTimeoutRunnable = null;
            try {
                if (MassageBoxFull != null && MassageBoxFull.isShowing()) {
                    MassageBoxFull.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyMassageBoxFullTimeout();
        };
        massageBoxFullHandler.postDelayed(massageBoxFullTimeoutRunnable, MASSAGE_BOX_FULL_TIMEOUT_MS);
    }

    private static void cancelMassageBoxFullTimeout() {
        if (massageBoxFullTimeoutRunnable != null) {
            massageBoxFullHandler.removeCallbacks(massageBoxFullTimeoutRunnable);
            massageBoxFullTimeoutRunnable = null;
        }
    }

    private static void notifyMassageBoxFullTimeout() {
        try {
            ADSInterDisplay.onMassageBoxFullTimeout();
            ADSInterDisplayClick.onMassageBoxFullTimeout();
            ADSAppStarting.onMassageBoxFullTimeout();
            ADSRewardDisplay.onMassageBoxFullTimeout();
            DefaultPermissionButtonAd.onMassageBoxFullTimeout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackScreen(Context context,String screenName) {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean(screenName, true);
            FirebaseAnalytics.getInstance(context).logEvent(screenName, bundle);
            ADSMainClass.setCallEndShow1(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    public static void DebugCheck(No_debug no_debug) {
//        ADSUtilitis.NoDebug = no_debug;
//    }

    public static void SendDeveloperIntent(Context context) {
        try {
            context.startActivity((new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS")).setFlags(268435456));
        } catch (ActivityNotFoundException e) {
        }
    }

    public static Boolean IsDibugg(Context context) {
        if (BuildConfig.DEBUG) {
            return false;
        }
        if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void Check_internet(no_internet no_internet) {
        ADSUtilitis.NoInternet = no_internet;
    }

    public static boolean IsNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void InternetMassageBox(Context context) {
        InternetMassageBox = new Dialog(context);
        InternetMassageBox.requestWindowFeature(1);
        InternetMassageBox.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        InternetMassageBox.setContentView(R.layout.internet_massage_box);
        int width = -1;
        InternetMassageBox.getWindow().setLayout(width, -2);
        InternetMassageBox.setCancelable(false);
        InternetMassageBox.show();
        TextView btn_try_again = (TextView) InternetMassageBox.findViewById(R.id.btn_try_again);
        btn_try_again.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ADSUtilitis.NoInternet.no_internet();
            }
        });
    }

    public interface no_internet {
        void no_internet();
    }

    public static void trackPermissionAllowOnce(Context context, String eventName) {
        try {
            String prefKey = "allow_event_" + eventName;
            android.content.SharedPreferences prefs = context.getSharedPreferences(
                    "permission_allow_events", Context.MODE_PRIVATE);
            if (prefs.getBoolean(prefKey, false)) {
                return;
            }
            prefs.edit().putBoolean(prefKey, true).commit();
            trackScreen(context, eventName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public interface No_debug {
        void no_debug();
    }

    public static void logAdRevenue(Context context, AdValue adValue) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        double revenue = adValue.getValueMicros() / 1_000_000.0;
        String currency = adValue.getCurrencyCode();
        Bundle adRevenueParams = new Bundle();
        adRevenueParams.putString(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager");
        adRevenueParams.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        adRevenueParams.putDouble(FirebaseAnalytics.Param.VALUE, revenue);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, adRevenueParams);
    }
}
//callback chhe?ha
