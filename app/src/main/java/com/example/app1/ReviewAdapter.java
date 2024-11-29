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
    private OnDeleteClickListener deleteClickListener; // For delete functionality

    // Constructor for delete functionality
    public ReviewAdapter(ArrayList<Review> reviewList, OnDeleteClickListener deleteClickListener) {
        this.reviewList = reviewList;
        this.deleteClickListener = deleteClickListener;
    }

    // Constructor for no delete functionality
    public ReviewAdapter(ArrayList<Review> reviewList) {
        this.reviewList = reviewList;
        this.deleteClickListener = null; // No delete functionality
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

        // Date formatting
        String formattedDate = formatDateString(review.getDate());
        holder.dateTextView.setText(formattedDate);

        // Set delete button functionality if available
        if (deleteClickListener != null) {
            holder.deleteButton.setVisibility(View.VISIBLE); // Show delete button
            holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(review.getId()));
        } else {
            holder.deleteButton.setVisibility(View.GONE); // Hide delete button
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reviewTextView, dateTextView;
        RatingBar reviewRatingBar;
        TextView deleteButton; // For delete functionality

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parkingLotNameTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            deleteButton = itemView.findViewById(R.id.deleteButton); // Add delete button
        }
    }

    // Interface for delete button click handling
    public interface OnDeleteClickListener {
        void onDeleteClick(int reviewId);
    }

    // Format date helper method
    private String formatDateString(String dateString) {
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat userFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault());
            Date date = serverFormat.parse(dateString);
            return userFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "날짜 변환 오류";
        }
    }
}
