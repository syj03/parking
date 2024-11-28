package com.example.app1;

public class Reservation {
    private int id;
    private String parkingLotName;
    private String reservationTime;
    private String status;

    public Reservation(int id, String parkingLotName, String reservationTime, String status) {
        this.id = id;
        this.parkingLotName = parkingLotName;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public String getStatus() {
        return status;
    }
}
