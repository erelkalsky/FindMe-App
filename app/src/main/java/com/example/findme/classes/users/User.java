package com.example.findme.classes.users;

import java.io.Serializable;

public class User implements Serializable {
    private String phone;
    private String password;
    private String firstName;
    private String lastName;
    private Integer role;
    private Integer loginAttempts;
    private boolean firstLogin;

    public User() {

    }

    public User(String phone, String password, String firstName, String lastName, int role) {
        this.phone = phone;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.loginAttempts = 0;
        this.firstLogin = true;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getPassword() {
        return this.password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public int getRole() {
        return this.role;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }
}
