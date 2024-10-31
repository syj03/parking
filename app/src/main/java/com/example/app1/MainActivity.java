package com.example.app1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.naver.maps.map.widget.CompassView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;

    private EditText addressInput;
    private Button searchButton;
    private Spinner mapTypeSpinner;

    private CompassView compassView; // 나침반 뷰 추가

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(this);

        addressInput = findViewById(R.id.address_input); // 주소 입력 필드
        searchButton = findViewById(R.id.search_button); // 검색 버튼
        compassView = findViewById(R.id.compass_view); // 나침반 뷰 초기화
        mapTypeSpinner = findViewById(R.id.map_type_spinner); // 드롭다운 버튼 초기화

        // 지도 유형 선택을 위한 스피너 설정
        String[] mapTypes = {"일반지도", "하이브리드지도"};  //드랍다운 버튼을 눌렀을때 뜨는 옵션목록
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mapTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (naverMap != null) {
                    switch (position) {
                        case 0:
                            naverMap.setMapType(NaverMap.MapType.Navi); //일반지도
                            break;
                        case 1:
                            naverMap.setMapType(NaverMap.MapType.NaviHybrid); //하이브리드지도
                            break;

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무 동작도 하지 않음
            }
        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressInput.getText().toString();
                if (!address.isEmpty()) {
                    // 주소를 이용해 좌표 검색
                    new GeocodingTask().execute(address);
                } else {
                    Toast.makeText(MainActivity.this, "주소를 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });



        ImageButton favoriteBtn = findViewById(R.id.favorite_btn);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });


        ImageButton profile_btn = findViewById(R.id.profile_btn);
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });




    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        naverMap.setLocationTrackingMode(LocationTrackingMode.Face); // 지도에 내 위치 표시
        naverMap.setMapType(NaverMap.MapType.Navi);

        UiSettings uiSettings = naverMap.getUiSettings(); // 인터페이스 옵션 모음
        uiSettings.setCompassEnabled(false); // 기본 나침반 비활성화
        uiSettings.setZoomControlEnabled(false); // 줌 컨트롤 버튼 비활성화
        uiSettings.setLocationButtonEnabled(true); // 내 위치로 이동 버튼


        // 나침반 뷰와 지도 연결
        compassView.setMap(naverMap);
    }

    // Geocoding API 호출
    private class GeocodingTask extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... strings) {
            String address = strings[0];
            String apiKey = "AIzaSyA-Jhr6zu8fNMtG8miSBugIKmtx7yCmmGE";  // 구글 Geocoding API 키
            String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // JSON 데이터 파싱
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray results = jsonObject.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    return new LatLng(lat, lng);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                // 지도에 마커 표시 및 위치 이동
                if (marker == null) {
                    marker = new Marker();
                }

                marker.setPosition(latLng);
                marker.setMap(naverMap);

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
                naverMap.moveCamera(cameraUpdate);
            } else {
                Toast.makeText(MainActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
