package com.example.buyfast.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. Define the security scheme (what it is)
        final String securitySchemeName = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP) // Type is HTTP
                .scheme("bearer")               // Scheme is "bearer"
                .bearerFormat("JWT")            // Format is "JWT"
                .description("Enter JWT Bearer token");

        // 2. Define the security requirement (how to use it)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        // 3. Create the OpenAPI bean
        return new OpenAPI()
                .info(new Info().title("BuyFast API").version("1.0"))
                .addSecurityItem(securityRequirement) // This makes ALL endpoints require the token
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme)); // This adds the "Authorize" button
    }
}