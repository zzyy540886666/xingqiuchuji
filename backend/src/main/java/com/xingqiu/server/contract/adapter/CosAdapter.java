package com.xingqiu.server.contract.adapter;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.http.HttpMethodName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

/**
 * 腾讯云 COS 对象存储适配器（真实实现）
 */
@Service
public class CosAdapter {

    private static final Logger log = LoggerFactory.getLogger(CosAdapter.class);

    private final COSClient cosClient;
    private final String bucket;
    private final String region;
    private final String cdnBase;

    public CosAdapter(COSClient cosClient,
                      @Value("${xingqiu.cos.bucket}") String bucket,
                      @Value("${xingqiu.cos.region:ap-guangzhou}") String region,
                      @Value("${xingqiu.cos.cdn-base:}") String cdnBase) {
        this.cosClient = cosClient;
        this.bucket = bucket;
        this.region = region;
        this.cdnBase = cdnBase;
        log.info("COS Adapter initialized — bucket: {}, region: {}", bucket, region);
    }

    /**
     * 上传文件到 COS
     */
    public String upload(String key, byte[] bytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        PutObjectRequest request = new PutObjectRequest(
                bucket, key,
                new ByteArrayInputStream(bytes), metadata
        );
        PutObjectResult result = cosClient.putObject(request);
        log.info("COS upload success — key: {}, size: {} bytes, etag: {}", key, bytes.length, result.getETag());

        if (cdnBase != null && !cdnBase.isBlank()) {
            return cdnBase.replaceAll("/$", "") + "/" + key;
        }
        return String.format("https://%s.cos.%s.myqcloud.com/%s", bucket, region, key);
    }

    /**
     * 生成预签名下载 URL
     */
    public String generatePresignedUrl(String key, long durationSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + durationSeconds * 1000);
        URL url = cosClient.generatePresignedUrl(bucket, key, expiration, HttpMethodName.GET);
        log.info("COS presigned URL generated — key: {}, expires in: {}s", key, durationSeconds);
        return url.toString();
    }

    @Configuration
    static class CosConfig {

        @Bean
        public COSClient cosClient(
                @Value("${xingqiu.cos.secret-id}") String secretId,
                @Value("${xingqiu.cos.secret-key}") String secretKey,
                @Value("${xingqiu.cos.region:ap-guangzhou}") String region) {
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig config = new ClientConfig(new Region(region));
            config.setConnectionTimeout(30000);
            config.setSocketTimeout(30000);
            return new COSClient(cred, config);
        }
    }
}
