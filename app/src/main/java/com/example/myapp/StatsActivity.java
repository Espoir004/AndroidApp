package com.example.myapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private DateStatsAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelper = new DatabaseHelper(this);
        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /* 构造连续 31 天：今天前后各 15 天 */
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -15);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 31; i++) {
            String date = sdf.format(cal.getTime());
            dates.add(date);
            counts.add(dbHelper.getPlansCountByDate(date, userId));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        adapter = new DateStatsAdapter(dates, counts);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        /* 滚动到“今天”位置 */
        recyclerView.scrollToPosition(15);
    }
}