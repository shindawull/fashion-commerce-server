package com.ccommit.fashionserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * packageName    : com.ccommit.fashionserver.config
 * fileName       : RedisConfig
 * author         : juoiy
 * date           : 2023-10-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-08        juoiy       최초 생성
 */
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.expire.time}")
    private int expireTime;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // RedisConnectionFactory를 구현한 LettuceConnectionFactory를 사용하여 Redis 연결
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Jackson2는 Java8의 LocalDate의 타입을 정확히 알지못해서 적절히 직렬화를 해주지 않는다.
     * 그래서 역직렬화 시 에러가 발생한다.
     * 따라서, ObjectMapper를 Serializer에 전달하여 직렬화 및 역직렬화를 정상화한다.*
     */
    private Object objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /*
     * setKeySerializer, serValueSerializer를 사용한 이유
     *  RedisTemplate 사용 시에 Spring-Redis 간 데이터 직렬화, 역직렬화에 사용하는 방식이 Jdk 직렬화 방식이다.
     *  직렬화 : 자바 시스템 내부에서 사용되는 Object 또는 Data를 외부의 자바 시스템에서도 사용할 수 있도록 byte 형태로 데이터를 변환하는 기술이다.
     *  역직렬화 : byte로 변환된 Data를 원래대로 Object나 Data로 변환하는 기술이다.
     *
     * 직렬화/역직렬화 사용 이유
     * 복잡한 데이터 구조의 클래스의 객체라도 직렬화 기본 조건만 지키면 큰 작업 없이 바로 직렬화, 역직렬화가 가능하다.
     * 데이터 타입이 자동으로 맞춰지기 때문에 관련 부분을 크게 신경 쓰지 않아도 된다.
     * 출처:https://go-coding.tistory.com/101
     * */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory);

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())) // value Serialize 변경
                .entryTtl(Duration.ofSeconds(expireTime));

        builder.cacheDefaults(configuration);
        return builder.build();
    }

}
