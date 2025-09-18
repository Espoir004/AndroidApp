package com.example.myapp;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HolidayRepo {
    private static final String BASE_URL = "https://date.nager.at/api/v3/PublicHolidays/";
    private static final String COUNTRY_CODE = "CN"; // 中国
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 缓存全年节假日数据
    private static Map<String, String> holidayCache = new HashMap<>();
    private static boolean isPreloading = false;
    private static int cachedYear = -1;

    // 本地节假日数据作为备用
    private static final Map<String, String> LOCAL_HOLIDAYS = new HashMap<>();
    static {
        // 添加中国主要节假日
        LOCAL_HOLIDAYS.put("01-01", "元旦");
        LOCAL_HOLIDAYS.put("05-01", "劳动节");
        LOCAL_HOLIDAYS.put("10-01", "国庆节");
        LOCAL_HOLIDAYS.put("10-02", "国庆节");
        LOCAL_HOLIDAYS.put("10-03", "国庆节");
        LOCAL_HOLIDAYS.put("10-04", "国庆节");
        LOCAL_HOLIDAYS.put("10-05", "国庆节");
        LOCAL_HOLIDAYS.put("10-06", "国庆节");
        LOCAL_HOLIDAYS.put("10-07", "国庆节");
    }

    /**
     * 预加载指定年份的节假日数据
     * @param year 要加载的年份
     */
    public static void preloadHolidays(int year) {
        if (isPreloading || year == cachedYear) {
            Log.d("HolidayRepo", "节假日数据正在加载中或已缓存，跳过请求");
            return;
        }

        isPreloading = true;
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + year + "/" + COUNTRY_CODE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                Log.d("HolidayRepo", "正在请求节假日API: " + url.toString());

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONArray holidays = new JSONArray(sb.toString());
                    Log.d("HolidayRepo", "获取到 " + holidays.length() + " 个节假日");

                    synchronized (holidayCache) {
                        holidayCache.clear(); // 清除旧数据
                        for (int i = 0; i < holidays.length(); i++) {
                            JSONObject holiday = holidays.getJSONObject(i);
                            String date = holiday.getString("date");
                            String name = holiday.getString("localName");
                            holidayCache.put(date, name);
                            Log.d("HolidayRepo", "缓存节假日: " + date + " - " + name);
                        }
                        cachedYear = year; // 更新缓存年份
                    }
                } else {
                    Log.e("HolidayRepo", "API请求失败，HTTP代码: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("HolidayRepo", "预加载节假日数据失败", e);
            } finally {
                isPreloading = false;
            }
        });
    }

    /**
     * 检查指定日期是否为节假日
     * @param date 日期，格式为yyyy-MM-dd
     * @return 如果是节假日返回true，否则返回false
     */
    public static boolean isHoliday(String date) {
        try {
            // 提取年份
            int year = Integer.parseInt(date.substring(0, 4));

            // 如果缓存中没有数据或年份不匹配，先尝试从API获取
            if (holidayCache.isEmpty() || cachedYear != year) {
                preloadHolidays(year);

                // 等待一小段时间让数据加载
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 检查缓存中是否有该日期
            synchronized (holidayCache) {
                if (holidayCache.containsKey(date)) {
                    return true;
                }
            }

            // 如果API中没有找到，尝试使用本地数据
            return isLocalHoliday(date);
        } catch (Exception e) {
            Log.e("HolidayRepo", "检查节假日失败，使用本地数据", e);
            return isLocalHoliday(date);
        }
    }

    /**
     * 获取指定日期的节假日名称
     * @param date 日期，格式为yyyy-MM-dd
     * @return 节假日名称，如果不是节假日则返回空字符串
     */
    public static String getName(String date) {
        try {
            // 提取年份
            int year = Integer.parseInt(date.substring(0, 4));

            // 如果缓存中没有数据或年份不匹配，先尝试从API获取
            if (holidayCache.isEmpty() || cachedYear != year) {
                preloadHolidays(year);

                // 等待一小段时间让数据加载
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 检查缓存中是否有该日期
            synchronized (holidayCache) {
                if (holidayCache.containsKey(date)) {
                    return holidayCache.get(date);
                }
            }

            // 如果API中没有找到，尝试使用本地数据
            return getLocalHolidayName(date);
        } catch (Exception e) {
            Log.e("HolidayRepo", "获取节假日名称失败，使用本地数据", e);
            return getLocalHolidayName(date);
        }
    }

    /**
     * 清除节假日缓存
     */
    public static void clearCache() {
        synchronized (holidayCache) {
            holidayCache.clear();
            cachedYear = -1;
        }
    }

    // 检查是否为本地节假日
    private static boolean isLocalHoliday(String date) {
        try {
            // 提取月份和日期部分 (MM-DD)
            String monthDay = date.substring(5);
            return LOCAL_HOLIDAYS.containsKey(monthDay);
        } catch (Exception e) {
            Log.e("HolidayRepo", "本地节假日检查失败", e);
            return false;
        }
    }

    // 获取本地节假日名称
    private static String getLocalHolidayName(String date) {
        try {
            // 提取月份和日期部分 (MM-DD)
            String monthDay = date.substring(5);
            return LOCAL_HOLIDAYS.getOrDefault(monthDay, "");
        } catch (Exception e) {
            Log.e("HolidayRepo", "获取本地节假日名称失败", e);
            return "";
        }
    }
}