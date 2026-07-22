package com.example.cabiso_capstone.model;

public class Administrator extends User{
    public Administrator() {
        super();
    }

    public Administrator(int userId, String fullName, String username, String password) {
        super(userId, fullName, username, password);
    }

    @Override
    public String getRole() {
        return "Administrator";
    }
}
