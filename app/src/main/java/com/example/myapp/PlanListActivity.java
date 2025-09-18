package com.example.myapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private int userId;
    private Uri avatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("user_id", -1);
        String avatarStr = intent.getStringExtra("avatar");
        if (avatarStr != null) avatarUri = Uri.parse(avatarStr);

        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        lvPlans = findViewById(R.id.lvPlans);
        fabAdd = findViewById(R.id.fabAdd);
        btnStats = findViewById(R.id.btnStats);

        tvUsername.setText(username);
        if (avatarUri != null) ivAvatar.setImageURI(avatarUri);

        dbHelper = new DatabaseHelper(this);
        if (userId == -1) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 预加载当前年份的节假日数据
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        HolidayRepo.preloadHolidays(currentYear);

        loadPlans();

        fabAdd.setOnClickListener(v -> showAddPlanDialog());
        btnStats.setOnClickListener(v -> {
            Intent statsIntent = new Intent(PlanListActivity.this, StatsActivity.class);
            statsIntent.putExtra("username", username);
            statsIntent.putExtra("user_id", userId);
            startActivity(statsIntent);
        });

        lvPlans.setOnItemClickListener((parent, view, position, id) -> {
            Plan plan = planList.get(position);
            showEditPlanDialog(plan);
        });

        lvPlans.setOnItemLongClickListener((parent, view, position, id) -> {
            Plan plan = planList.get(position);
            showDeleteDialog(plan);
            return true;
        });
    }

    private void loadPlans() {
        planList = dbHelper.getAllPlans(userId);
        adapter = new PlanAdapter(this, planList);
        lvPlans.setAdapter(adapter);
    }

    /* -------------------- 添加计划 -------------------- */
    private void showAddPlanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_plan, null);

        TextView etTitle = view.findViewById(R.id.etTitle);
        TextView etDescription = view.findViewById(R.id.etDescription);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(cal.getTime());
        tvSelectedDate.setText(today);
        final String[] selectedDate = {today};

        btnSelectDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (picker, y, m, d) -> {
                selectedDate[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                tvSelectedDate.setText(selectedDate[0]);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            /* ===== 第三方节假日 API 提醒（子线程→主线程）===== */
            new Thread(() -> {
                String holiday = HolidayRepo.getName(selectedDate[0]);
                boolean isHoliday = HolidayRepo.isHoliday(selectedDate[0]);
                Log.d("Holiday", "date=" + selectedDate[0] + "  holiday=" + holiday + "  isHoliday=" + isHoliday);
                runOnUiThread(() -> {
                    if (isHoliday) {   // 非工作日就弹
                        String msg = holiday.isEmpty() ? "今天是休息日" : "API 返回：今天是 " + holiday;
                        new AlertDialog.Builder(PlanListActivity.this)
                                .setTitle("节假日提醒")
                                .setMessage(msg + "，仍要保存吗？")
                                .setPositiveButton("仍保存", (d, w) -> {
                                    doSave(title, description, selectedDate[0], holiday);
                                    dialog.dismiss();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } else {
                        // 工作日直接保存
                        doSave(title, description, selectedDate[0], holiday);
                        dialog.dismiss();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    /* -------------------- 编辑计划 -------------------- */
    private void showEditPlanDialog(Plan plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_plan, null);

        TextView etTitle = view.findViewById(R.id.etTitle);
        TextView etDescription = view.findViewById(R.id.etDescription);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        // 回显旧数据
        etTitle.setText(plan.getTitle());
        etDescription.setText(plan.getDescription());
        tvSelectedDate.setText(plan.getDate());
        final String[] selectedDate = {plan.getDate()};

        btnSelectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (picker, y, m, d) -> {
                selectedDate[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                tvSelectedDate.setText(selectedDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            /* ===== 第三方节假日 API 提醒 ===== */
            new Thread(() -> {
                String holiday = HolidayRepo.getName(selectedDate[0]);
                boolean isHoliday = HolidayRepo.isHoliday(selectedDate[0]);
                Log.d("Holiday", "date=" + selectedDate[0] + "  holiday=" + holiday + "  isHoliday=" + isHoliday);
                runOnUiThread(() -> {
                    if (isHoliday) {
                        String msg = holiday.isEmpty() ? "今天是休息日" : "API 返回：今天是 " + holiday;
                        new AlertDialog.Builder(PlanListActivity.this)
                                .setTitle("节假日提醒")
                                .setMessage(msg + "，仍要更新吗？")
                                .setPositiveButton("仍更新", (d, w) -> {
                                    doUpdate(plan, title, desc, selectedDate[0], holiday);
                                    dialog.dismiss();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } else {
                        doUpdate(plan, title, desc, selectedDate[0], holiday);
                        dialog.dismiss();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    /* -------------------- 真正写数据库 -------------------- */
    private void doSave(String title, String description, String date, String holiday) {
        Plan plan = new Plan(title, description, date, userId);
        plan.setHoliday(holiday);
        plan.setTemperature("--");
        dbHelper.addPlan(plan);
        loadPlans();
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
    }

    private void doUpdate(Plan plan, String title, String description, String date, String holiday) {
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setDate(date);
        plan.setHoliday(holiday);
        plan.setTemperature("--");
        dbHelper.updatePlan(plan);
        loadPlans();
        Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show();
    }

    /* -------------------- 删除 -------------------- */
    private void showDeleteDialog(Plan plan) {
        new AlertDialog.Builder(this)
                .setTitle("删除计划")
                .setMessage("确定要删除这个计划吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deletePlan(plan.getId());
                    loadPlans();
                    Toast.makeText(this, "计划已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}