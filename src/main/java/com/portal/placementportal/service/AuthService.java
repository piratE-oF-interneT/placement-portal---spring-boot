package com.portal.placementportal.service;

import com.portal.placementportal.dto.AuthDtos.LoginRequest;
import com.portal.placementportal.dto.AuthDtos.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
