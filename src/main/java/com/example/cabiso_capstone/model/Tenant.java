package com.example.cabiso_capstone.model;

public class Tenant extends User{
    private String contactNumber;
    private Room assignedRoom;
    private double balance;
    private String status;

    public Tenant() {
        super();
    }

    public Tenant(int userId, String fullName, String username, String password, String contactNumber, Room assignedRoom, double balance, String status) {
        super(userId, fullName, username, password);

        this.contactNumber = contactNumber;
        this.assignedRoom = assignedRoom;
        this.balance = balance;
        this.status = status;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Room getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(Room assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedRoomNumber() {
        if (assignedRoom == null) {
            return "Not Assigned";
        }

        return assignedRoom.getRoomNumber();
    }

    public void addBalance(double amount) {
        balance += amount;
    }

    public void deductBalance(double amount) {
        balance -= amount;

        if (balance < 0) {
            balance = 0;
        }
    }

    @Override
    public String getRole() {
        return "Tenant";
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
