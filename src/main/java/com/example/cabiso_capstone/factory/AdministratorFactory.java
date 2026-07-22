package com.example.cabiso_capstone.factory;

import com.example.cabiso_capstone.model.Administrator;
import com.example.cabiso_capstone.model.User;

public class AdministratorFactory extends UserFactory {

    @Override
    public User createUser(
            int userId,
            String fullName,
            String username,
            String password
    ) {

        return new Administrator(
                userId,
                fullName,
                username,
                password
        );
    }
}