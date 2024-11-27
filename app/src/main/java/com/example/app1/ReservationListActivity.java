package com.example.app1;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReservationListActivity extends AppCompatActivity {

    private RecyclerView reservationRecyclerView;
    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        // RecyclerView 초기화
        reservationRecyclerView = findViewById(R.id.reservation_recycler_view);
        reservationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 예약 목록 초기화 (예시 데이터)
        reservationList = new ArrayList<>();
        reservationList.add(new Reservation("주차장 A", "2024-11-26 14:00"));
        reservationList.add(new Reservation("주차장 B", "2024-11-27 10:30"));

        // 어댑터 설정
        reservationAdapter = new ReservationAdapter(reservationList);
        reservationRecyclerView.setAdapter(reservationAdapter);
    }
}
