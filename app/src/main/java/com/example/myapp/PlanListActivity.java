package com.example.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class PlanListActivity extends AppCompatActivity {
    private ListView lvPlans;
    private FloatingActionButton fabAdd;
    private Button btnStats;
    private DatabaseHelper dbHelper;
    private List<Plan> planList;
    private PlanAdapter adapter;
    private String username;
    private Uri avatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        // 获取传递的数据
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        String avatarStr = intent.getStringExtra("avatar");
        if (avatarStr != null) {
            avatarUri = Uri.parse(avatarStr);
        }

        // 初始化视图
        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        lvPlans = findViewById(R.id.lvPlans);
        fabAdd = findViewById(R.id.fabAdd);
        btnStats = findViewById(R.id.btnStats);

        // 设置用户名和头像
        tvUsername.setText(username);
        if (avatarUri != null) {
            ivAvatar.setImageURI(avatarUri);
        }

        // 初始化数据库
        dbHelper = new DatabaseHelper(this);

        // 加载计划列表
        loadPlans();

        // 添加计划按钮点击事件
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPlanDialog();
            }
        });

        // 查看统计按钮点击事件
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statsIntent = new Intent(PlanListActivity.this, StatsActivity.class);
                statsIntent.putExtra("username", username);
                startActivity(statsIntent);
            }
        });

        // 列表项长按事件（删除）
        lvPlans.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Plan plan = planList.get(position);
                showDeleteDialog(plan);
                return true;
            }
        });
    }

    private void loadPlans() {
        // 假设用户ID为1（实际应用中应该根据登录用户获取）
        int userId = 1;
        planList = dbHelper.getAllPlans(userId);
        adapter = new PlanAdapter(this, planList);
        lvPlans.setAdapter(adapter);
    }

    private void showAddPlanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_plan, null);

        final CustomEditText etTitle = view.findViewById(R.id.etTitle);
        final CustomEditText etDescription = view.findViewById(R.id.etDescription);
        final CustomEditText etDate = view.findViewById(R.id.etDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String date = etDate.getText().toString().trim();

                if (title.isEmpty() || date.isEmpty()) {
                    Toast.makeText(PlanListActivity.this, "请输入标题和日期", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 假设用户ID为1
                Plan plan = new Plan(title, description, date, 1);
                dbHelper.addPlan(plan);
                loadPlans();
                dialog.dismiss();
                Toast.makeText(PlanListActivity.this, "计划添加成功", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showDeleteDialog(final Plan plan) {
        new AlertDialog.Builder(this)
                .setTitle("删除计划")
                .setMessage("确定要删除这个计划吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deletePlan(plan.getId());
                    loadPlans();
                    Toast.makeText(PlanListActivity.this, "计划已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}