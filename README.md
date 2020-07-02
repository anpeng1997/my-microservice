# My Microservice

## RabbitMQ Email Service

1. 在Email Service中配置RabbitMQ配置类,用来注册队列、交换器、及它们之间的绑定

   ```java
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
   
   ```

   

2. 添加一个监听器，用来消费队列中的消息

   ```java
   @Service
   public class SendEmailConsumer {
   
       @Autowired
       private ISendEmailService sendEmailService;
   
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
   ```

   

3. 测试给队列中发送消息

   ```java
    @Test
       void sendQueueMessage() {
           HashMap<String, String> map = new HashMap<>();
           map.put("toEmail", "123456");
           map.put("code", "123123");
           //这个routingKey只要符合RabbitConfig类中配置绑定的key时，队列就能接收到消息
           //配置绑定是为：email.* （*代表一个单词）
           //这里我们使用 email.send 就可以发送给绑定了该交换器，并符合绑定key的队列
           rabbitTemplate.convertAndSend(RabbitConstant.BACKGROUND_TOPIC_EXCHANGE,"email.send",map);
       }
   ```

   

4. 定义队列和交换器名称常量，防止写错

   ```java
   public class RabbitConstant {
       public static final String BACKGROUND_SEND_EMAIL_QUEUE="background.send.email.queue";
   
       public static final String BACKGROUND_TOPIC_EXCHANGE="background.topic.exchange";
   }
   ```