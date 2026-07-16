package com.example.cabiso_capstone.model;

public abstract class User {
        private int userId;
        private String fullName;
        private String username;
        private String password;

        public User() {
        }

        public User(int userId, String fullName, String username, String password) {
            this.userId = userId;
            this.fullName = fullName;
            this.username = username;
            this.password = password;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public abstract String getRole();

        @Override
        public String toString() {
            return fullName;
        }
}
