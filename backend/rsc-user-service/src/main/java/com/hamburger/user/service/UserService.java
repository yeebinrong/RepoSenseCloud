package com.hamburger.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamburger.user.dao.UserDao;
import com.hamburger.user.dao.ResetPasswordDao;
import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dao.entity.ResetPassword;
import com.hamburger.user.dto.RegisterReqDto;
import com.hamburger.user.service.util.JwtUtil;
import com.hamburger.user.service.util.PasswordUtil;

import java.util.Date;

@Service
public class UserService {
    private final UserDao userDao;
    private final ResetPasswordDao resetPasswordDao;

    @Autowired
    public UserService(UserDao userDao, ResetPasswordDao resetPasswordDao) {
        this.userDao = userDao;
        this.resetPasswordDao = resetPasswordDao;
    }

    public void registerUser(RegisterReqDto req) {
        // Check if userName already exists
        User existingUser = userDao.findByUserName(req.getUserName());
        if (existingUser != null) {
            System.out.println("UserName already exists");
            return;
        }

        User user = User.builder()
                .userName(req.getUserName())
                .email(req.getEmail())
                .hashedPassword(req.getHashedPassword())
                .build();
        userDao.save(user);
        System.out.println("User registered!");
    }

    public User getUser(String userName) {
        return userDao.findByUserName(userName);
    }

    public void requestResetPassword(String email) {
        String token = JwtUtil.generateToken(email).toString();
        long expiry = System.currentTimeMillis() + 15 * 60 * 1000; // 15 minutes expiry
        resetPasswordDao.save(new ResetPassword(email, token, expiry));
        String resetLink = System.getenv("FRONTEND_ORIGIN") + "/reset?email=" + email + "&token=" + token;
        sesService.sendResetPasswordEmail(email, resetLink);
        System.out.println("Reset password email sent to " + email);
    }
}