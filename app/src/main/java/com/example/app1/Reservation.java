package com.example.app1;

public class Reservation {
    private String parkingLotName;
    private String reservationTime;

    public Reservation(String parkingLotName, String reservationTime) {
        this.parkingLotName = parkingLotName;
        this.reservationTime = reservationTime;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public String getReservationTime() {
        return reservationTime;
    }
}
