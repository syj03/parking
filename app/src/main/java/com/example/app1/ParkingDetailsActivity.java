package com.example.app1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ParkingDetailsActivity extends AppCompatActivity {

    private TextView parkingLotNameText;
    private Button reserveButton, reviewButton;
    private int parkingLotId;
    private String parkingLotName;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_details);

        parkingLotNameText = findViewById(R.id.parking_lot_name);
        reserveButton = findViewById(R.id.reserve_button);
        reviewButton = findViewById(R.id.review_button);

        // 전달된 데이터 받기
        parkingLotId = getIntent().getIntExtra("parkingLotId", -1);
        parkingLotName = getIntent().getStringExtra("parkingLotName");
        userId = getIntent().getIntExtra("userId", -1);

        // 주차장 이름 설정
        parkingLotNameText.setText(parkingLotName);

        // 예약 버튼 클릭 이벤트
        reserveButton.setOnClickListener(v -> {
            // 사용자 고유 ID와 주차장 ID를 토스트 메시지로 출력
            //.makeText(ParkingDetailsActivity.this, "사용자 ID: " + userId + ", 주차장 ID: " + parkingLotId, Toast.LENGTH_LONG).show();

            // 예약 화면으로 이동
            Intent intent = new Intent(ParkingDetailsActivity.this, ReservationActivity.class);
            intent.putExtra("parkingLotId", parkingLotId);
            intent.putExtra("userId", userId);
            intent.putExtra("parkingLotName", parkingLotName);
            startActivity(intent);
        });

        // 리뷰 버튼 클릭 이벤트
        reviewButton.setOnClickListener(v -> {
            // 리뷰 작성 화면으로 이동
            Intent intent = new Intent(ParkingDetailsActivity.this, MyReviewActivity.class);
            intent.putExtra("parkingLotId", parkingLotId);
            intent.putExtra("userId", userId);

            // 리뷰 작성 결과를 받기 위해 startActivityForResult 사용
            startActivityForResult(intent, 200); // 200은 REQUEST_CODE
        });
    }

    // 리뷰 작성 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            boolean newReviewAdded = data.getBooleanExtra("newReviewAdded", false);
            if (newReviewAdded) {
                Toast.makeText(this, "리뷰가 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
