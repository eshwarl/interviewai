package com.interviewai.service.impl;

import com.interviewai.exception.auth.InvalidCredentialsException;
import com.interviewai.exception.auth.UserAlreadyExistsException;
import com.interviewai.model.Role;
import com.interviewai.model.User;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;




@Service
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String name, String email, String password, String role) {

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with this email");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.valueOf(role.toUpperCase()))
                .build();

        return userRepository.save(user);
    }
   @Override
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return user;
    }


}