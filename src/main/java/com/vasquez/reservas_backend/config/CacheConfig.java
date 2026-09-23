package com.vasquez.reservas_backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String SERVICIOS_ACTIVOS = "serviciosActivos";

    public static final String SERVICIO_POR_ID = "servicioPorId";

    public static final String DISPONIBILIDADES_POR_SERVICIO =
            "disponibilidadesPorServicio";

    public static final String DISPONIBILIDADES_POR_SERVICIO_Y_DIA =
            "disponibilidadesPorServicioYDia";

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {
        GenericJacksonJsonRedisSerializer jsonSerializer =
                GenericJacksonJsonRedisSerializer
                        .builder()
                        .build();

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(jsonSerializer)
                        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(
                        SERVICIOS_ACTIVOS,
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                        SERVICIO_POR_ID,
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                        DISPONIBILIDADES_POR_SERVICIO,
                        defaultConfig.entryTtl(Duration.ofSeconds(30))
                )
                .withCacheConfiguration(
                        DISPONIBILIDADES_POR_SERVICIO_Y_DIA,
                        defaultConfig.entryTtl(Duration.ofSeconds(30))
                )
                .build();
    }
}