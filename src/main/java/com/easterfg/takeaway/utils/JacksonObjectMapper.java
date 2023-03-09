package com.easterfg.takeaway.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author EasterFG on 2022/9/30
 * <p>
 * 解析器
 */
@Component
public class JacksonObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = -6224919434023348235L;

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JacksonObjectMapper() {

        // 忽略空值
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 收到未知属性时不报异常
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 反序列化时，属性不存在的兼容处理
        // this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule simpleModule = new SimpleModule()
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(Long.TYPE, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DEFAULT_DATE_TIME_FORMAT))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DEFAULT_DATE_FORMAT))
                // 反序列化
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DEFAULT_DATE_TIME_FORMAT))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DEFAULT_DATE_FORMAT));
        this.registerModule(simpleModule);
    }
}
