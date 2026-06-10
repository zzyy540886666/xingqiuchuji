package com.xingqiu.server.repair.controller;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.util.FileSignatureValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repair/attachments")
public class RepairAttachmentController {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${xingqiu.upload.local-dir:./uploads}")
    private String localDir;

    @Value("${xingqiu.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostMapping("/images")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()
                || file.getSize() > MAX_IMAGE_BYTES
                || !ALLOWED_TYPES.contains(file.getContentType())
                || !FileSignatureValidator.isAllowedImage(file, ALLOWED_TYPES)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "仅支持不超过10MB的 JPG、PNG 或 WebP 图片");
        }
        String suffix = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path dir = Paths.get(localDir, "repair");
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(fileName));
        return ApiResponse.ok(Map.of("url", baseUrl + "/repair/" + fileName));
    }
}
