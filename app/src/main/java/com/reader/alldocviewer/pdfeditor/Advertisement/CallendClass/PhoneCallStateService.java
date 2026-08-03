package com.reader.alldocviewer.pdfeditor.Advertisement.CallendClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.CallFunction.Service.PreferenceCallManege;

import java.util.Date;


public class PhoneCallStateService extends BroadcastReceiver {
    private static final String TAG = "PhoneCallStateService";

    public static boolean isIncomingCallSend = false;
    static boolean isIncomingCallMain = false;
    static boolean isMissCalls = false;
    static boolean isOutgoingCall = false;
    static boolean isRingingCall = false;
    static boolean isShowScreenCall = false;
    static String outgoingNumber;
    protected Context savedContext;
    Date callStartTime = new Date();
    PreferenceCallManege preferenceCallManager;

    private void sendToMixpanel(String str) {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive:fgsggdsgdsg     1111111 " );
        ADSAppManage.dialogboolean = false;
        this.preferenceCallManager = PreferenceCallManege.getInstance(context);
        this.savedContext = context;
        if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
            try {
                String stringExtra = intent.getStringExtra("state");
                String str = outgoingNumber;
                if (str == null || str.length() == 0) {
                    outgoingNumber = intent.getStringExtra("incoming_number");

                }
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(stringExtra)) {

                    if (!isIncomingCallMain && isRingingCall) {
                        isMissCalls = true;
                        isIncomingCallMain = false;
                        isOutgoingCall = false;
                    }
                    String str2 = isIncomingCallMain ? "Incoming" : isMissCalls ? "Missed Call" : "Outgoing";
                    if (!isShowScreenCall) {
                        sendToMixpanel("Call_End");
                        openNewActivity(context, outgoingNumber, this.callStartTime, new Date(), str2);
                        isShowScreenCall = true;


                        return;
                    }
                    outgoingNumber = null;
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stringExtra)) {
                    this.callStartTime = new Date();
                    if (!isRingingCall) {
                        isOutgoingCall = true;
                    } else {
                        isIncomingCallSend = true;
                        sendToMixpanel("Incoming_Call");
                        isIncomingCallMain = true;
                    }
                    isShowScreenCall = false;
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stringExtra)) {
                    this.callStartTime = new Date();
                    isRingingCall = true;
                    isShowScreenCall = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onIncomingMainCallStarted(String str, Date date, Context context) {
        this.preferenceCallManager.getIncoming();
    }

    public void onOutgoingMainCallStarted(String str, Date date, Context context) {
        this.preferenceCallManager.getOutgoing();
    }

    public void onIncomingMainCallEnded(String str, Date date, Date date2, Context context) {
        if (this.preferenceCallManager.getIncoming()) {
            openNewActivity(context, str, date, date2, "Incoming");
        }
    }

    public void onOutgoingMainCallEnded(String str, Date date, Date date2, Context context) {
        if (this.preferenceCallManager.getOutgoing()) {
            openNewActivity(context, str, date, date2, "Outgoing");
        }
    }

    public void onMissedCall(String str, Date date, Context context) {
        if (this.preferenceCallManager.getMissedCall()) {
            openNewActivity(context, str, date, new Date(), "Missed Call");
        }
    }


    private void openNewActivity(Context context, String str, Date date, Date date2, String str2) {
        if (isIncomingCallSend) {
            sendToMixpanel("Request_to_native_ad_load");
        }

        if (!ADSMainClass.getIsShowCallEnd()) {
            outgoingNumber = null;
            isMissCalls = false;
            isIncomingCallMain = false;
            isOutgoingCall = false;
            return;
        }

//        Log.d(TAG, "openNewActivity: delegated to CallEndLaunchHelper");
        Log.d(TAG, "onReceive:fgsggdsgdsg     2222222 "+ADSMainClass.getIsShowCallEnd() );
        CallEndLaunchHelper.openAfterCallEnded(context, str, date, date2, str2);

        outgoingNumber = null;
        isMissCalls = false;
        isIncomingCallMain = false;
        isOutgoingCall = false;
    }
}
