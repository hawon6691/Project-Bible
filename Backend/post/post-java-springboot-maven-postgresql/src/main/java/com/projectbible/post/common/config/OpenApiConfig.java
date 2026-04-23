package com.projectbible.post.common.config;
import io.swagger.v3.oas.models.OpenAPI; import io.swagger.v3.oas.models.info.Info; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class OpenApiConfig { @Bean public OpenAPI openAPI() { return new OpenAPI().info(new Info().title("post-java-springboot-maven-postgresql API").version("v1")); } }
