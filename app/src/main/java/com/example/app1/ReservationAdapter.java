package com.example.app1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private ArrayList<Reservation> reservationList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int reservationId, int position);
    }

    public ReservationAdapter(ArrayList<Reservation> reservationList, OnDeleteClickListener deleteClickListener) {
        this.reservationList = reservationList;
        this.deleteClickListener = deleteClickListener;
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
        holder.statusTextView.setText(reservation.getStatus());

        // 삭제 버튼 클릭 이벤트 연결
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(reservation.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reservationTimeTextView, statusTextView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parking_lot_name);
            reservationTimeTextView = itemView.findViewById(R.id.reservation_time);
            statusTextView = itemView.findViewById(R.id.reservation_status);
            deleteButton = itemView.findViewById(R.id.delete_reservation_button);
        }
    }
}
