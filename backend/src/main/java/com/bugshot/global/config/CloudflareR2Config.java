package com.bugshot.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Cloudflare R2 Configuration
 * R2는 S3 호환 API를 제공하므로 S3Client를 그대로 사용 가능
 * 비용: 무료 (10GB 저장소 + 무제한 Egress)
 */
@Configuration
public class CloudflareR2Config {

    @Value("${app.cloudflare.r2.account-id}")
    private String accountId;

    @Value("${app.cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${app.cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${app.cloudflare.r2.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        // Cloudflare R2 endpoint: https://<account-id>.r2.cloudflarestorage.com
        String r2Endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);

        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            // 로컬 개발 환경: R2 없이 실행 가능 (에러 스킵)
            return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(r2Endpoint))
                .build();
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
            .region(Region.of("auto")) // R2는 "auto" region 사용
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .endpointOverride(URI.create(r2Endpoint))
            .build();
    }
}
