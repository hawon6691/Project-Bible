package com.projectbible.post.maven.postgresql.common.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    @Operation(summary = "Health check")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
            "status", "UP",
            "service", "post-java-springboot-maven-jpa-postgresql",
            "domain", "post"
        ));
    }
}
