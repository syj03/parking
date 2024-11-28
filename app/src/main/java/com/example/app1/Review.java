package com.example.app1;

public class Review {
    private int id;
    private String parkingLotName;
    private String reviewText;
    private int rating;
    private String date;

    public Review(int id, String parkingLotName, String reviewText, int rating, String date) {
        this.id = id;
        this.parkingLotName = parkingLotName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }
}