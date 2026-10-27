package com.reader.alldocviewer.pdfeditor.launcher.adapter;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.LauncherHomeActivity;
import com.reader.alldocviewer.pdfeditor.launcher.common.AppUsageStatsHelper;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {
    private static PopupWindow appActionPopup;

    private final Context context;
    private final List<AppsModel> arrayListApps;
    private int textColor;
    private final OnAppsUninstallClickListener onAppsUninstallClickListener;

    private int iconSize;
    private boolean labelVisible;
    private float labelSize;

    public AppsAdapter(Context context, List<AppsModel> arrayListApps, int textColor, OnAppsUninstallClickListener onAppsUninstallClickListener) {
        this.context = context;
        this.arrayListApps = arrayListApps;
        this.textColor = textColor;
        this.onAppsUninstallClickListener = onAppsUninstallClickListener;

        iconSize = Utils.dpToPx(context, Utils.getAppIconSize(context));
        labelVisible = Utils.getLabelVisibility(context);
        labelSize = Utils.getAppLabelSize(context);

        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_apps, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppsModel appsModel = arrayListApps.get(position);

        holder.ivAppIcon.setImageDrawable(appsModel.getAppIcon());
        holder.setIconSize(iconSize);

        if (labelVisible) {
            holder.tvAppName.setVisibility(View.VISIBLE);
            holder.tvAppName.setText(appsModel.getAppName());
            holder.tvAppName.setTextSize(labelSize);
            holder.tvAppName.setTextColor(textColor);
        } else {
            holder.tvAppName.setVisibility(View.GONE);
        }

//        holder.itemView.setOnClickListener(v -> {
//            int adapterPosition = holder.getBindingAdapterPosition();
//            if (adapterPosition == RecyclerView.NO_POSITION) {
//                return;
//            }
//
//            if (LauncherHomeActivity.appsFolderDialog != null && LauncherHomeActivity.appsFolderDialog.isShowing()) {
//                LauncherHomeActivity.appsFolderDialog.dismiss();
//            }
//
////            MyApplication.isAppOpenBlock = true;
//            String packageName = arrayListApps.get(adapterPosition).getPackageName();
//            launchApp(packageName);
//
//        });
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= arrayListApps.size()) {
                return;
            }
            launchAppWithAd(arrayListApps.get(adapterPosition).getPackageName());
        });
        holder.itemView.setOnLongClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return true;
            }
            showAppDialog(v, arrayListApps.get(adapterPosition).getAppName(), arrayListApps.get(adapterPosition).getPackageName());
            return true;
        });
    }

    private void launchAppWithAd(String packageName) {
        Activity activity = getHostActivity();
        if (activity != null) {
            AdPlacement.handleLauncherAppClickAd(activity, () -> launchApp(packageName));
        } else {
            launchApp(packageName);
        }
    }

    @Nullable
    private Activity getHostActivity() {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    private void launchApp(String packageName) {
        dismissAppsFolderDialog();
        setRecentApp(packageName);
        AppUsageStatsHelper.onAppLaunched(context, packageName);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    private static void dismissAppsFolderDialog() {
        if (LauncherHomeActivity.appsFolderDialog != null && LauncherHomeActivity.appsFolderDialog.isShowing()) {
            LauncherHomeActivity.appsFolderDialog.dismiss();
        }
    }

    @Override
    public int getItemCount() {
        return arrayListApps.size();
    }

    @Override
    public long getItemId(int position) {
        String packageName = arrayListApps.get(position).getPackageName();
        return packageName != null ? packageName.hashCode() : position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView ivAppIcon;
        private final AppCompatTextView tvAppName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
        }

        public void setIconSize(int iconSize) {
            ViewGroup.LayoutParams params = ivAppIcon.getLayoutParams();
            params.width = iconSize;
            params.height = iconSize;
            ivAppIcon.setLayoutParams(params);
        }
    }

    private void setRecentApp(String packageName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("recentThread", MODE_PRIVATE);
        String oldData = sharedPreferences.getString("packages", "");

        ArrayList<String> list = new ArrayList<>();
        if (!oldData.isEmpty()) {
            list.addAll(Arrays.asList(oldData.split(",")));
        }
        list.remove(packageName);
        list.add(0, packageName);

        while (list.size() > 4) {
            list.remove(list.size() - 1);
        }

        sharedPreferences.edit().putString("packages", TextUtils.join(",", list)).apply();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSizeVisiblity() {
        iconSize = Utils.dpToPx(context, Utils.getAppIconSize(context));
        labelVisible = Utils.getLabelVisibility(context);
        labelSize = Utils.getAppLabelSize(context);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTextColor(int textColor) {
        this.textColor = textColor;
        notifyDataSetChanged();
    }

    public static void dismissAppActionPopup() {
        if (appActionPopup != null && appActionPopup.isShowing()) {
            appActionPopup.dismiss();
        }
        appActionPopup = null;
    }

    @SuppressLint("InflateParams")
    private void showAppDialog(View view, String appName, String appPackage) {
        dismissAppActionPopup();

        Context context = view.getContext();
        View popupView = LayoutInflater.from(context).inflate(R.layout.dialog_app_action, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10f);
        popupWindow.setOnDismissListener(() -> appActionPopup = null);
        appActionPopup = popupWindow;

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        if (location[1] + view.getHeight() + popupHeight > screenHeight) {
            popupWindow.showAsDropDown(view, 0, -(popupHeight + view.getHeight()));
        } else {
            popupWindow.showAsDropDown(view);
        }

        AppCompatTextView tvAppName = popupView.findViewById(R.id.tvAppName);
        AppCompatImageView ivAppInfo = popupView.findViewById(R.id.ivAppInfo);
        LinearLayout llAppOpen = popupView.findViewById(R.id.llAppOpen);
        LinearLayout llAppUninstall = popupView.findViewById(R.id.llAppUninstall);

        if (appPackage.equals(context.getPackageName())) {
            llAppUninstall.setVisibility(GONE);
        } else {
            llAppUninstall.setVisibility(VISIBLE);
        }

        tvAppName.setText(appName);

        ivAppInfo.setOnClickListener(view1 -> {
            popupWindow.dismiss();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + appPackage));
            context.startActivity(intent);
        });

        llAppOpen.setOnClickListener(view1 -> {
            popupWindow.dismiss();
            launchAppWithAd(appPackage);
        });

        llAppUninstall.setOnClickListener(view1 -> {
            popupWindow.dismiss();
            onAppsUninstallClickListener.onAppsUninstallClick(appPackage);
        });
    }
}