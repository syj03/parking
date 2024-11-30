package com.example.app1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 2;
    private static final String USER_API_URL = "http://1.237.179.199:8080/api/users";

    private ImageView profileImageView;
    private TextView nameTextView;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 뒤로 가기 버튼
        ImageButton backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(view -> finish());


        // 이름 초기화
        nameTextView = findViewById(R.id.nameTextView);

        // Intent에서 userId 가져오기
        userId = getIntent().getIntExtra("userId", -1);
        birthDate = getIntent().getStringExtra("birthDate");

        // 사용자 정보 가져오기
        new FetchUserTask().execute(userId);



        // 이름 변경 클릭 이벤트
        LinearLayout changeNicknameLayout = findViewById(R.id.change_nickname_layout);
        changeNicknameLayout.setOnClickListener(view -> showChangeNameDialog());

        // 내 리뷰 목록 클릭 이벤트
        LinearLayout myReviewListLayout = findViewById(R.id.my_review_list_layout);
        myReviewListLayout.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, MyReviewListActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    // 계정 탈퇴 클릭 이벤트
        LinearLayout deleteAccountLayout = findViewById(R.id.my_delet_layout);
        deleteAccountLayout.setOnClickListener(view -> showDeleteAccountDialog());
        // 저장소 권한 요청
        requestStoragePermission();
    }


    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이름 변경");

        final EditText input = new EditText(this);
        input.setHint("새로운 이름을 입력하세요");
        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                new UpdateUserNameTask().execute(newName);
            } else {
                Toast.makeText(ProfileActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }



    private class FetchUserTask extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... params) {
            int userId = params[0];
            try {
                URL url = new URL(USER_API_URL + "/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return new JSONObject(response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject userJson) {
            if (userJson != null) {
                try {
                    String name = userJson.getString("name");
                    nameTextView.setText(name);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", userJson.getString("email"));
                    editor.putString("password", userJson.getString("password"));
                    editor.putString("birthDate", userJson.getString("birthDate"));
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "사용자 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateUserNameTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String newName = params[0];
            try {
                URL url = new URL(USER_API_URL + "/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                SharedPreferences sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String email = sharedPrefs.getString("email", "");
                String password = sharedPrefs.getString("password", "");
                String birthDate = sharedPrefs.getString("birthDate", "");

                JSONObject updateJson = new JSONObject();
                updateJson.put("name", newName);
                updateJson.put("email", email);
                updateJson.put("password", password);
                updateJson.put("birthDate", birthDate);

                OutputStream os = connection.getOutputStream();
                os.write(updateJson.toString().getBytes("UTF-8"));
                os.close();

                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ProfileActivity.this, "이름이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                new FetchUserTask().execute(userId);
            } else {
                Toast.makeText(ProfileActivity.this, "이름 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 계정 탈퇴 확인 다이얼로그 표시
    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("계정 탈퇴");
        builder.setMessage("정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.");

        builder.setPositiveButton("확인", (dialog, which) -> new DeleteAccountTask().execute(userId));
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    // 계정 삭제를 처리하는 AsyncTask
    private class DeleteAccountTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int userId = params[0];
            try {
                URL url = new URL(USER_API_URL + "/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ProfileActivity.this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            } else {
                Toast.makeText(ProfileActivity.this, "계정 삭제에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 로그인 화면으로 이동
    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
