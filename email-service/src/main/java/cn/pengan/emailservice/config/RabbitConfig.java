package cn.pengan.emailservice.config;

import cn.pengan.common.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置rabbit的队列与exchange绑定
 */
@Configuration
public class RabbitConfig {

    /**
     * 声明该服务消费的队列
     * @return
     */
    @Bean
    public Queue setQueue() {
        return new Queue(RabbitConstant.BACKGROUND_SEND_EMAIL_QUEUE);
    }


    /**
     * 声明交换机
     * @return
     */
    @Bean
    public TopicExchange setTopicExchange() {
        return new TopicExchange(RabbitConstant.BACKGROUND_TOPIC_EXCHANGE);
    }

    /**
     * 声明绑定
     * @return
     */
    @Bean
    public Binding setBinding(Queue setQueue,TopicExchange setTopicExchange) {
                                                                    //Topic类型，可以使用 * 或 # 代替一个或多个字符
        return BindingBuilder.bind(setQueue).to(setTopicExchange).with("email.*");
    }
}
