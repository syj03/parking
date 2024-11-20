package com.example.app1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
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

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private ImageView profileImageView;
    private TextView nameTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 뒤로 가기 버튼
        ImageButton backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(view -> finish());

        // 프로필 이미지 초기화
        profileImageView = findViewById(R.id.imageView3);
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
            profileImageView.setImageResource(R.drawable.profilepicture);
        }

        // 이름 초기화
        nameTextView = findViewById(R.id.nameTextView);
        String registeredName = sharedPreferences.getString("registeredName", "name");
        nameTextView.setText(registeredName);

        // 프로필 사진 변경 클릭 이벤트
        LinearLayout changeProfileImageLayout = findViewById(R.id.change_profile_image_layout);
        changeProfileImageLayout.setOnClickListener(view -> openGallery());

        // 이름 변경 클릭 이벤트
        LinearLayout changeNicknameLayout = findViewById(R.id.change_nickname_layout);
        changeNicknameLayout.setOnClickListener(view -> showChangeNameDialog());

        // 내 리뷰 목록 클릭 이벤트
        LinearLayout myReviewListLayout = findViewById(R.id.my_review_list_layout);
        myReviewListLayout.setOnClickListener(view ->
                Toast.makeText(ProfileActivity.this, "내 리뷰 목록 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
        );

        // 저장소 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // 이름 변경 다이얼로그 표시 메서드
    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이름 변경");

        // 사용자로부터 이름을 입력받기 위한 EditText 설정
        final EditText input = new EditText(this);
        input.setHint("새로운 이름을 입력하세요");
        builder.setView(input);

        // 확인 버튼
        builder.setPositiveButton("확인", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                // SharedPreferences에 새 이름 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("registeredName", newName);
                editor.apply();

                // TextView에 새 이름 표시
                nameTextView.setText(newName);
                Toast.makeText(ProfileActivity.this, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 취소 버튼
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 갤러리에서 이미지 선택하는 메서드
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 이미지 선택 후 결과 처리 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap); // 프로필 이미지 업데이트

                // 선택된 이미지 URI를 SharedPreferences에 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profilePictureUri", imageUri.toString());
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "저장소 접근 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
