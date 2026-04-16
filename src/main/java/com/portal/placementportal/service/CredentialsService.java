package com.portal.placementportal.service;

import com.portal.placementportal.entity.Credentials;
import com.portal.placementportal.entity.Role;

public interface CredentialsService {

    /** Create a new Credentials row with the given raw password (hashed internally). */
    Credentials create(String loginId, String email, String rawPassword, Role role);

    Credentials getByLoginId(String loginId);

    Credentials getByEmail(String email);

    /** Validate raw password against stored hash. Throws InvalidCredentialsException on mismatch. */
    void verifyPassword(Credentials credentials, String rawPassword);

    /** Replace password for the given credentials (raw → hashed). */
    void updatePassword(Credentials credentials, String newRawPassword);
}
