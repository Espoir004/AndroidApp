package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PlanAdapter extends ArrayAdapter<Plan> {

    public PlanAdapter(Context context, List<Plan> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plan plan = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView tvTitle = convertView.findViewById(android.R.id.text1);
        TextView tvSub   = convertView.findViewById(android.R.id.text2);

        // 第一行：标题
        tvTitle.setText(plan.getTitle());

        // 第二行：日期 + 描述 + 温度 + 节假日（一行显示）
        String temp  = plan.getTemperature();
        String holiday = plan.getHoliday();
        StringBuilder sb = new StringBuilder();
        sb.append(plan.getDate()).append(" - ").append(plan.getDescription());
        if (temp != null && !temp.trim().isEmpty()) sb.append("  ").append(temp);
        if (holiday != null && !holiday.trim().isEmpty()) sb.append("  🎈").append(holiday);
        tvSub.setText(sb.toString());

        return convertView;
    }
}