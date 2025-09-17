package com.example.myapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

public class CustomEditText extends AppCompatEditText {
    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置背景和样式
        setBackgroundResource(R.drawable.edittext_border);
        setTextColor(ContextCompat.getColor(getContext(), R.color.gray_900));
        setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray_500));
        setTextSize(16);
        setPadding(16, 16, 16, 16);

        // 焦点变化监听
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setBackgroundResource(R.drawable.edittext_border_focused);
                } else {
                    setBackgroundResource(R.drawable.edittext_border);
                }
            }
        });
    }
}