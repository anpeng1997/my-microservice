package cn.pengan.emailservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@SpringBootTest
class EmailServiceApplicationTests {

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${mail.from.addr}")
    private String fromAddr;

    @Test
    void sendEmailTest() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddr);
        simpleMailMessage.setTo("onlyange2017@gmail.com");
        simpleMailMessage.setSubject("title: test email");
        simpleMailMessage.setText("test email");
        sender.send(simpleMailMessage);
    }

    @Test
    void sendTemplateEmail() throws MessagingException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(fromAddr);
        helper.setTo("onlyange2017@gmail.com");
        helper.setSubject("title: test email");
        Context context = new Context();
        context.setVariable("code", "627138");
        String emailContent = templateEngine.process("verificationTemplate.html", context);
        helper.setText(emailContent, true);
        sender.send(mimeMessage);
    }
}
