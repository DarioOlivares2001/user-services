// src/main/java/com/matchwork/user_service/service/S3Service.java
package com.matchwork.user_service.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final Tika tika = new Tika();

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.baseUrl:}")
    private String baseUrl; // URL base personalizada si usas CloudFront

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Sube una foto de perfil al bucket S3
     */
    public String uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {
        validateImageFile(file);
        
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        String key = String.format("profiles/%d/photo/%s_%s", 
            userId, 
            UUID.randomUUID().toString(), 
            sanitizedFileName
        );
        
        return uploadFile(file, key);
    }

    /**
     * Sube un CV al bucket S3
     */
    public String uploadCV(Long userId, MultipartFile file) throws IOException {
        validateDocumentFile(file);
        
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        String key = String.format("profiles/%d/cv/%s_%s", 
            userId, 
            UUID.randomUUID().toString(), 
            sanitizedFileName
        );
        
        return uploadFile(file, key);
    }

    /**
     * Sube logo de empresa al bucket S3
     */
    public String uploadCompanyLogo(Long userId, MultipartFile file) throws IOException {
        validateImageFile(file);  // puedes crear un validateLogoFile si quieres cambiar tamaños
        
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        String key = String.format("profiles/%d/logo/%s_%s",
            userId, UUID.randomUUID().toString(), sanitizedFileName);
        
        return uploadFile(file, key);
    }

    /**
     * Sanitiza el nombre del archivo eliminando caracteres problemáticos
     */
    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "file";
        }

        // Normalizar caracteres unicode (elimina acentos)
        String normalized = Normalizer.normalize(originalFileName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Reemplazar espacios y caracteres especiales
        String sanitized = normalized
                .replaceAll("\\s+", "_")  // Espacios por guiones bajos
                .replaceAll("[^a-zA-Z0-9._-]", "")  // Solo caracteres alfanuméricos, puntos, guiones y guiones bajos
                .replaceAll("_{2,}", "_")  // Multiple underscores por uno solo
                .replaceAll("^[._-]+|[._-]+$", "");  // Eliminar caracteres especiales al inicio y final

        // Si después de la sanitización queda vacío, usar un nombre por defecto
        if (sanitized.isEmpty()) {
            sanitized = "file";
        }

        // Limitar longitud del nombre
        if (sanitized.length() > 100) {
            String name = sanitized.substring(0, 90);
            String extension = getFileExtension(originalFileName);
            sanitized = name + (extension.isEmpty() ? "" : "." + extension);
        }

        return sanitized;
    }

    /**
     * Extrae la extensión del archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Alternativa: Generar nombre completamente nuevo basado en timestamp
     */
    private String generateSafeFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%d_%s%s", 
            timestamp, 
            uuid, 
            extension.isEmpty() ? "" : "." + extension
        );
    }

    /**
     * Método genérico para subir archivos
     */
    private String uploadFile(MultipartFile file, String key) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Retornar la URL del archivo
            return getFileUrl(key);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a S3: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo del bucket S3
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                
                s3Client.deleteObject(deleteObjectRequest);
            }
        } catch (Exception e) {
            // Log del error pero no fallar la operación principal
            System.err.println("Error eliminando archivo de S3: " + e.getMessage());
        }
    }

    /**
     * Genera la URL del archivo
     */
    private String getFileUrl(String key) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl + "/" + key;
        }
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    /**
     * Extrae la key del archivo desde la URL
     */
    private String extractKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        
        if (baseUrl != null && !baseUrl.isEmpty() && url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length() + 1);
        }
        
        // Formato estándar de S3
        String bucketPattern = String.format("https://%s.s3.amazonaws.com/", bucketName);
        if (url.startsWith(bucketPattern)) {
            return url.substring(bucketPattern.length());
        }
        
        return null;
    }

    /**
     * Valida que el archivo sea una imagen
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar los 5MB");
        }

        // Validar tipo MIME
        String mimeType = tika.detect(file.getInputStream());
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Tipos específicos permitidos
        if (!mimeType.equals("image/jpeg") && 
            !mimeType.equals("image/png") && 
            !mimeType.equals("image/webp")) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WebP");
        }
    }

    /**
     * Valida que el archivo sea un documento válido para CV
     */
    private void validateDocumentFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tamaño (máximo 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("El CV no puede superar los 10MB");
        }

        // Validar tipo MIME
        String mimeType = tika.detect(file.getInputStream());
        if (!mimeType.equals("application/pdf") && 
            !mimeType.equals("application/msword") &&
            !mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF, DOC o DOCX");
        }
    }
}