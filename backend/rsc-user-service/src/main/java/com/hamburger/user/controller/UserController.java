package com.hamburger.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hamburger.user.service.UserService;
import com.hamburger.user.dao.database.entity.User;
import com.hamburger.user.dto.RegisterReqDto;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody RegisterReqDto req) {
        // todo improve to automatic print the request path and request body
        System.out.println("Received request to register user: " + req.toString());
        userService.registerUser(req);
    }

    @GetMapping("/{userName}")
    public User getUser(@PathVariable String userName) {
        return userService.getUser(userName);
    }
}