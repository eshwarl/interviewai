package com.interviewai.controller;

import com.interviewai.dto.AuthResponse;
import com.interviewai.model.User;
import com.interviewai.security.JwtUtil;
import com.interviewai.service.UserService;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public User register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role
    ) {
        return userService.registerUser(name, email, password, role);
    }
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        User user = userService.loginUser(email, password);

        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }


}
