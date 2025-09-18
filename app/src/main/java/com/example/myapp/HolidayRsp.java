package com.example.myapp;

public class HolidayRsp {
    public int code;
    public Type type;

    public static class Type {
        public int type;   // 0 工作日 1 周末 2 法定
        public String cn;  // “工作日”“国庆节”
    }
}