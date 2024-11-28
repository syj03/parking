package com.example.app1;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class ReservationActivity extends AppCompatActivity {

    private TextView selectedDateText, selectedTimeText;
    private String selectedDate = null;
    private String selectedTime = null;
    private int parkingLotId, userId;

    private static final String RESERVATION_API_URL = "http://1.237.179.199:8080/api/reservations/add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Intent로 전달된 데이터 가져오기
        parkingLotId = getIntent().getIntExtra("parkingLotId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        selectedDateText = findViewById(R.id.selected_date_text);
        selectedTimeText = findViewById(R.id.selected_time_text);
        Button selectDateButton = findViewById(R.id.select_date_button);
        Button selectTimeButton = findViewById(R.id.select_time_button);
        Button confirmReservationButton = findViewById(R.id.confirm_reservation_button);

        // 날짜 선택 버튼 클릭 이벤트
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        // 시간 선택 버튼 클릭 이벤트
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());

        // 예약 확인 버튼 클릭 이벤트
        confirmReservationButton.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "날짜와 시간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                String reservationTime = selectedDate + "T" + selectedTime; // 예약 시간 포맷: yyyy-MM-ddTHH:mm:ss
                new ConfirmReservationTask().execute(parkingLotId, userId, reservationTime);
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            selectedDateText.setText("선택된 날짜: " + selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedTime = String.format("%02d:%02d:00", hourOfDay, minute1);
            selectedTimeText.setText("선택된 시간: " + selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private class ConfirmReservationTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            int parkingLotId = (int) params[0];
            int userId = (int) params[1];
            String reservationTime = (String) params[2];

            try {
                URL url = new URL(RESERVATION_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                // JSON 데이터 생성
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("parkingLotId", parkingLotId);
                jsonBody.put("userId", userId);
                jsonBody.put("reservationTime", reservationTime);
                jsonBody.put("reservedSlots", 1); // 기본 슬롯
                jsonBody.put("status", "RESERVED"); // 상태 값

                // 서버로 전송
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ReservationActivity.this, "예약이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ReservationActivity.this, "예약에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
