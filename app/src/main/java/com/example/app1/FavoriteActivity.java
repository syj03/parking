package com.example.app1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout favoritesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        favoritesContainer = findViewById(R.id.favorites_container);

        // 즐겨찾기 목록 로드
        loadFavorites();
    }

    private void loadFavorites() {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        String savedFavorites = sharedPreferences.getString("parkingLots", "");

        // 즐겨찾기가 없는 경우 처리
        if (savedFavorites.isEmpty()) {
            Toast.makeText(this, "저장된 즐겨찾기가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 저장된 즐겨찾기 목록 파싱
        String[] favorites = savedFavorites.split(";");
        for (String favorite : favorites) {
            String[] details = favorite.split(",");
            if (details.length != 3) continue;

            String name = details[0];
            double lat = Double.parseDouble(details[1]);
            double lng = Double.parseDouble(details[2]);

            // 즐겨찾기 항목 추가
            addFavoriteItem(name, lat, lng);
        }
    }

    private void addFavoriteItem(String name, double lat, double lng) {
        // 즐겨찾기 항목 레이아웃 생성
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(16, 16, 16, 16);

        // 주차장 이름 텍스트뷰 생성
        TextView textView = new TextView(this);
        textView.setText(name);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        textView.setTextSize(18);

        itemLayout.addView(textView);

        // 삭제 버튼 생성
        Button deleteButton = new Button(this);
        deleteButton.setText("삭제");
        deleteButton.setOnClickListener(v -> {
            removeFavorite(name, lat, lng);
        });

        itemLayout.addView(deleteButton);

        // 클릭 이벤트 (예약 및 리뷰 화면으로 이동)
        itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, ParkingDetailsActivity.class);
            intent.putExtra("parkingLotName", name);
            intent.putExtra("latitude", lat);
            intent.putExtra("longitude", lng);
            startActivity(intent);
        });

        // 컨테이너에 즐겨찾기 항목 추가
        favoritesContainer.addView(itemLayout);
    }
    

    private void removeFavorite(String name, double lat, double lng) {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        String savedFavorites = sharedPreferences.getString("parkingLots", "");

        if (!savedFavorites.isEmpty()) {
            String[] favorites = savedFavorites.split(";");
            StringBuilder newFavorites = new StringBuilder();

            for (String favorite : favorites) {
                if (!favorite.equals(name + "," + lat + "," + lng)) {
                    if (newFavorites.length() > 0) {
                        newFavorites.append(";");
                    }
                    newFavorites.append(favorite);
                }
            }

            // 업데이트된 즐겨찾기 저장
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("parkingLots", newFavorites.toString());
            editor.apply();

            // UI 업데이트
            favoritesContainer.removeAllViews();
            loadFavorites();

            Toast.makeText(this, name + "이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }


}
