package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;

public class FindAccountActivity extends AppCompatActivity {

    private static final String FIND_ACCOUNT_API_URL = "http://1.237.179.199:8080/api/users"; // 서버의 사용자 조회 API URL
    private static final String TAG = "FindAccountActivity";

    private EditText nameEditText;
    private EditText birthdateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        nameEditText = findViewById(R.id.nameEditText);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        Button findButton = findViewById(R.id.btnFindAccount);

        findButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String birthdate = birthdateEditText.getText().toString().trim();

            // 생년월일 입력 형식 확인 및 변환
            if (birthdate.length() == 8) {
                birthdate = birthdate.substring(0, 4) + "-" + birthdate.substring(4, 6) + "-" + birthdate.substring(6, 8);
            }

            if (name.isEmpty() || birthdate.isEmpty()) {
                Toast.makeText(FindAccountActivity.this, "이름과 생년월일을 입력하세요.", Toast.LENGTH_SHORT).show();
            } else {
                // 서버에서 사용자 정보 조회
                new FindUserTask().execute(name, birthdate);
            }
        });
    }

    private class FindUserTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String name = params[0];
            String birthdate = params[1];
            try {
                // URL에 매개변수 추가 (이름과 생년월일)
                String encodedName = URLEncoder.encode(name, "UTF-8");
                String encodedBirthdate = URLEncoder.encode(birthdate, "UTF-8");
                URL url = new URL(FIND_ACCOUNT_API_URL + "?name=" + encodedName + "&birthDate=" + encodedBirthdate);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Log.d(TAG, "Response: " + response.toString());
                    JSONArray users = new JSONArray(response.toString());

                    // 이름과 생년월일이 일치하는 사용자 검색
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        String registeredName = user.optString("name", "");
                        String registeredBirthDate = user.optString("birthDate", "");

                        if (registeredName.equalsIgnoreCase(name) && registeredBirthDate.equals(birthdate)) {
                            return user; // 일치하는 사용자 반환
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.getMessage());
            }
            return null; // 일치하는 사용자가 없거나 오류 발생
        }

        @Override
        protected void onPostExecute(JSONObject user) {
            if (user != null) {
                try {
                    // 이메일과 비밀번호 가져오기
                    String email = user.optString("email", "이메일 정보 없음");
                    String password = user.optString("password", "비밀번호 정보 없음");

                    // 이메일과 비밀번호를 표시
                    String accountInfo = "아이디(이메일): " + email + "\n비밀번호: " + password;
                    Toast.makeText(FindAccountActivity.this, accountInfo, Toast.LENGTH_LONG).show();

                    // 확인 후 로그인 화면으로 이동
                    Intent intent = new Intent(FindAccountActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FindAccountActivity.this, "사용자 정보를 처리하는 데 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 일치하는 사용자가 없는 경우
                Toast.makeText(FindAccountActivity.this, "입력하신 정보와 일치하는 계정을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
