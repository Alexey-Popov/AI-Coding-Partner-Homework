package com.supportiq.ticket.config;

import com.supportiq.ticket.enums.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EnumConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, TicketCategory.class, new StringToEnumConverter<>(TicketCategory::fromValue));
        registry.addConverter(String.class, TicketPriority.class, new StringToEnumConverter<>(TicketPriority::fromValue));
        registry.addConverter(String.class, TicketStatus.class, new StringToEnumConverter<>(TicketStatus::fromValue));
        registry.addConverter(String.class, Source.class, new StringToEnumConverter<>(Source::fromValue));
        registry.addConverter(String.class, DeviceType.class, new StringToEnumConverter<>(DeviceType::fromValue));
    }

    private static class StringToEnumConverter<T> implements Converter<String, T> {
        private final java.util.function.Function<String, T> converter;

        StringToEnumConverter(java.util.function.Function<String, T> converter) {
            this.converter = converter;
        }

        @Override
        public T convert(String source) {
            return converter.apply(source);
        }
    }
}
