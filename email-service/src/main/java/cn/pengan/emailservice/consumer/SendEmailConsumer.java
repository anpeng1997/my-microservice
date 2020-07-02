package cn.pengan.emailservice.consumer;

import cn.pengan.emailservice.service.ISendEmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.pengan.common.constant.RabbitConstant;

import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.Map;

@Service
public class SendEmailConsumer{

    @Autowired
    private ISendEmailService sendEmailService;

    /**
     * 监听BACKGROUND_SEND_EMAIL_QUEUE该队列
     * @param map 会自动帮我们转换成相对应的类型（需要和生产者的发送对象类型一致，对象要实现Serializable接口）
     */
    @RabbitListener(queues = {RabbitConstant.BACKGROUND_SEND_EMAIL_QUEUE})
    public void getQueueMessage(Map map) {
        String toEmail = (String) map.get("toEmail");
        String code = (String) map.get("code");
        try {
            sendEmailService.sendVerificationEmail(toEmail, code);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
