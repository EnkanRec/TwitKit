package com.enkanrec.twitkitFridge;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class TwitkitFridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitkitFridgeApplication.class, args);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
