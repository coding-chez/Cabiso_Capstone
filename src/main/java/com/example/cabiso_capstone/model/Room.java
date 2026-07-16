package com.example.cabiso_capstone.model;

public class Room {
    private int roomId;
    private String roomNumber;
    private int capacity;
    private int occupants;
    private double monthlyRate;
    private String status;

    public Room() {
    }

    public Room(int roomId, String roomNumber, int capacity, int occupants, double monthlyRate, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupants = occupants;
        this.monthlyRate = monthlyRate;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupants() {
        return occupants;
    }

    public void setOccupants(int occupants) {
        this.occupants = occupants;
    }

    public double getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(double monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAvailableSlots() {
        return capacity - occupants;
    }

    public boolean isAvailable() {
        return occupants < capacity
                && !"Unavailable".equalsIgnoreCase(status);
    }

    public void updateAvailabilityStatus() {
        if (occupants >= capacity) {
            status = "Occupied";
        } else {
            status = "Available";
        }
    }

    @Override
    public String toString() {
        return roomNumber;
    }
}
