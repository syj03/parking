package com.example.app1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyReviewListActivity extends AppCompatActivity {

    private static final String REVIEWS_API_URL = "http://1.237.179.199:8080/api/reviews"; // 리뷰 API URL
    private int userId;

    private RecyclerView recyclerView;
    private TextView emptyView; // 안내 문구를 위한 TextView
    private ReviewAdapter reviewAdapter;
    private ArrayList<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_review_list);

        // Intent에서 사용자 ID 받기
        userId = getIntent().getIntExtra("userId", -1);

        // RecyclerView와 TextView 초기화
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_view); // 안내 문구 뷰

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);

        // 리뷰 데이터 가져오기
        new FetchReviewsTask().execute(userId);
    }

    private class FetchReviewsTask extends AsyncTask<Integer, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Integer... params) {
            int userId = params[0];
            try {
                URL url = new URL(REVIEWS_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 전체 리뷰 데이터 가져오기
                    JSONArray allReviews = new JSONArray(response.toString());
                    JSONArray userReviews = new JSONArray();

                    // 현재 사용자 리뷰만 필터링
                    for (int i = 0; i < allReviews.length(); i++) {
                        JSONObject review = allReviews.getJSONObject(i);
                        JSONObject user = review.getJSONObject("user");

                        if (user.getInt("id") == userId) {
                            userReviews.put(review); // 사용자 리뷰만 추가
                        }
                    }

                    return userReviews;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray reviews) {
            if (reviews != null) {
                try {
                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject reviewJson = reviews.getJSONObject(i);
                        JSONObject parkingLotJson = reviewJson.getJSONObject("parkingLot");

                        int id = reviewJson.getInt("id");
                        String parkingLotName = parkingLotJson.getString("name");
                        String reviewText = reviewJson.getString("reviewText");
                        String date = reviewJson.getString("createdAt");
                        int rating = reviewJson.getInt("rating");

                        reviewList.add(new Review(id, parkingLotName, reviewText, rating, date));
                    }
                    toggleEmptyView(); // 리뷰 상태에 따라 UI 갱신
                    reviewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MyReviewListActivity.this, "리뷰 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyReviewListActivity.this, "리뷰를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 리뷰가 있는지 여부에 따라 UI 갱신
    private void toggleEmptyView() {
        if (reviewList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
