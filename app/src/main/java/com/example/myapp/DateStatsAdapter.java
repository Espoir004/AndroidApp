package com.example.myapp;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class DateStatsAdapter extends RecyclerView.Adapter<DateStatsAdapter.Holder> {
    private final List<String> dates;
    private final List<Integer> counts;

    public DateStatsAdapter(List<String> dates, List<Integer> counts) {
        this.dates = dates;
        this.counts = counts;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_date_bar, p, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        String date = dates.get(i);
        int count = counts.get(i);

        h.tvDate.setText(date.substring(5)); // MM-dd
        h.tvCount.setText(String.valueOf(count));

        if (count == 0) {
            h.bar.setVisibility(View.GONE); // 不画柱子
        } else {
            h.bar.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = h.bar.getLayoutParams();
            lp.height = Math.max(20, count * 30); // 最小 20 px
            h.bar.setLayoutParams(lp);

            // 颜色分级
            int color;
            if (count <= 2) {
                color = Color.parseColor("#FF90CAF9"); // 淡蓝
            } else if (count <= 5) {
                color = Color.parseColor("#FF42A5F5"); // 中蓝
            } else {
                color = Color.parseColor("#FF1976D2"); // 深蓝
            }
            h.bar.setBackgroundColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        View bar;
        TextView tvDate, tvCount;

        Holder(@NonNull View itemView) {
            super(itemView);
            bar = itemView.findViewById(R.id.bar);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCount = itemView.findViewById(R.id.tvCount);
        }
    }
}