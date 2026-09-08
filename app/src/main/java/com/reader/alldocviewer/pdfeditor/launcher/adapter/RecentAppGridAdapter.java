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

public class RecentAppGridAdapter extends RecyclerView.Adapter<RecentAppGridAdapter.ViewHolder> {

    private final Context context;
    private final List<UsageAppModel> items = new ArrayList<>();

    public RecentAppGridAdapter(Context context) {
        this.context = context;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_app_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsageAppModel model = items.get(position);
        holder.ivAppIcon.setImageDrawable(model.getAppIcon());
        holder.tvAppName.setText(model.getAppName());
        holder.itemView.setOnClickListener(v -> launchApp(model.getPackageName()));
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
        }
    }
}
