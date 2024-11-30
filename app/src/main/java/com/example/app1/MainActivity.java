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
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.CompassView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource; // 위치 추적 소스
    private LatLng currentLocation; // 현재 위치
    private MapView mapView;
    private NaverMap naverMap;


    private EditText addressInput;
    private Button searchButton;
    private Spinner mapTypeSpinner;
    // 하단 메뉴 버튼
    private LinearLayout reservationListLayoutBtn, profileLayoutBtn, searchLayoutBtn;


    private static final String CLIENT_ID = "pa593595je"; // 네이버 클라이언트 ID
    private static final String CLIENT_SECRET = "GAUZAT2h82xh7TGb6lLXkm4yvblSj9xhaBfC63Dn"; // 네이버 클라이언트 시크릿
    private static final String PARKING_LOTS_API_URL = "http://1.237.179.199:8080/api/parking-lots";

    private int userId; // 로그인된 사용자 ID
    private String userName;
    private String birthDate;
    private ArrayList<MarkerData> parkingLotMarkers = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FusedLocationSource 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // Intent에서 사용자 정보 가져오기
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        userName = intent.getStringExtra("userName");

        // 네이버 지도 초기화
        mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(this);

        // 하단 버튼 설정
        findViewById(R.id.search_layout_btn).setOnClickListener(v -> moveToNearestParkingLot());


        // Intent에서 로그인된 사용자 ID와 이름 가져오기
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
                intent.putExtra("userId", userId); // userId 전달
                startActivity(intent);
                Log.d("MainActivity", "Passing userId: " + userId); // 디버그용 로그 추가
            } else if (v.getId() == R.id.profile_layout_btn) {
                intent = new Intent(MainActivity.this, ProfileActivity.class);

                // 사용자 정보 전달
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("birthDate", birthDate);
                startActivity(intent);
            } else {
                moveToNearestParkingLot();
            }

        };

        reservationListLayoutBtn.setOnClickListener(listener);
        profileLayoutBtn.setOnClickListener(listener);
        searchLayoutBtn.setOnClickListener(listener);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // UiSettings로 나침반 활성화
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(false); // NaverMap의 기본 나침반 비활성화 (커스텀 CompassView 사용)
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setLocationButtonEnabled(true);

        // FusedLocationSource 연결
        naverMap.setLocationSource(locationSource);
        // 내 위치 오버레이 활성화
        naverMap.getLocationOverlay().setVisible(true); // 내 위치 표시
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow); // 내 위치 추적



        // CompassView 초기화 및 연결
        CompassView compassView = findViewById(R.id.compass_view);
        compassView.setMap(naverMap); // CompassView와 NaverMap 연결

        // 지도 회전 시 CompassView 동작 확인용 로그
        naverMap.addOnCameraChangeListener((reason, animated) -> {
            Log.d("CompassView", "지도 회전: 이유=" + reason + ", 애니메이션=" + animated);
        });

        // 위치 변경 리스너
        naverMap.addOnLocationChangeListener(location -> {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("Location", "현재 위치: " + currentLocation.latitude + ", " + currentLocation.longitude);
        });

        // 주차장 데이터를 API로 불러오기
        new FetchParkingLotsTask().execute();
    }



    private class FetchParkingLotsTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... voids) {
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

                    return new JSONArray(response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray parkingLots) {
            if (parkingLots != null) {
                try {
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

    private void addParkingLotMarker(LatLng location, String name, int parkingLotId) {
        Marker marker = new Marker();
        marker.setPosition(location);
        marker.setCaptionText(name);
        marker.setIconTintColor(0xFF0000FF); // 파란색 마커
        marker.setMap(naverMap);

        parkingLotMarkers.add(new MarkerData(parkingLotId, name, location));

        marker.setOnClickListener(overlay -> {
            Intent intent = new Intent(MainActivity.this, ParkingDetailsActivity.class);
            intent.putExtra("parkingLotId", parkingLotId);
            intent.putExtra("parkingLotName", name);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;
        });
    }


    private void moveToNearestParkingLot() {
        if (currentLocation == null) {
            Toast.makeText(this, "현재 위치를 가져오는 중입니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_SHORT).show();
            Log.d("NearestParkingLot", "현재 위치가 null입니다.");
            return;
        }

        if (parkingLotMarkers.isEmpty()) {
            Toast.makeText(this, "주차장 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            Log.d("NearestParkingLot", "주차장 데이터가 비어 있습니다.");
            return;
        }

        // 가장 가까운 주차장 찾기
        MarkerData nearestParkingLot = null;
        double minDistance = Double.MAX_VALUE;

        for (MarkerData markerData : parkingLotMarkers) {
            double distance = calculateDistance(currentLocation, markerData.getLocation());
            Log.d("DistanceCheck", "주차장: " + markerData.getName() + ", 거리: " + distance);
            if (distance < minDistance) {
                minDistance = distance;
                nearestParkingLot = markerData;
            }
        }

        if (nearestParkingLot != null) {
            Log.d("NearestParkingLot", "가장 가까운 주차장: " + nearestParkingLot.getName() + ", 거리: " + minDistance);

            // 바로 ParkingDetailsActivity로 이동
            Intent intent = new Intent(MainActivity.this, ParkingDetailsActivity.class);
            intent.putExtra("parkingLotId", nearestParkingLot.getId());
            intent.putExtra("parkingLotName", nearestParkingLot.getName());
            intent.putExtra("userId", userId);
            startActivity(intent);

            Toast.makeText(this, "가장 가까운 주차장으로 이동합니다: " + nearestParkingLot.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "주변에 주차장이 없습니다.", Toast.LENGTH_SHORT).show();
            Log.d("NearestParkingLot", "가까운 주차장을 찾을 수 없습니다.");
        }
    }

    private double calculateDistance(LatLng from, LatLng to) {
        double earthRadius = 6371e3; // meters
        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double deltaLat = Math.toRadians(to.latitude - from.latitude);
        double deltaLon = Math.toRadians(to.longitude - from.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private static class MarkerData {
        private final int id;
        private final String name;
        private final LatLng location;

        public MarkerData(int id, String name, LatLng location) {
            this.id = id;
            this.name = name;
            this.location = location;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public LatLng getLocation() {
            return location;
        }
    }

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
                marker.setIconTintColor(0xFF00FF00); // 초록색 마커
                marker.setMap(naverMap);

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
                naverMap.moveCamera(cameraUpdate);

                Toast.makeText(MainActivity.this, "위치로 이동합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
