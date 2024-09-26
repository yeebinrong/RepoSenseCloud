package com.hamburger.user.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hamburger.user.service.util.PasswordUtil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "password")
public class RegisterReqDto {

    @Getter
    @NotNull
    @Size(min = 5, max = 15)
    // Username must be non-null and between 5 and 15 characters long
    private String userName;

    @Getter
    @NotNull
    @Email
    // Email must be a well formatted non-null email address
    private String email;

    @Setter
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Include password during deserialization
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*_=+-]).{8,20}$")
    // Password must contain at least one digit [0-9]
    // Password must contain at least one lowercase Latin character [a-z]
    // Password must contain at least one uppercase Latin character [A-Z]
    // Password must contain at least one special character like ! @ # $ % ^ & * _ = + -
    // Password must be eight to twenty characters long
    private String password;

    public String getHashedPassword() {
        System.out.println("Password hashed!");
        return PasswordUtil.hashPassword(password);
    }
}