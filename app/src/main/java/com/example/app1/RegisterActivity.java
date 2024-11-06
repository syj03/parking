package com.example.app1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText nameEditText = findViewById(R.id.etRegisterName);
        EditText idEditText = findViewById(R.id.etRegisterId);
        EditText passwordEditText = findViewById(R.id.etRegisterPassword);
        EditText confirmPasswordEditText = findViewById(R.id.etConfirmPassword);
        EditText birthdateEditText = findViewById(R.id.etBirthdate);
        Button registerButton = findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            String id = idEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String birthdate = birthdateEditText.getText().toString();

            if (!password.equals(confirmPassword)) {
                // 비밀번호 불일치 경고 메시지 표시
                Toast.makeText(RegisterActivity.this, getString(R.string.register_error_password_mismatch), Toast.LENGTH_SHORT).show();
            } else {
                // SharedPreferences를 사용하여 사용자 정보 저장
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("registeredName", name);
                editor.putString("registeredId", id);
                editor.putString("registeredPassword", password);
                editor.putString("registeredBirthdate", birthdate);
                editor.apply();

                // 성공적으로 가입 후 로그인 화면으로 이동
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
