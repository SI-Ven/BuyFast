package com.example.buyfast;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "BuyFast API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
// --- FIXED: Added "com.example.buyfast.modules.auth.repository" to the scan list ---
@MapperScan({
        "com.example.buyfast.modules.category.repository",
        "com.example.buyfast.modules.user.repository",
        "com.example.buyfast.modules.otp.repository",
        "com.example.buyfast.modules.verify.repository",
        "com.example.buyfast.modules.company.repository",
        "com.example.buyfast.modules.product.repository",
        "com.example.buyfast.modules.auth.repository",
        "com.example.buyfast.modules.favorite.repository",
        "com.example.buyfast.modules.address.repository",
        "com.example.buyfast.modules.order.repository",
        "com.example.buyfast.modules.review.repository",
        "com.example.buyfast.modules.dispute.repository"
})
@EnableAsync // Add this line for quickly response for user while process in background
public class BuyFastApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyFastApplication.class, args);
    }

}