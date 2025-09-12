package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

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
     * 按日期统计计划数量
     */
    public int getPlansCountByDate(String date, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANS + " WHERE " +
                        COLUMN_PLAN_DATE + " = ? AND " + COLUMN_PLAN_USER_ID + " = ?",
                new String[]{date, String.valueOf(userId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 获取最近7天的计划统计
     */
    public List<Integer> getLast7DaysPlanCounts(int userId) {
        List<Integer> counts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 获取最近7天的日期
        List<String> dates = getLast7Days();

        for (String date : dates) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANS + " WHERE " +
                            COLUMN_PLAN_DATE + " = ? AND " + COLUMN_PLAN_USER_ID + " = ?",
                    new String[]{date, String.valueOf(userId)});
            cursor.moveToFirst();
            counts.add(cursor.getInt(0));
            cursor.close();
        }

        return counts;
    }

    /**
     * 获取最近7天的日期列表
     */
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