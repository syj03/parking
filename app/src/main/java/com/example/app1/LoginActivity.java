package com.example.app1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailEditText = findViewById(R.id.etEmail);
        EditText passwordEditText = findViewById(R.id.etPassword);
        Button loginButton = findViewById(R.id.btnLogin);
        TextView signUpTextView = findViewById(R.id.tvSignUp);
        TextView findAccountTextView = findViewById(R.id.tvFindAccount);

        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // SharedPreferences에서 사용자 정보 가져오기
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String registeredId = sharedPreferences.getString("registeredId", "");
            String registeredPassword = sharedPreferences.getString("registeredPassword", "");

            if (email.equals(registeredId) && password.equals(registeredPassword)) {
                // 로그인 성공 시 MainActivity로 이동
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                // 로그인 실패 시 경고 메시지 표시
                String errorMessage = getString(R.string.login_error_message);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        signUpTextView.setOnClickListener(view -> {
            // RegisterActivity로 이동
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        findAccountTextView.setOnClickListener(view -> {
            // FindAccountActivity로 이동
            startActivity(new Intent(LoginActivity.this, FindAccountActivity.class));
        });
    }
}
