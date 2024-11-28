package com.example.app1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private ArrayList<Reservation> reservationList;

    public ReservationAdapter(ArrayList<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.parkingLotNameTextView.setText(reservation.getParkingLotName());
        holder.reservationTimeTextView.setText("예약 시간: " + reservation.getReservationTime());
        holder.statusTextView.setText(reservation.getStatus()); // 예약완료 출력
    }


    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reservationTimeTextView, statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parking_lot_name);
            reservationTimeTextView = itemView.findViewById(R.id.reservation_time);
            statusTextView = itemView.findViewById(R.id.reservation_status);
        }
    }
}
