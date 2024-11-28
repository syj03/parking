package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyReviewActivity extends AppCompatActivity {

    private static final String REVIEW_API_URL = "http://1.237.179.199:8080/api/reviews/add"; // 리뷰 API URL
    private static final String TAG = "MyReviewActivity";

    private RatingBar ratingBar;
    private EditText reviewEditText;
    private Button submitReviewButton;
    private int parkingLotId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_review);

        ratingBar = findViewById(R.id.rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        submitReviewButton = findViewById(R.id.submit_review_button);

        // 전달된 주차장 ID와 사용자 ID 받기
        parkingLotId = getIntent().getIntExtra("parkingLotId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        // 리뷰 제출 버튼 클릭 이벤트
        submitReviewButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String reviewContent = reviewEditText.getText().toString();

            if (rating == 0 || reviewContent.isEmpty()) {
                Toast.makeText(MyReviewActivity.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                // 서버에 리뷰 전송
                new SubmitReviewTask().execute(parkingLotId, userId, rating, reviewContent);
            }
        });
    }

    // 리뷰 정보를 서버로 전송하기 위한 AsyncTask
    private class SubmitReviewTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            int parkingLotId = (int) params[0];
            int userId = (int) params[1];
            float rating = (float) params[2]; // rating을 float로 변경
            String reviewText = (String) params[3];

            try {
                // 리뷰 데이터를 JSON 객체로 생성
                JSONObject reviewData = new JSONObject();
                reviewData.put("parkingLotId", parkingLotId); // 정수형으로 전송
                reviewData.put("userId", userId); // 정수형으로 전송
                reviewData.put("rating", rating); // float형으로 전송
                reviewData.put("reviewText", reviewText); // 문자열로 전송

                Log.d(TAG, "전송할 리뷰 데이터: " + reviewData.toString());

                // 서버로 POST 요청 전송
                URL url = new URL(REVIEW_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 연결 시간 초과 시간 설정 (10초)
                connection.setReadTimeout(10000); // 읽기 시간 초과 시간 설정 (10초)

                OutputStream os = connection.getOutputStream();
                os.write(reviewData.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "서버 응답 코드: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    Log.e(TAG, "서버 에러 응답: " + errorResponse.toString());
                    return false;
                }

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "리뷰 제출 중 오류 발생: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MyReviewActivity.this, "리뷰가 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();

                // 리뷰 작성 완료 후 결과 전달
                Intent intent = new Intent();
                intent.putExtra("newReviewAdded", true); // 새로운 리뷰가 추가되었음을 알림
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(MyReviewActivity.this, "리뷰 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }




}