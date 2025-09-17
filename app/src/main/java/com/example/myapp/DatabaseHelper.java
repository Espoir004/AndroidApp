package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PlanApp.db";
    private static final int DATABASE_VERSION = 1;

    // 用户表
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_AVATAR = "avatar";

    // 计划表
    private static final String TABLE_PLANS = "plans";
    private static final String COLUMN_PLAN_ID = "plan_id";
    private static final String COLUMN_PLAN_TITLE = "title";
    private static final String COLUMN_PLAN_DESCRIPTION = "description";
    private static final String COLUMN_PLAN_DATE = "date";
    private static final String COLUMN_PLAN_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_AVATAR + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // 创建计划表
        String CREATE_PLANS_TABLE = "CREATE TABLE " + TABLE_PLANS + "("
                + COLUMN_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLAN_TITLE + " TEXT,"
                + COLUMN_PLAN_DESCRIPTION + " TEXT,"
                + COLUMN_PLAN_DATE + " TEXT,"
                + COLUMN_PLAN_USER_ID + " INTEGER" + ")";
        db.execSQL(CREATE_PLANS_TABLE);

        // 添加默认用户
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_PASSWORD, "password");
        values.put(COLUMN_AVATAR, "default_avatar");
        db.insert(TABLE_USERS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANS);
        onCreate(db);
    }

    // 用户相关操作

    /**
     * 检查用户是否存在
     */
    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + " = ?", new String[]{username});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    /**
     * 验证用户登录信息
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                        COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    /**
     * 获取用户头像
     */
    public String getUserAvatar(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_AVATAR + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        String avatar = "default_avatar";
        if (cursor.moveToFirst()) {
            avatar = cursor.getString(0);
        }
        cursor.close();
        return avatar;
    }

    /**
     * 添加新用户
     */
    public boolean addUser(String username, String password, String avatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_AVATAR, avatar);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * 根据用户名获取用户ID
     */
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // 计划相关操作

    /**
     * 添加计划
     */
    public void addPlan(Plan plan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAN_TITLE, plan.getTitle());
        values.put(COLUMN_PLAN_DESCRIPTION, plan.getDescription());
        values.put(COLUMN_PLAN_DATE, plan.getDate());
        values.put(COLUMN_PLAN_USER_ID, plan.getUserId());
        db.insert(TABLE_PLANS, null, values);
        db.close();
    }

    /**
     * 获取用户的所有计划
     */
    public List<Plan> getAllPlans(int userId) {
        List<Plan> planList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLANS + " WHERE " +
                        COLUMN_PLAN_USER_ID + " = ? ORDER BY " + COLUMN_PLAN_DATE + " DESC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Plan plan = new Plan();
                plan.setId(cursor.getInt(0));
                plan.setTitle(cursor.getString(1));
                plan.setDescription(cursor.getString(2));
                plan.setDate(cursor.getString(3));
                plan.setUserId(cursor.getInt(4));
                planList.add(plan);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return planList;
    }

    /**
     * 删除计划
     */
    public void deletePlan(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLANS, COLUMN_PLAN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }


    /**
     * 获取最近N天的计划数量统计
     */
    public Map<String, Integer> getPlanCountsLastNDays(int userId, int days) {
        Map<String, Integer> result = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 获取最近N天的日期
        for (int i = days - 1; i >= 0; i--) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, -i);
            String date = sdf.format(day.getTime());
            int count = getPlansCountByDate(date, userId);
            result.put(date, count);
        }

        return result;
    }

    /**
     * 获取最近7天的日期列表
     */
    public List<String> getLast7Days() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 6; i >= 0; i--) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, -i);
            dates.add(sdf.format(day.getTime()));
        }

        return dates;
    }

    /**
     * 获取用户所有计划中涉及的唯一日期，并按升序排列
     */
    public List<String> getAllPlanDates(int userId) {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_PLAN_DATE +
                " FROM " + TABLE_PLANS +
                " WHERE " + COLUMN_PLAN_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_PLAN_DATE + " ASC", new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            dates.add(cursor.getString(0));
        }
        cursor.close();
        return dates;
    }

    /**
     * 获取指定日期的计划数量
     */
    public int getPlansCountByDate(String date, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANS +
                        " WHERE " + COLUMN_PLAN_DATE + " = ? AND " + COLUMN_PLAN_USER_ID + " = ?",
                new String[]{date, String.valueOf(userId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 更新计划
     */
    public void updatePlan(Plan plan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAN_TITLE, plan.getTitle());
        values.put(COLUMN_PLAN_DESCRIPTION, plan.getDescription());
        values.put(COLUMN_PLAN_DATE, plan.getDate());

        db.update(TABLE_PLANS, values, COLUMN_PLAN_ID + " = ?",
                new String[]{String.valueOf(plan.getId())});
        db.close();
    }

    /**
     * 根据ID获取计划
     */
    public Plan getPlanById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLANS + " WHERE " +
                COLUMN_PLAN_ID + " = ?", new String[]{String.valueOf(id)});

        Plan plan = null;
        if (cursor.moveToFirst()) {
            plan = new Plan();
            plan.setId(cursor.getInt(0));
            plan.setTitle(cursor.getString(1));
            plan.setDescription(cursor.getString(2));
            plan.setDate(cursor.getString(3));
            plan.setUserId(cursor.getInt(4));
        }
        cursor.close();
        return plan;
    }
}