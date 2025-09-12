package com.example.myapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        String username = getIntent().getStringExtra("username");

        // 可以设置标题栏显示当前用户
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(username + "的计划统计");
        }
    }
}