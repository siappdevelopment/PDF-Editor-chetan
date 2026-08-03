package com.reader.alldocviewer.pdfeditor.swiperefresh;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.reader.alldocviewer.pdfeditor.R;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultCustomHeadView extends LinearLayout
        implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {
    private static final int ROTATE_ANIM_DURATION = 180;

    private LinearLayout mContainer;
    private ImageView mImageView;
    private TextView mMainTextView;
    private LottieAnimationView mLottieView;
    private Animation mRotateDownAnim;
    private Animation mRotateUpAnim;

    public DefaultCustomHeadView(Context context) {
        super(context);
        setWillNotDraw(false);
        setupLayout();
    }

    public void setupLayout() {
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.default_swiperefresh_head_layout, null);
        mContainer = linearLayout;
        addView(linearLayout, layoutParams);
        setGravity(Gravity.BOTTOM);
        mImageView = findViewById(R.id.default_header_arrow);
        mMainTextView = findViewById(R.id.default_header_textview);
        mLottieView = findViewById(R.id.lavRecycle);
        setupAnimation();
    }

    public void setupAnimation() {
        mRotateUpAnim = new RotateAnimation(
                0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(
                -180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    private void showRefreshingLoader(boolean show) {
        if (mLottieView == null) {
            return;
        }
        if (show) {
            mLottieView.setVisibility(View.VISIBLE);
            if (!mLottieView.isAnimating()) {
                mLottieView.playAnimation();
            }
        } else {
            mLottieView.cancelAnimation();
            mLottieView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStateChange(
            CustomSwipeRefreshLayout.State state,
            CustomSwipeRefreshLayout.State lastState) {
        int refreshState = state.getRefreshState();
        int lastRefreshState = lastState.getRefreshState();
        if (refreshState == lastRefreshState) {
            return;
        }

        if (refreshState == 3 || refreshState == 2) {
            mImageView.clearAnimation();
            mImageView.setVisibility(View.INVISIBLE);
            showRefreshingLoader(true);
        } else {
            showRefreshingLoader(false);
            mImageView.setVisibility(View.VISIBLE);
        }

        if (refreshState == 0) {
            if (lastRefreshState == 1) {
                mImageView.startAnimation(mRotateDownAnim);
            }
            if (lastRefreshState == 2) {
                mImageView.clearAnimation();
            }
            mMainTextView.setText(R.string.csr_text_state_normal);
        } else if (refreshState == 1) {
            if (lastRefreshState != 1) {
                mImageView.clearAnimation();
                mImageView.startAnimation(mRotateUpAnim);
                mMainTextView.setText(R.string.csr_text_state_ready);
            }
        } else if (refreshState == 2) {
            mMainTextView.setText(R.string.csr_text_state_refresh);
        } else if (refreshState == 3) {
            mMainTextView.setText(R.string.csr_text_state_complete);
        }
    }

    public String fetchData() {
        return getResources().getString(R.string.csr_text_last_refresh) + " "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
