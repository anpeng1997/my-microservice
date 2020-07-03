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



## redis

### redis 事务的使用

```java
redisTemplate.execute(new SessionCallback() {
    @Override
    public Object execute(RedisOperations redisOperations) throws DataAccessException {
        redisOperations.multi();
        redisOperations.opsForValue().set("transactionTest","1");
        redisOperations.opsForValue().set("transactionTest2","2");
        redisOperations.exec();
        return null;
    }
});
```

### 关于存入的键值对乱码问题

```java
//使用该对象存储对象时，redis默认使用的jdk的序列化方式,会导致key值乱码,所以要配置Redis的序列化方式
@Autowired
private RedisTemplate redisTemplate;

//使用该对象则不会出现乱码
@Autowired
private StringRedisTemplate stringRedisTemplate;


 //使用RedisTemplate时需要配置Redis的序列化方式
 //https://blog.csdn.net/WYA1993/article/details/86591716
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
```

### redis 乐观锁

> 原理与SQL的乐观锁机制（CAS:compare AND set）类型，都是采用版本号递增的方式来维护

```cmd
 #设置一个key为版本号
 set version 1
 
 #监听它
 watch vesion
 
 #开启事务
 multi
 
 #修改操作
 set transactionTest 123
 
 #提交事务
 exec
 
 ## 假如在执行事务后，发现version被修改了，则会回滚
```

### redis流水线（需要插入多条记录）

```java
/**
     * 流水线批量操作数据（可以减少因为频繁连接网络操作的时间）
     * 实际业务场景：可以进行热门商品的预热缓存操作，根据2/8定律，将前20%的热门商品提前缓存在redis中
     */
@Test
void redisPipelinedTest() {
    //流水线方式 （耗时2622 毫秒）
    stringRedisTemplate.executePipelined(new SessionCallback() {
        @Override
        public Object execute(RedisOperations redisOperations) throws DataAccessException {
            for (int i = 1; i <= 100000; i++) {
                redisOperations.opsForValue().set("key"+i,i+"");
            }
            return null;
        }
    });

    //普通方式 （耗时11906 毫秒）
    for (int i = 1; i <= 100000; i++) {
        stringRedisTemplate.opsForValue().set("key" + i, i + "");
    }
}
//从耗时时间可以看出，使用流水线的方式，将大大提高我们的插入时间（这次测试还是在本地测试的，在实际的生产环境中，差距应该更大）
```

### redis主从复制

1. 从节点保存主节点信息
2. 从节点建立socker连接
3. 从节点发送ping命令，等待主节点返回pong信息
4. 权限验证
5. 主从连接正常后，开始同步数据集，首次建立复制，是全量复制的方式
6. 持续的主从复制：后续主节点发生数据变更时，会继续给从节点发送命令。此处采用增量复制（主从节点都会记录一个偏移量的值，用来确定从那个地方开始复制）



* 当一个redis被设置成slave后，就不能进行写操作（因为这样会造成数据混乱）



#### 哨兵机制（一主多从，则使用哨兵机制）

> 哨兵解决了故障自动化处理的问题，当主机发生故障时，哨兵会接触该主机和从机之间的关系，并选择一台（偏移量值最大的）从机将它设置为新的主机



#### 分布式集群架构（多个（小）主从形成的集群机构）



> 不需要哨兵，集群之间的各个节点互相监督
>
> 当使用集群set，get值时，要怎么确认数据在那台机器上？

1. 在创建集群的时候，确定管辖范围（插槽）0-16383（16384）（一台机器分配一些插槽）
2. set key1 value1 根据key值，进行CRC16算法，等到一个数值，用该数值%16384，就得到一个0-16383之间的之间的数

3. get操作时，也是同set同理