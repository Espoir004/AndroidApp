package com.example.myapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// 添加必要的导入
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

        final TextView etTitle = view.findViewById(R.id.etTitle);
        final TextView etDescription = view.findViewById(R.id.etDescription);
        final TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        // 设置默认日期为今天
        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(calendar.getTime());
        tvSelectedDate.setText(today);
        final String[] selectedDate = {today};

        // 日期选择按钮点击事件
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建日期选择器对话框
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        PlanListActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // 月份从0开始，所以需要+1
                                String formattedDate = String.format(Locale.getDefault(),
                                        "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                                tvSelectedDate.setText(formattedDate);
                                selectedDate[0] = formattedDate;
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePickerDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String date = selectedDate[0];

                if (title.isEmpty()) {
                    Toast.makeText(PlanListActivity.this, "请输入标题", Toast.LENGTH_SHORT).show();
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