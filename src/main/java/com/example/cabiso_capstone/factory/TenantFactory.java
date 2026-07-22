package com.example.cabiso_capstone.factory;

import com.example.cabiso_capstone.model.Tenant;
import com.example.cabiso_capstone.model.User;


public class TenantFactory extends UserFactory {

    @Override
    public User createUser(
            int userId,
            String fullName,
            String username,
            String password
    ) {

        Tenant tenant = new Tenant();

        tenant.setUserId(userId);
        tenant.setFullName(fullName);
        tenant.setUsername(username);
        tenant.setPassword(password);

        return tenant;
    }
}