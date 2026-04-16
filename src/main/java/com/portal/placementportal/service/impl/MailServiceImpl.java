package com.portal.placementportal.service.impl;

import com.portal.placementportal.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stub mail sender. Logs the message at INFO level. Replace with JavaMailSender
 * once spring-boot-starter-mail is added.
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
    public void sendStudentCredentials(String toEmail, String usn, String password) {
        log.info("[MAIL-STUB] To: {} | Subject: Placement Portal credentials | USN: {} | Password: {}",
                toEmail, usn, password);
    }
}
