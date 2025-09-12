package com.example.myapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    private BarChart barChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        barChart = findViewById(R.id.barChart);
        dbHelper = new DatabaseHelper(this);

        // 假设用户ID为1
        int userId = 1;

        // 获取最近7天的日期
        List<String> dates = getLast7Days();

        // 创建柱状图数据
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            int count = dbHelper.getPlansCountByDate(dates.get(i), userId);
            entries.add(new BarEntry(i, count));
        }

        BarDataSet dataSet = new BarDataSet(entries, "每日计划数量");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private List<String> getLast7Days() {
        // 这里应该返回最近7天的日期列表
        // 为了简化，我们返回一些示例日期
        List<String> dates = new ArrayList<>();
        dates.add("2023-10-01");
        dates.add("2023-10-02");
        dates.add("2023-10-03");
        dates.add("2023-10-04");
        dates.add("2023-10-05");
        dates.add("2023-10-06");
        dates.add("2023-10-07");
        return dates;
    }
}