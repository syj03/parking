package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ParkingDetailsActivity extends AppCompatActivity {

    private static final String REVIEWS_API_URL = "http://1.237.179.199:8080/api/reviews";
    private int parkingLotId;
    private String parkingLotName;
    private int userId;

    private RecyclerView reviewRecyclerView;
    private ReviewAdapter reviewAdapter;
    private ArrayList<Review> reviewList;
    private TextView reviewEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_details);

        // Retrieve data passed via Intent
        Intent intent = getIntent();
        parkingLotId = intent.getIntExtra("parkingLotId", -1);
        parkingLotName = intent.getStringExtra("parkingLotName");
        userId = intent.getIntExtra("userId", -1);

        // Initialize UI components
        TextView parkingLotNameTextView = findViewById(R.id.parking_lot_name);
        Button reserveButton = findViewById(R.id.reserve_button);
        Button reviewButton = findViewById(R.id.review_button);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);
        reviewEmptyView = findViewById(R.id.review_empty_view);

        parkingLotNameTextView.setText(parkingLotName);

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewRecyclerView.setAdapter(reviewAdapter);

        // Fetch reviews for this parking lot
        new FetchReviewsTask().execute(parkingLotId);

        // Set up click listeners
        reserveButton.setOnClickListener(v -> openReservationActivity());
        reviewButton.setOnClickListener(v -> openReviewActivity());
    }

    private void openReservationActivity() {
        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putExtra("parkingLotId", parkingLotId);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void openReviewActivity() {
        Intent intent = new Intent(this, MyReviewActivity.class);
        intent.putExtra("parkingLotId", parkingLotId);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private class FetchReviewsTask extends AsyncTask<Integer, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Integer... params) {
            int parkingLotId = params[0];
            try {
                URL url = new URL(REVIEWS_API_URL + "?parkingLotId=" + parkingLotId);
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

                    return new JSONArray(response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray reviews) {
            if (reviews != null && reviews.length() > 0) {
                try {
                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject reviewJson = reviews.getJSONObject(i);

                        int id = reviewJson.getInt("id");
                        String reviewText = reviewJson.getString("reviewText");
                        int rating = reviewJson.getInt("rating");
                        String date = reviewJson.getString("createdAt");

                        reviewList.add(new Review(id, parkingLotName, reviewText, rating, date));
                    }
                    reviewAdapter.notifyDataSetChanged();
                    toggleEmptyView(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ParkingDetailsActivity.this, "리뷰 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                toggleEmptyView(true);
            }
        }
    }

    private void toggleEmptyView(boolean showEmpty) {
        if (showEmpty) {
            reviewRecyclerView.setVisibility(View.GONE);
            reviewEmptyView.setVisibility(View.VISIBLE);
        } else {
            reviewRecyclerView.setVisibility(View.VISIBLE);
            reviewEmptyView.setVisibility(View.GONE);
        }
    }
}
