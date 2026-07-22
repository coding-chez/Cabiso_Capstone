package com.example.cabiso_capstone.factory;

import com.example.cabiso_capstone.model.User;

public abstract class UserFactory {

    public abstract User createUser(
            int userId,
            String fullName,
            String username,
            String password
    );
}