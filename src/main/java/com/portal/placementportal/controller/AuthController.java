package com.portal.placementportal.controller;

import com.portal.placementportal.dto.AuthDtos.LoginRequest;
import com.portal.placementportal.dto.AuthDtos.LoginResponse;
import com.portal.placementportal.dto.AuthDtos.RegisterResponse;
import com.portal.placementportal.dto.AuthDtos.StudentRegisterRequest;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.service.AuthService;
import com.portal.placementportal.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StudentService studentService;

    @PostMapping("/student/register")
    public ResponseEntity<RegisterResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        Student s = studentService.register(request);
        return ResponseEntity.ok(new RegisterResponse(
                s.getStudentId(), s.getUsn(), s.getEmail(),
                "Registration successful. A random password has been emailed to " + s.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
