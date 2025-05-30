package com.hamburger.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.LoginReqDto;
import com.hamburger.user.dto.RegisterReqDto;
import com.hamburger.user.dto.AuthReqDto;
import com.hamburger.user.service.UserService;
import com.hamburger.user.service.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginReqDto req, HttpServletResponse response) {
        User user = userService.getUser(req.getUserName());
        if (user == null || !req.isPasswordValid(req.getPassword(), user.getHashedPassword())) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid username or password"));
        }
        String token = req.getToken(req.getUserName());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful");
        responseBody.put("token", token);
        responseBody.put("userInfo", Map.of(
            "userName", user.getUserName()
        ));

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/auth")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody AuthReqDto requestBody) {
        String token = requestBody.getToken();
        System.out.println("Received token for validation: " + token);
        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }

        String username = JwtUtil.extractUsername(token);
        System.out.println("Token validated for user: " + username);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Valid token");
        responseBody.put("username", username);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/{userName}")
    public ResponseEntity<User> getUser(@PathVariable("userName") String userName) {
        User user = userService.getUser(userName);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}