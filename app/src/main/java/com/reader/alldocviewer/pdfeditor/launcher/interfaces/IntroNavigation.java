package com.reader.alldocviewer.pdfeditor.launcher.interfaces;

import android.app.Activity;
import android.content.Intent;

import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.intro.Intro1Activity;
import com.reader.alldocviewer.pdfeditor.launcher.intro.Intro2Activity;
import com.reader.alldocviewer.pdfeditor.launcher.intro.Intro3Activity;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager;
import com.reader.alldocviewer.pdfeditor.ui.activities.permission.PermissionActivity;

public class IntroNavigation {

    public static void openIntroButtonFlow(Activity activity) {
        int count = ADSMainClass.getOnboardingCountShow();
        if (count > 0) {
            activity.startActivity(new Intent(activity, Intro1Activity.class));
        } else {
            completeIntroAndOpenPermission(activity);
        }
        activity.finish();
    }

    public static void goToNextIntroButtonScreen(Activity activity, int currentScreen) {
        int onboardingCount = ADSMainClass.getOnboardingCountShow();

        if (currentScreen >= onboardingCount) {
            completeIntroAndOpenPermission(activity);
            return;
        }

        Class<?> nextClass;
        switch (currentScreen + 1) {
            case 2:
                nextClass = Intro2Activity.class;
                break;
            case 3:
                nextClass = Intro3Activity.class;
                break;
            default:
                completeIntroAndOpenPermission(activity);
                return;
        }

        activity.startActivity(new Intent(activity, nextClass));
        activity.finish();
    }

    public static void completeIntroAndOpenPermission(Activity activity) {
        // Use startup flow so Permission is opened only when it is the next step
        // (avoids opening Permission twice when flow already showed it before OnBoarding).
        if (StartupFlowManager.isStartupFlowActive(activity)) {
            StartupFlowManager.completeOnboarding(activity);
            return;
        }
        Utils.setIntroCompleted(activity.getApplicationContext(), true);
        activity.startActivity(new Intent(activity, PermissionActivity.class));
        activity.finish();
    }
}
