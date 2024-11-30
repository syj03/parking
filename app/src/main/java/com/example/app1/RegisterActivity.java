package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_API_URL = "http://1.237.179.199:8080/api/users/add"; // 서버의 회원가입 API URL
    private static final String EMAIL_CHECK_API_URL = "http://1.237.179.199:8080/api/users/check-email"; // 이메일 중복 검사 API URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText nameEditText = findViewById(R.id.etRegisterName);
        EditText emailEditText = findViewById(R.id.etRegisterId); // 이메일 입력 필드
        EditText passwordEditText = findViewById(R.id.etRegisterPassword);
        EditText confirmPasswordEditText = findViewById(R.id.etConfirmPassword);
        EditText birthdateEditText = findViewById(R.id.etBirthdate); // 생년월일 입력 필드
        Button registerButton = findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString(); // 이메일
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String birthdate = birthdateEditText.getText().toString();

            if (!password.equals(confirmPassword)) {
                // 비밀번호 불일치 경고 메시지 표시
                Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 생년월일 형식 변환 (yyyymmdd -> yyyy-MM-dd)
                String formattedBirthdate = formatBirthdate(birthdate);
                if (formattedBirthdate == null) {
                    Toast.makeText(RegisterActivity.this, "생년월일 형식이 잘못되었습니다. (올바른 형식: yyyymmdd)", Toast.LENGTH_SHORT).show();
                } else {
                    // 이메일 중복 검사 후 회원가입 진행
                    new CheckEmailTask().execute(name, email, password, formattedBirthdate);
                }
            }
        });
    }

    // 생년월일 형식 변환 메소드
    private String formatBirthdate(String birthdate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = originalFormat.parse(birthdate);
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
            return newFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 이메일 중복 검사를 수행하는 AsyncTask
    private class CheckEmailTask extends AsyncTask<Object, Void, Boolean> {
        private String name;
        private String email;
        private String password;
        private String birthdate;

        @Override
        protected Boolean doInBackground(Object... params) {
            name = (String) params[0];
            email = (String) params[1];
            password = (String) params[2];
            birthdate = (String) params[3];

            try {
                // 서버로 이메일 중복 검사 요청
                URL url = new URL(EMAIL_CHECK_API_URL + "?email=" + email);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                // HTTP_OK이면 이메일 사용 가능, HTTP_CONFLICT이면 중복
                return responseCode == HttpURLConnection.HTTP_OK;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isEmailAvailable) {
            if (isEmailAvailable) {
                // 이메일이 사용 가능하면 회원가입 진행
                new RegisterUserTask().execute(name, email, password, birthdate);
            } else {
                // 이메일 중복 경고 메시지 표시
                Toast.makeText(RegisterActivity.this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 사용자 등록을 서버로 제출하는 AsyncTask
    private class RegisterUserTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String name = params[0];
            String email = params[1];
            String password = params[2];
            String birthdate = params[3];

            try {
                // 회원가입 데이터를 JSON 객체로 생성
                JSONObject registerData = new JSONObject();
                registerData.put("name", name);
                registerData.put("email", email); // 이메일로 수정
                registerData.put("password", password);
                registerData.put("birthDate", birthdate); // 생년월일 추가

                // 서버로 POST 요청 전송
                URL url = new URL(REGISTER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(registerData.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(RegisterActivity.this, "회원가입이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                // 로그인 화면으로 이동
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
