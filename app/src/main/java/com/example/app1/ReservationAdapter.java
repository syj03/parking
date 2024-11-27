package com.example.app1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservationList;
    private Context context;

    public ReservationAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.parkingLotNameTextView.setText(reservation.getParkingLotName());
        holder.reservationTimeTextView.setText(reservation.getReservationTime());

        // 예약 취소 버튼 클릭 이벤트 설정 (리스트에서 항목 제거)
        holder.cancelReservationButton.setOnClickListener(v -> {
            // 예제 데이터에서 항목을 삭제
            reservationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, reservationList.size());

            // 취소 알림 메시지
            Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reservationTimeTextView;
        Button cancelReservationButton;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parking_lot_name);
            reservationTimeTextView = itemView.findViewById(R.id.reservation_time);
            cancelReservationButton = itemView.findViewById(R.id.cancel_reservation_button);
        }
    }
}
