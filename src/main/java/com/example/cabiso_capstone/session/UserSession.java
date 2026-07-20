package com.example.cabiso_capstone.session;

import java.io.Serializable;

public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int userId;
    private final Integer tenantId;
    private final String username;
    private final String fullName;
    private final String role;
    private final String accountStatus;

    public UserSession(int userId, Integer tenantId, String username, String fullName, String role, String accountStatus) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public int getUserId() {
        return userId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isTenant() {
        return "TENANT".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(accountStatus);
    }

    @Override
    public String toString() {
        return "UserSession{" + "userId=" + userId + ", tenantId=" + tenantId + ", username='" + username + '\'' + ", fullName='" + fullName + '\'' + ", role='" + role + '\'' + ", accountStatus='" + accountStatus + '\'' + '}';
    }
}