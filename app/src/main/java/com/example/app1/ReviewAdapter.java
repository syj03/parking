package com.example.app1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<Review> reviewList;

    public ReviewAdapter(ArrayList<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.parkingLotNameTextView.setText(review.getParkingLotName());
        holder.reviewTextView.setText(review.getReviewText());
        holder.reviewRatingBar.setRating(review.getRating());

        // 날짜 포맷 변환
        String formattedDate = formatDateString(review.getDate());
        holder.dateTextView.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reviewTextView, dateTextView;
        RatingBar reviewRatingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parkingLotNameTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
        }
    }

    // 날짜 포맷 변환 메서드
    private String formatDateString(String dateString) {
        try {
            // 서버에서 받은 날짜 형식
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

            // 사용자 친화적인 날짜 형식
            SimpleDateFormat userFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault());

            // 변환
            Date date = serverFormat.parse(dateString);
            return userFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return "날짜 변환 오류";
        }
    }
}
