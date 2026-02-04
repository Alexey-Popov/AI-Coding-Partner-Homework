package com.support.ticket.config;

import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Status;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, Category>() {
            @Override
            public Category convert(String source) {
                return Category.fromValue(source);
            }
        });
        registry.addConverter(new Converter<String, Priority>() {
            @Override
            public Priority convert(String source) {
                return Priority.fromValue(source);
            }
        });
        registry.addConverter(new Converter<String, Status>() {
            @Override
            public Status convert(String source) {
                return Status.fromValue(source);
            }
        });
    }
}
