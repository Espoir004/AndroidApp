package com.example.myapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {
    private CustomBarChartView barChartView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        barChartView = findViewById(R.id.barChartView);
        dbHelper = new DatabaseHelper(this);

        // 获取从上一个活动传递过来的用户ID
        int userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show(); // 修正拼写错误
            finish();
            return;
        }

        // 获取最近7天的计划数量
        Map<String, Integer> planCounts = dbHelper.getPlanCountsLastNDays(userId, 7);

        // 提取日期和数量
        List<String> dates = new ArrayList<>(planCounts.keySet());
        List<Integer> counts = new ArrayList<>(planCounts.values());

        // 设置图表数据
        barChartView.setData(dates, counts);
    }
}