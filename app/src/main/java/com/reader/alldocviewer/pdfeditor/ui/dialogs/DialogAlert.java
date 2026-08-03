package com.reader.alldocviewer.pdfeditor.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.reader.alldocviewer.pdfeditor.R;

/* loaded from: classes11.dex */
public class DialogAlert extends Dialog {
    TextView btnCancel;
    TextView btnOk;
    boolean discard;
    public OnClick listener;
    String msg;
    private final String negativeBtn;
    private final String posBtn;
    private final String title;
    TextView tvMsg;
    TextView tvTitle;

    /* loaded from: classes11.dex */
    public interface OnClick {

        /* loaded from: classes11.dex */
        public static final class C1825CC {
            public static void $default$onDiscard(OnClick onClick) {
            }
        }

        void onCancelClick();

        void onDiscard();

        void onOkClick();
    }

    public DialogAlert(Context context, String str, String str2, String str3, String str4) {
        super(context);
        this.title = str;
        this.msg = str2;
        this.posBtn = str3;
        this.negativeBtn = str4;
    }

    public DialogAlert(Context context, String str, String str2, String str3, String str4, boolean z) {
        super(context);
        this.title = str;
        this.msg = str2;
        this.posBtn = str3;
        this.negativeBtn = str4;
    }

    public void showDialog() {
        show();
    }

    public DialogAlert setListener(OnClick onClick) {
        this.listener = onClick;
        return this;
    }

    @Override // android.app.Dialog
    @SuppressLint({"WrongConstant"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        View inflate = getLayoutInflater().inflate(R.layout.dialog_alert, (ViewGroup) null);
        setContentView(inflate);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.5f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(params);
            int horizontalMargin = (int) (24 * getContext().getResources().getDisplayMetrics().density);
            int dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels
                    - (horizontalMargin * 2);
            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        initView(inflate);
        this.btnOk.setOnClickListener(new View.OnClickListener() { // from class: com.office.allreader.ui.DialogAlert.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DialogAlert.this.dismiss();
                OnClick onClick = DialogAlert.this.listener;
                if (onClick != null) {
                    onClick.onOkClick();
                }
            }
        });
        this.btnCancel.setOnClickListener(new View.OnClickListener() { // from class: com.office.allreader.ui.DialogAlert.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DialogAlert.this.dismiss();
                OnClick onClick = DialogAlert.this.listener;
                if (onClick != null) {
                    onClick.onCancelClick();
                }
            }
        });
        String str = this.title;
        if (str == null || str.equals("")) {
            this.tvTitle.setText("Alert");
        } else {
            this.tvTitle.setText(this.title);
        }
        this.tvMsg.setText(this.msg);
        String str2 = this.posBtn;
        if (str2 == null || str2.equals("")) {
            this.btnOk.setVisibility(8);
        } else {
            this.btnOk.setText(this.posBtn);
        }
        String str3 = this.negativeBtn;
        if (str3 == null || str3.equals("")) {
            this.btnCancel.setVisibility(8);
        } else {
            this.btnCancel.setText(this.negativeBtn);
        }
        if (this.discard) {
            findViewById(R.id.btnDiscard).setVisibility(0);
            findViewById(R.id.btnDiscard).setOnClickListener(new View.OnClickListener() { // from class: com.office.allreader.ui.DialogAlert.3
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    DialogAlert.this.dismiss();
                    OnClick onClick = DialogAlert.this.listener;
                    if (onClick != null) {
                        onClick.onDiscard();
                    }
                }
            });
        }
    }

    private void initView(View view) {
        this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        this.tvMsg = (TextView) view.findViewById(R.id.tvMsg);
        this.btnOk = (TextView) view.findViewById(R.id.btnOk);
        this.btnCancel = (TextView) view.findViewById(R.id.btnCancel);
    }
}
