package com.portal.placementportal.service.impl;

import com.portal.placementportal.entity.Credentials;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.InvalidCredentialsException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.CredentialsRepository;
import com.portal.placementportal.service.CredentialsService;
import com.portal.placementportal.utilities.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CredentialsServiceImpl implements CredentialsService {

    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoderUtil passwordEncoder;

    @Override
    @Transactional
    public Credentials create(String loginId, String email, String rawPassword, Role role) {
        if (credentialsRepository.existsByLoginIdIgnoreCase(loginId)) {
            throw new DuplicateResourceException("Login id already registered: " + loginId);
        }
        if (credentialsRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }
        Credentials c = Credentials.builder()
                .loginId(loginId)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build();
        return credentialsRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Credentials getByLoginId(String loginId) {
        return credentialsRepository.findByLoginIdIgnoreCase(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("Credentials", loginId));
    }

    @Override
    @Transactional(readOnly = true)
    public Credentials getByEmail(String email) {
        return credentialsRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Credentials", email));
    }

    @Override
    public void verifyPassword(Credentials credentials, String rawPassword) {
        if (!credentials.isActive()) {
            throw new InvalidCredentialsException("Account is not active");
        }
        if (!passwordEncoder.matches(rawPassword, credentials.getPassword())) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional
    public void updatePassword(Credentials credentials, String newRawPassword) {
        credentials.setPassword(passwordEncoder.encode(newRawPassword));
        credentialsRepository.save(credentials);
    }
}
