package com.xingqiu.server.admin.controller;

import com.xingqiu.server.common.util.FileSignatureValidator;
import com.xingqiu.server.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/api/v1/admin/upload")
public class AdminUploadController {

    private static final Logger log = LoggerFactory.getLogger(AdminUploadController.class);
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of("video/mp4", "video/quicktime", "video/x-m4v", "video/webm");

    @Value("${xingqiu.upload.local-dir:./uploads}")
    private String localDir;

    @Value("${xingqiu.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file, ALLOWED_IMAGE_TYPES, false, 10 * 1024 * 1024L, "\\.(jpg|jpeg|png|gif|webp)$", ".png", "images", "图片");
    }

    @PostMapping("/video")
    public ApiResponse<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file, ALLOWED_VIDEO_TYPES, true, 200 * 1024 * 1024L, "\\.(mp4|mov|m4v|webm)$", ".mp4", "videos", "视频");
    }

    private ApiResponse<Map<String, String>> upload(MultipartFile file,
                                                    Set<String> allowedContentTypes,
                                                    boolean video,
                                                    long maxSize,
                                                    String extensionPattern,
                                                    String defaultExtension,
                                                    String subDir,
                                                    String label) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("INVALID_FILE", "文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            return ApiResponse.fail("INVALID_FILE_TYPE", "仅支持" + label + "文件");
        }

        if (file.getSize() > maxSize) {
            return ApiResponse.fail("FILE_TOO_LARGE", label + "大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = defaultExtension;
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }
        if (!ext.matches(extensionPattern)) {
            ext = defaultExtension;
        }

        boolean signatureOk = video
                ? FileSignatureValidator.isAllowedVideo(file, allowedContentTypes)
                : FileSignatureValidator.isAllowedImage(file, allowedContentTypes);
        if (!signatureOk) {
            return ApiResponse.fail("INVALID_FILE_TYPE", label + "文件内容与类型不匹配");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(localDir, subDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = normalizeBaseUrl(baseUrl) + "/" + subDir + "/" + fileName;
        log.info("{} uploaded: {} -> {}", label, originalName, url);

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
