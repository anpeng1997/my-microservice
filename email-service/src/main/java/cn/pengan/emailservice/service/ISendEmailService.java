package cn.pengan.emailservice.service;

import javax.mail.MessagingException;

public interface ISendEmailService {
    void sendVerificationEmail(String toEmail,String code) throws MessagingException;
}
