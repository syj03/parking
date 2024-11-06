package com.example.app1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FindAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        EditText nameEditText = findViewById(R.id.etFindName);
        EditText birthdateEditText = findViewById(R.id.etFindBirthdate);
        Button findButton = findViewById(R.id.btnFindAccount);

        findButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            String birthdate = birthdateEditText.getText().toString();

            // SharedPreferences에서 사용자 정보 가져오기
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String registeredName = sharedPreferences.getString("registeredName", "");
            String registeredId = sharedPreferences.getString("registeredId", "");
            String registeredPassword = sharedPreferences.getString("registeredPassword", "");
            String registeredBirthdate = sharedPreferences.getString("registeredBirthdate", "");

            // 이름과 생년월일이 일치하는 경우
            if (name.equals(registeredName) && birthdate.equals(registeredBirthdate)) {
                // 아이디와 비밀번호를 표시
                String accountInfo = "아이디: " + registeredId + "\n비밀번호: " + registeredPassword;
                Toast.makeText(FindAccountActivity.this, accountInfo, Toast.LENGTH_LONG).show();

                // 확인 후 로그인 화면으로 이동
                Intent intent = new Intent(FindAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // 일치하지 않는 경우 오류 메시지 표시
                Toast.makeText(FindAccountActivity.this, "입력하신 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
