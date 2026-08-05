package com.chat.app.service;

import com.chat.app.config.AppProperties;
import com.chat.app.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppProperties appProperties;

    public String storeFile(MultipartFile file, String subDir) {
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String fileName = UUID.randomUUID() + extension;

            Path uploadDir = Paths.get(appProperties.getUpload().getDir(), subDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + fileName;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = Paths.get(appProperties.getUpload().getDir())
                    .resolve(filePath.replace("/uploads/", ""))
                    .normalize()
                    .toAbsolutePath();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            }
            throw new BadRequestException("File not found: " + filePath);
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + filePath);
        }
    }
}
