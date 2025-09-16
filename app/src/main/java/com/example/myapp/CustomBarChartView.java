package com.example.myapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class CustomBarChartView extends View {
    private List<String> labels;
    private List<Integer> values;
    private Paint barPaint;
    private Paint textPaint;

    public CustomBarChartView(Context context) {
        super(context);
        init();
    }

    public CustomBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#3F51B5"));
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<String> labels, List<Integer> values) {
        this.labels = labels;
        this.values = values;
        invalidate(); // 重绘视图
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (labels == null || values == null || labels.size() != values.size()) {
            // 绘制提示信息
            canvas.drawText("暂无数据", getWidth() / 2, getHeight() / 2, textPaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // 计算最大值用于缩放
        int maxValue = 1;
        for (int value : values) {
            if (value > maxValue) maxValue = value;
        }

        // 绘制柱状图
        int barWidth = chartWidth / values.size() - 10;
        for (int i = 0; i < values.size(); i++) {
            int barHeight = (int) ((float) values.get(i) / maxValue * chartHeight);
            int left = padding + i * (barWidth + 10);
            int top = height - padding - barHeight;
            int right = left + barWidth;
            int bottom = height - padding;

            canvas.drawRect(left, top, right, bottom, barPaint);

            // 绘制日期标签
            String label = labels.get(i).substring(5); // 只显示月和日
            canvas.drawText(label, left + barWidth / 2, height - padding + 40, textPaint);

            // 绘制数量数值
            canvas.drawText(String.valueOf(values.get(i)), left + barWidth / 2, top - 10, textPaint);
        }
    }
}