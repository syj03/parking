package com.example.app1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

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
        holder.ratingTextView.setText("평점: " + review.getRating());
        holder.dateTextView.setText(review.getDate());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView parkingLotNameTextView, reviewTextView, ratingTextView, dateTextView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingLotNameTextView = itemView.findViewById(R.id.parkingLotNameTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
