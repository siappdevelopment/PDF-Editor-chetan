package com.reader.alldocviewer.pdfeditor.privacy;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.common.AdPlacement;
import com.reader.alldocviewer.pdfeditor.launcher.utils.Constance;
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleAwareAppCompatActivity;
import com.reader.alldocviewer.pdfeditor.launcher.utils.SharedPreferenceManager;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowStep;


public class PrivacyPolicySetting extends LocaleAwareAppCompatActivity {

    private CheckBox cbAgreeDataCollection;
    private TextView btnAgreeContinue;
    private TextView tvPrivacyFooter;
    private TextView tvAgreeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_setting);

        initViews();
        setupBackPress();
        setupFooterLinks();
        setupClicks();
        updateContinueEnabled(cbAgreeDataCollection.isChecked());
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back = the Agree button (unchecked box still shows the toast and stays).
                if (StartupFlowManager.isStartupFlowActive(PrivacyPolicySetting.this)) {
                    btnAgreeContinue.performClick();
                } else {
                    finish();
                }
            }
        });


    }

    private void initViews() {
        cbAgreeDataCollection = findViewById(R.id.cbAgreeDataCollection);
        btnAgreeContinue = findViewById(R.id.btnAgreeContinue);
        tvPrivacyFooter = findViewById(R.id.tvPrivacyFooter);
        tvAgreeLabel = findViewById(R.id.tvAgreeLabel);
    }

    private void setupFooterLinks() {
        String terms = getString(R.string.privacy_terms_short);
        String privacy = getString(R.string.privacy_policy);
        String text = getString(R.string.privacy_data_footer, terms, privacy);

        SpannableString spannable = new SpannableString(text);
        applyPolicyLinkSpan(spannable, text, terms, true);
        applyPolicyLinkSpan(spannable, text, privacy, false);

        tvPrivacyFooter.setText(spannable);
        tvPrivacyFooter.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivacyFooter.setHighlightColor(Color.TRANSPARENT);
    }

    private void applyPolicyLinkSpan(
            SpannableString spannable,
            String fullText,
            String label,
            boolean isTerms
    ) {
        if (label == null || label.isEmpty()) {
            return;
        }

        int start = fullText.indexOf(label);
        if (start < 0) {
            return;
        }

        int end = start + label.length();
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (isTerms) {
                    openTermsUrl();
                } else {
                    openPrivacyPolicyUrl();
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(PrivacyPolicySetting.this, R.color.privacy_accent));
                ds.setUnderlineText(true);
            }
        };
        spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setupClicks() {
        cbAgreeDataCollection.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateContinueEnabled(isChecked)
        );

        tvAgreeLabel.setOnClickListener(v ->
                cbAgreeDataCollection.setChecked(!cbAgreeDataCollection.isChecked())
        );

        btnAgreeContinue.setOnClickListener(v -> {
            if (!cbAgreeDataCollection.isChecked()) {
                Toast.makeText(this, R.string.privacy_please_agree, Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferenceManager.INSTANCE.putBoolean(
                    this,
                    Constance.PRIVACY_DATA_CONSENT,
                    true
            );
            if (StartupFlowManager.isStartupFlowActive(this)) {
                StartupFlowManager.completeStep(this, StartupFlowStep.POLICY_SCREEN);
            } else {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void updateContinueEnabled(boolean enabled) {
        btnAgreeContinue.setEnabled(enabled);
        btnAgreeContinue.setAlpha(enabled ? 1f : 0.5f);
    }

    private void openPrivacyPolicyUrl() {
        String url = ADSMainClass.getPrivacyPolicy();
        if (url != null && !url.isEmpty()) {
            ADSAppManage.isAppOpenBlocked = true;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTermsUrl() {
        String termsUrl = AdPlacement.getTermsConditions();
        if (termsUrl != null && !termsUrl.isEmpty()) {
            ADSAppManage.isAppOpenBlocked = true;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl)));
            return;
        }
        openPrivacyPolicyUrl();
    }
}
