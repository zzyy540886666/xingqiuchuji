package com.xingqiu.server.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public final class FileSignatureValidator {

    private FileSignatureValidator() {
    }

    public static boolean isAllowedImage(MultipartFile file, Set<String> allowedContentTypes) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            return false;
        }
        byte[] header = readHeader(file, 16);
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> startsWithAscii(header, "GIF87a") || startsWithAscii(header, "GIF89a");
            case "image/webp" -> startsWithAscii(header, "RIFF") && asciiAt(header, 8, "WEBP");
            default -> false;
        };
    }

    public static boolean isAllowedVideo(MultipartFile file, Set<String> allowedContentTypes) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            return false;
        }
        byte[] header = readHeader(file, 16);
        String normalized = contentType.toLowerCase();
        if ("video/webm".equals(normalized)) {
            return startsWith(header, 0x1A, 0x45, 0xDF, 0xA3);
        }
        if ("video/mp4".equals(normalized) || "video/quicktime".equals(normalized) || "video/x-m4v".equals(normalized)) {
            return asciiAt(header, 4, "ftyp");
        }
        return false;
    }

    private static byte[] readHeader(MultipartFile file, int maxBytes) throws IOException {
        byte[] header = new byte[maxBytes];
        try (InputStream input = file.getInputStream()) {
            int read = input.read(header);
            if (read <= 0) {
                return new byte[0];
            }
            if (read == maxBytes) {
                return header;
            }
            byte[] actual = new byte[read];
            System.arraycopy(header, 0, actual, 0, read);
            return actual;
        }
    }

    private static boolean startsWith(byte[] actual, int... expected) {
        if (actual.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((actual[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithAscii(byte[] actual, String expected) {
        return asciiAt(actual, 0, expected);
    }

    private static boolean asciiAt(byte[] actual, int offset, String expected) {
        if (actual.length < offset + expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if (actual[offset + i] != (byte) expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
