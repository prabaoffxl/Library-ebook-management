package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
