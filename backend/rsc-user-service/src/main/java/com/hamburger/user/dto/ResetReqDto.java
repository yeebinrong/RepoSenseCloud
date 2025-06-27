package com.hamburger.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "newPassword")
public class ResetReqDto {
    private String email;
    private String token;
    private String newPassword;

    public boolean isEmailValid() {
        if (email == null) {
            return false;
        }
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }

    public boolean isPasswordValid() {
        if (newPassword == null) {
            return false;
        }
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()])[a-zA-Z\\d~!@#$%^&*()]{8,}$";
        return newPassword.matches(regex);
    }
}