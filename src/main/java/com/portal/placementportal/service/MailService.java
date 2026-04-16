package com.portal.placementportal.service;

public interface MailService {
    void sendStudentCredentials(String toEmail, String usn, String password);
}
