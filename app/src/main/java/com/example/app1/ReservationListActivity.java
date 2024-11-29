package com.example.app1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;

public class ReservationListActivity extends AppCompatActivity {

    private static final String RESERVATION_API_URL = "http://1.237.179.199:8080/api/reservations";
    private static final String PARKING_LOTS_API_URL = "http://1.237.179.199:8080/api/parking-lots";
    private int userId;
    private ArrayList<Reservation> reservationList;
    private ReservationAdapter reservationAdapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private HashMap<Integer, String> parkingLotCache = new HashMap<>(); // 주차장 데이터 캐시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        userId = getIntent().getIntExtra("userId", -1);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(reservationList, (reservationId, position) ->
                new DeleteReservationTask().execute(reservationId, position)
        );
        recyclerView.setAdapter(reservationAdapter);

        new FetchParkingLotsTask().execute(); // 주차장 데이터 먼저 가져오기
    }

    private class FetchParkingLotsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(PARKING_LOTS_API_URL);
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

                    JSONArray parkingLots = new JSONArray(response.toString());
                    for (int i = 0; i < parkingLots.length(); i++) {
                        JSONObject parkingLot = parkingLots.getJSONObject(i);
                        int id = parkingLot.getInt("id");
                        String name = parkingLot.getString("name");
                        parkingLotCache.put(id, name); // 주차장 ID와 이름을 캐시에 저장
                    }

                    return true;
                } else {
                    Log.e("FetchParkingLotsTask", "Server returned non-OK response code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                new FetchReservationsTask().execute(); // 예약 데이터 가져오기
            } else {
                Toast.makeText(ReservationListActivity.this, "주차장 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchReservationsTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                URL url = new URL(RESERVATION_API_URL);
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
                } else {
                    Log.e("FetchReservationsTask", "Server returned non-OK response code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray reservations) {
            if (reservations != null) {
                try {
                    for (int i = 0; i < reservations.length(); i++) {
                        JSONObject reservationJson = reservations.getJSONObject(i);

                        if (reservationJson.getInt("userId") == userId) {
                            int id = reservationJson.getInt("id");
                            String reservationTime = reservationJson.getString("reservationTime");
                            int parkingLotId = reservationJson.getInt("parkingLotId");
                            String status = reservationJson.getString("status");

                            String parkingLotName = parkingLotCache.getOrDefault(parkingLotId, "알 수 없는 주차장");
                            reservationList.add(new Reservation(id, parkingLotName, reservationTime, status.equals("RESERVED") ? "예약완료" : status));
                        }
                    }

                    reservationAdapter.notifyDataSetChanged();
                    toggleEmptyView();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReservationListActivity.this, "예약 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReservationListActivity.this, "예약 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteReservationTask extends AsyncTask<Integer, Void, Boolean> {
        private int position;

        @Override
        protected Boolean doInBackground(Integer... params) {
            int reservationId = params[0];
            position = params[1];

            try {
                URL url = new URL(RESERVATION_API_URL + "/" + reservationId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_NO_CONTENT;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ReservationListActivity.this, "예약이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                reservationList.remove(position);
                reservationAdapter.notifyItemRemoved(position);
                toggleEmptyView();
            } else {
                Toast.makeText(ReservationListActivity.this, "예약 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleEmptyView() {
        if (reservationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
