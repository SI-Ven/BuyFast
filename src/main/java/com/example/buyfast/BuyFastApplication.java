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

@SpringBootApplication
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "ResellKH API", version = "1.0"),
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
        "com.example.buyfast.modules.auth.repository" // <--- THIS WAS MISSING
})
public class BuyFastApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyFastApplication.class, args);
    }

}