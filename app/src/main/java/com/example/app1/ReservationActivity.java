package com.example.app1;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class ReservationActivity extends AppCompatActivity {
    private static final String URL = "http://1.237.179.199:8080/api/reservations/add";
    private EditText reservationDateInput, reservationTimeInput, reservedSlotsInput;
    private TextView parkingLotName;
    private Button submitButton;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        parkingLotName = findViewById(R.id.parking_lot_name);
        reservationDateInput = findViewById(R.id.reservation_date_input);
        reservationTimeInput = findViewById(R.id.reservation_time_input);
        reservedSlotsInput = findViewById(R.id.reserved_slots_input);
        submitButton = findViewById(R.id.submit_button);

        client = new OkHttpClient();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String lotName = parkingLotName.getText().toString();
                    String date = reservationDateInput.getText().toString();
                    String time = reservationTimeInput.getText().toString();
                    int slots = Integer.parseInt(reservedSlotsInput.getText().toString());

                    sendReservationData(lotName, date, time, slots);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReservationActivity.this, "입력을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendReservationData(String lotName, String date, String time, int slots) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("parkingLotName", lotName);
            jsonObject.put("date", date);
            jsonObject.put("time", time);
            jsonObject.put("slots", slots);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ReservationActivity.this, "예약 실패: 네트워크 오류", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ReservationActivity.this, "예약 성공", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ReservationActivity.this, "예약 실패: 서버 오류", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터 생성 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
}
