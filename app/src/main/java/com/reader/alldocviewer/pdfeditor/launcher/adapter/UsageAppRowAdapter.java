package com.reader.alldocviewer.pdfeditor.launcher.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.common.AppUsageStatsHelper;
import com.reader.alldocviewer.pdfeditor.launcher.model.UsageAppModel;

import java.util.ArrayList;
import java.util.List;

public class UsageAppRowAdapter extends RecyclerView.Adapter<UsageAppRowAdapter.ViewHolder> {

    public interface OnUsageAppClickListener {
        void onUsageAppClick(UsageAppModel model);
    }

    private final Context context;
    private final int layoutRes;
    private final boolean showScreenTimePrefix;
    private final List<UsageAppModel> items = new ArrayList<>();
    private OnUsageAppClickListener clickListener;

    public UsageAppRowAdapter(Context context, int layoutRes, boolean showScreenTimePrefix) {
        this.context = context;
        this.layoutRes = layoutRes;
        this.showScreenTimePrefix = showScreenTimePrefix;
    }

    public void setOnUsageAppClickListener(OnUsageAppClickListener listener) {
        this.clickListener = listener;
    }

    public void submitList(List<UsageAppModel> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsageAppModel model = items.get(position);
        holder.ivAppIcon.setImageDrawable(model.getAppIcon());
        holder.tvAppName.setText(model.getAppName());
        String time = AppUsageStatsHelper.formatUsageTime(model.getUsageMillis());
        if (showScreenTimePrefix) {
            holder.tvUsageTime.setText(context.getString(R.string.screen_time_value, time));
        } else {
            holder.tvUsageTime.setText(time);
        }
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUsageAppClick(model);
            } else {
                launchApp(model.getPackageName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void launchApp(String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                AppUsageStatsHelper.onAppLaunched(context, packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception ignored) {
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final AppCompatImageView ivAppIcon;
        final AppCompatTextView tvAppName;
        final AppCompatTextView tvUsageTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvUsageTime = itemView.findViewById(R.id.tvUsageTime);
        }
    }
}
