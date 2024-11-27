package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;

    private EditText addressInput;
    private Button searchButton;
    private Spinner mapTypeSpinner;

    // 하단 메뉴 버튼
    private LinearLayout reservationListLayoutBtn, profileLayoutBtn, searchLayoutBtn;

    private static final String CLIENT_ID = "pa593595je"; // 네이버 클라이언트 ID
    private static final String CLIENT_SECRET = "GAUZAT2h82xh7TGb6lLXkm4yvblSj9xhaBfC63Dn"; // 네이버 클라이언트 시크릿
    private static final String PARKING_LOTS_API_URL = "http://1.237.179.199:8080/api/parking-lots"; // 주차장 API URL

    private int userId; // 로그인된 사용자 ID
    private String userName; // 로그인된 사용자 이름
    private String birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Intent에서 로그인된 사용자 ID와 이름 가져오기
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        userName = intent.getStringExtra("userName");
        birthDate = intent.getStringExtra("birthDate");

        // 네이버 지도 초기화
        mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(this);

        // UI 요소 초기화
        addressInput = findViewById(R.id.address_input);
        searchButton = findViewById(R.id.search_button);
        mapTypeSpinner = findViewById(R.id.map_type_spinner);

        // 하단 메뉴 버튼
        reservationListLayoutBtn = findViewById(R.id.reservation_list_layout_btn);
        profileLayoutBtn = findViewById(R.id.profile_layout_btn);
        searchLayoutBtn = findViewById(R.id.search_layout_btn);

        // 지도 유형 선택 스피너 설정
        String[] mapTypes = {"일반지도", "하이브리드지도"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mapTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (naverMap != null) {
                    naverMap.setMapType(position == 0 ? NaverMap.MapType.Basic : NaverMap.MapType.Hybrid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 주소 검색 버튼 클릭 리스너
        searchButton.setOnClickListener(v -> {
            String address = addressInput.getText().toString();
            if (!address.isEmpty()) {
                new NaverGeocodingTask().execute(address);
            } else {
                Toast.makeText(MainActivity.this, "주소를 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 하단 메뉴 버튼 리스너 설정
        setBottomButtonListeners();
    }

    private void setBottomButtonListeners() {
        View.OnClickListener listener = v -> {
            Intent intent;
            if (v.getId() == R.id.reservation_list_layout_btn) {
                intent = new Intent(MainActivity.this, ReservationListActivity.class);
            } else if (v.getId() == R.id.profile_layout_btn) {
                intent = new Intent(MainActivity.this, ProfileActivity.class);

                // 사용자 정보 전달
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("birthDate", birthDate);
            } else {
                Toast.makeText(MainActivity.this, "주변 주차장 기능이 준비 중입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(intent);
        };

        reservationListLayoutBtn.setOnClickListener(listener);
        profileLayoutBtn.setOnClickListener(listener);
        searchLayoutBtn.setOnClickListener(listener);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 초기 위치 설정 (예: 충북 제천시)
        LatLng initialLocation = new LatLng(37.1418, 128.1937);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialLocation);
        naverMap.moveCamera(cameraUpdate);

        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 주차장 데이터를 API로 불러오기
        new FetchParkingLotsTask().execute();
    }

    // NaverGeocodingTask 클래스 추가
    private class NaverGeocodingTask extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... addresses) {
            String address = addresses[0];
            try {
                String apiUrl = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", CLIENT_ID);
                connection.setRequestProperty("X-NCP-APIGW-API-KEY", CLIENT_SECRET);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray addressesArray = jsonObject.getJSONArray("addresses");

                if (addressesArray.length() > 0) {
                    JSONObject location = addressesArray.getJSONObject(0);
                    double latitude = location.getDouble("y");
                    double longitude = location.getDouble("x");
                    return new LatLng(latitude, longitude);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setCaptionText("검색된 위치");
                marker.setMap(naverMap);

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
                naverMap.moveCamera(cameraUpdate);

                Toast.makeText(MainActivity.this, "위치로 이동합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 주차장 데이터 API 호출
    private class FetchParkingLotsTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                // API 호출
                URL url = new URL(PARKING_LOTS_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // 서버 응답 코드 확인
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("FetchParkingLotsTask", "Server returned response code: " + responseCode);
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // JSON 배열 반환
                return new JSONArray(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("FetchParkingLotsTask", "Error fetching parking lots: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray parkingLots) {
            if (parkingLots != null) {
                try {
                    // 주차장 데이터를 마커로 추가
                    for (int i = 0; i < parkingLots.length(); i++) {
                        JSONObject parkingLot = parkingLots.getJSONObject(i);
                        int id = parkingLot.getInt("id");
                        String name = parkingLot.getString("name");
                        double latitude = parkingLot.getDouble("latitude");
                        double longitude = parkingLot.getDouble("longitude");

                        addParkingLotMarker(new LatLng(latitude, longitude), name, id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "주차장 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "주차장 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 지도에 주차장 마커 추가
    private void addParkingLotMarker(LatLng location, String name, int parkingLotId) {
        Marker marker = new Marker();
        marker.setPosition(location);
        marker.setCaptionText(name); // 주차장 이름 표시
        marker.setMap(naverMap);

        Log.d("AddMarker", "Marker added: " + name + " at " + location.latitude + ", " + location.longitude);

        // 마커 클릭 이벤트 설정
        marker.setOnClickListener(overlay -> {
            try {
                // 마커 클릭 시 ParkingDetailsActivity로 이동
                Intent intent = new Intent(MainActivity.this, ParkingDetailsActivity.class);
                intent.putExtra("parkingLotId", parkingLotId); // 주차장 ID 전달
                intent.putExtra("parkingLotName", name); // 주차장 이름 전달
                intent.putExtra("userId", userId); // 사용자 ID 전달
                intent.putExtra("userName", userName); // 사용자 이름 전달
                intent.putExtra("birthDate", birthDate);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("MarkerClickListener", "Error starting ParkingDetailsActivity: " + e.getMessage());
            }
            return true;
        });
    }
}

