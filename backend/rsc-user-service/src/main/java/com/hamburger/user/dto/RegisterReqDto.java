package com.hamburger.user.dto;

import com.hamburger.user.service.util.PasswordUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "password")
public class RegisterReqDto {

    @Getter
    private String userName;

    @Getter
    private String email;

    @Setter
    private String password;

    // Email must be a well formatted non-null email address
    public boolean isEmailValid() {
        if (email == null) {
            return false;
        }
        String regex = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return email.matches(regex);
    }

    public boolean isPasswordValid() {
        if (password == null) {
            return false;
        }
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
        return password.matches(regex);
    }

    public String getHashedPassword() {
        System.out.println("Password hashed!");
        return PasswordUtil.hashPassword(password);
    }
}