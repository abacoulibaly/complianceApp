package com.threasury.compliance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "compliance")
public record ComplianceProperties(
        Processing processing,
        Batch batch
) {
    public record Processing(long timeoutMs) {}
    public record Batch(int maxConcurrency) {}
}
