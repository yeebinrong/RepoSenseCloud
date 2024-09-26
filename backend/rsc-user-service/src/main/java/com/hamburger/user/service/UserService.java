package com.hamburger.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamburger.user.dao.UserDao;
import com.hamburger.user.dao.database.entity.User;
import com.hamburger.user.dto.RegisterReqDto;

@Service
public class UserService {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
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
}