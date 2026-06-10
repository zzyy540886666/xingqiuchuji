package com.xingqiu.server.common.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.util.FileSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${xingqiu.upload.local-dir:./uploads}")
    private String localDir;

    @Value("${xingqiu.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("INVALID_FILE", "文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return ApiResponse.fail("INVALID_FILE_TYPE", "仅支持图片文件");
        }

        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            return ApiResponse.fail("FILE_TOO_LARGE", "图片大小不能超过 10MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = ".png";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            ext = ".png";
        }
        if (!FileSignatureValidator.isAllowedImage(file, ALLOWED_IMAGE_TYPES)) {
            return ApiResponse.fail("INVALID_FILE_TYPE", "图片文件内容与类型不匹配");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(localDir, "avatars").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = normalizeBaseUrl(baseUrl) + "/avatars/" + fileName;
        log.info("avatar uploaded: {} -> {}", originalName, url);

        return ApiResponse.ok(Map.of("url", url, "fileName", fileName));
    }

    private String normalizeBaseUrl(String value) {
        String url = value == null || value.isBlank() ? "/uploads" : value;
        if (url.startsWith("http://localhost:8080/uploads")) {
            url = "/uploads";
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
