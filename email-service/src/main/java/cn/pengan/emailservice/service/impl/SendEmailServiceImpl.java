package cn.pengan.emailservice.service.impl;

import cn.pengan.emailservice.service.ISendEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class SendEmailServiceImpl implements ISendEmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${mail.from.addr}")
    private String fromAddr;

    public SendEmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String code) {
        if (StringUtils.isEmpty(toEmail) || StringUtils.isEmpty(code)) {
            return;
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromAddr);
            helper.setTo(toEmail);
            helper.setSubject("verification Email");
            Context context = new Context();
            context.setVariable("code", code);
            String emailContent = templateEngine.process("verificationTemplate.html", context);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
