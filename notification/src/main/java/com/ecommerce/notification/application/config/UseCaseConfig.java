package com.ecommerce.notification.application.config;

import com.ecommerce.notification.domain.model.gateway.SmsGateway;
import com.ecommerce.notification.domain.usecase.NotificacionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public NotificacionUseCase notificacionUseCase(SmsGateway smsGateway) {
        return new NotificacionUseCase(smsGateway);
    }
}
