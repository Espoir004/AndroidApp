package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class PlanAdapter extends ArrayAdapter<Plan> {
    private int resourceId;

    public PlanAdapter(Context context, int textViewResourceId, List<Plan> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    public PlanAdapter(Context context, List<Plan> objects) {
        this(context, android.R.layout.simple_list_item_2, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plan plan = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = view.findViewById(android.R.id.text1);
            viewHolder.tvDate = view.findViewById(android.R.id.text2);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvTitle.setText(plan.getTitle());
        viewHolder.tvDate.setText(plan.getDate() + " - " + plan.getDescription());

        return view;
    }

    class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
    }
}