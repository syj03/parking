package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private static final String USERS_API_URL = "http://1.237.179.199:8080/api/users"; // 서버의 사용자 조회 API URL

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

            if (!email.isEmpty() && !password.isEmpty()) {
                // 서버에서 사용자 정보 조회
                new LoginUserTask().execute(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
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

    // 로그인 요청을 서버로 보내는 AsyncTask
    private class LoginUserTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            try {
                // 서버로부터 사용자 정보 목록을 가져옴
                URL url = new URL(USERS_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 서버에서 가져온 사용자 정보 파싱
                JSONArray usersArray = new JSONArray(response.toString());
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject user = usersArray.getJSONObject(i);
                    String registeredEmail = user.getString("email");
                    String registeredPassword = user.getString("password");

                    // 이메일과 비밀번호가 일치하는지 확인
                    if (email.equals(registeredEmail) && password.equals(registeredPassword)) {
                        return user; // 로그인 성공 시 사용자 정보 반환
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null; // 로그인 실패 시 null 반환
        }

        @Override
        protected void onPostExecute(JSONObject user) {
            if (user != null) {
                try {
                    int userId = user.getInt("id");
                    String userName = user.getString("name");
                    String email = user.getString("email");
                    String password = user.getString("password");
                    String birthDate = user.getString("birthDate");

                    //Toast.makeText(LoginActivity.this, "로그인 id값: " + userId, Toast.LENGTH_SHORT).show();

                    // 로그인 성공 시 MainActivity로 이동하며 사용자 정보 전달
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userName", userName);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("birthDate", birthDate);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "로그인 실패: 아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
