package com.example.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private CustomEditText etUsername, etPassword;
    private Button btnLogin, btnSelectAvatar, btnRegister; // 添加注册按钮引用
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private Uri avatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        imageView = findViewById(R.id.imageView);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSelectAvatar = findViewById(R.id.btnSelectAvatar);
        btnRegister = findViewById(R.id.btnRegister); // 初始化注册按钮
        progressBar = findViewById(R.id.progressBar);

        // 初始化数据库
        dbHelper = new DatabaseHelper(this);

        // 选择头像按钮点击事件
        btnSelectAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAvatar();
            }
        });

        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void selectAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            imageView.setImageURI(avatarUri);
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示进度条
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false); // 禁用注册按钮

        // 模拟登录过程
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isValid = dbHelper.checkUser(username, password);
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true); // 重新启用注册按钮

                if (isValid) {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    // 跳转到计划列表页面
                    Intent intent = new Intent(LoginActivity.this, PlanListActivity.class);
                    intent.putExtra("username", username);
                    if (avatarUri != null) {
                        intent.putExtra("avatar", avatarUri.toString());
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }, 2000); // 延迟2秒模拟网络请求
    }
}