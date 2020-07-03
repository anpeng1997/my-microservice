package cn.pengan.webservice.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootTest
public class RedisTest {

    //使用该对象存储对象时，redis默认使用的jdk的序列化方式,会导致key值乱码,所以要配置Redis的序列化方式
    @Autowired
    private RedisTemplate redisTemplate;

    //使用该对象则不会出现乱码
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test() {
        redisTemplate.opsForValue().set("hahah:", "123123");
        System.out.println(redisTemplate.opsForValue().get("hahah"));
//        stringRedisTemplate.opsForValue().set("qweqweqweqwewqeqw:", "123123");
//        System.out.println("ok");
//        Object transactionTest = redisTemplate.opsForValue().get("transactionTest");
//        System.out.println(transactionTest.toString());
    }

    @Test
    void redisTransactionTest() {
        //配置Redis的序列化方式
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                redisOperations.opsForValue().set("transactionTest", "1");
                redisOperations.opsForValue().set("transactionTest2", "2");
                redisOperations.exec();
                return null;
            }
        });
    }

    /**
     * 流水线批量操作数据（可以减少因为频繁连接网络操作的时间）
     * 实际业务场景：可以进行热门商品的预热缓存操作，根据2/8定律，将前20%的热门商品提前缓存在redis中
     */
    @Test
    void redisPipelinedTest() {
        long start = System.currentTimeMillis();
        stringRedisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                for (int i = 1; i <= 100000; i++) {
                    redisOperations.opsForValue().set("key"+i,i+"");
                }
                return null;
            }
        });
        for (int i = 1; i <= 100000; i++) {
            stringRedisTemplate.opsForValue().set("key" + i, i + "");
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);//2622 毫秒
                                        //11906 毫秒
    }

}
