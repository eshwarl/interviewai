package com.interviewai.service;

import com.interviewai.model.User;

public interface UserService {

    User registerUser(String name, String email, String password, String role);
    User loginUser(String email, String password);
}
