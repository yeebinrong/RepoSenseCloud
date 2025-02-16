package com.hamburger.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hamburger.user.service.UserService;

import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.RegisterReqDto;
import com.hamburger.user.dto.LoginReqDto;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterReqDto req) {
        if (!req.isEmailValid()) {
            return ResponseEntity.status(400).body("Invalid email format");
        }
        if (!req.isPasswordValid()) {
            return ResponseEntity.status(400).body("Invalid password format");
        }

        // Check if user already exists
        User existingUser = userService.getUser(req.getUserName());
        if (existingUser != null) {
            return ResponseEntity.status(409).body("User already exists");
        }

        System.out.println("Received request to register user: " + req.toString());
        userService.registerUser(req);
        return ResponseEntity.ok("User registered!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginReqDto req, HttpServletResponse response) {
        User user = userService.getUser(req.getUserName());
        if (user == null || !req.isPasswordValid(req.getPassword(), user.getHashedPassword())) {
            return ResponseEntity.status(400).body("Invalid username or password");
        }
        String token = req.getToken(req.getUserName());
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/{userName}")
    public ResponseEntity<User> getUser(@PathVariable String userName) {
        User user = userService.getUser(userName);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}