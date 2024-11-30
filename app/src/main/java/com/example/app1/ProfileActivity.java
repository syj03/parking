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

    private static final int PICK_IMAGE_REQUEST = 1;
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

        // 프로필 이미지 초기화
        profileImageView = findViewById(R.id.imageView3);
        loadProfileImage();

        // 이름 초기화
        nameTextView = findViewById(R.id.nameTextView);

        // Intent에서 userId 가져오기
        userId = getIntent().getIntExtra("userId", -1);
        birthDate = getIntent().getStringExtra("birthDate");

        // 사용자 정보 가져오기
        new FetchUserTask().execute(userId);

        // 프로필 사진 변경 클릭 이벤트
        LinearLayout changeProfileImageLayout = findViewById(R.id.change_profile_image_layout);
        changeProfileImageLayout.setOnClickListener(view -> openGallery());

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

        // 저장소 권한 요청
        requestStoragePermission();
    }

    private void loadProfileImage() {
        String profilePictureUri = sharedPreferences.getString("profilePictureUri", null);
        if (profilePictureUri != null) {
            Uri imageUri = Uri.parse(profilePictureUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            profileImageView.setImageResource(R.drawable.plogo); // 기본 이미지 변경
        }
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profilePictureUri", imageUri.toString());
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
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
}
