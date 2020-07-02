package cn.pengan.webservice.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.pengan.common.constant.RabbitConstant;

import java.util.HashMap;

@RestController
@RequestMapping("/api/eamil")
public class EmailController {

    private final RabbitTemplate rabbitTemplate;

    public EmailController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/send")
    public String index(String mail, String code) {
        HashMap<String, String> map = new HashMap<>();
        map.put("toEmail", mail);
        map.put("code", code);
        rabbitTemplate.convertAndSend(RabbitConstant.BACKGROUND_TOPIC_EXCHANGE, "email.send", map);
        return "ok";
    }
}
