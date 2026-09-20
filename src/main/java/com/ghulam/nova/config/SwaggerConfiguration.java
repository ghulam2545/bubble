package com.ghulam.nova.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Bubble",
                version = "1.0.0",
                description = "A postgres database viewer",
                contact = @Contact(
                        name = "example@gmail.com"
                )
        )
)
@Configuration
public class SwaggerConfiguration {
}