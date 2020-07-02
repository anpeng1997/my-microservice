package cn.pengan.emailservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cn.pengan.common.constant.RabbitConstant;

import javax.mail.MessagingException;
import java.util.HashMap;

@SpringBootTest
public class SendEmailTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ISendEmailService sendEmailService;

    @Test
    void sendTest() throws MessagingException {
        sendEmailService.sendVerificationEmail("peng.an@yunstorm.com","888888");
    }

    @Test
    void sendQueueMessage() {
        HashMap<String, String> map = new HashMap<>();
        map.put("toEmail", "peng.an@yunstorm.com");
        map.put("code", "123123");
        //这个routingKey只要符合RabbitConfig类中配置绑定的key时，队列就能接收到消息
        //配置绑定是为：email.* （*代表一个单词）
        //这里我们使用 email.send 就可以发送给绑定了该交换器，并符合绑定key的队列
        rabbitTemplate.convertAndSend(RabbitConstant.BACKGROUND_TOPIC_EXCHANGE,"email.send",map);
    }
}
