package com.coursecircle.file;

import com.coursecircle.auth.CurrentUser;
import com.coursecircle.config.AppProperties;
import com.coursecircle.course.CourseEntity;
import com.coursecircle.course.CourseRepository;
import com.coursecircle.dto.CourseFileResponse;
import com.coursecircle.dto.UploadFileResponse;
import com.coursecircle.exception.ResourceNotFoundException;
import com.coursecircle.user.UserEntity;
import com.coursecircle.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseFileService {

    private static final Logger log = LoggerFactory.getLogger(CourseFileService.class);

    private final CourseFileRepository courseFileRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public CourseFileService(CourseFileRepository courseFileRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             AppProperties appProperties) {
        this.courseFileRepository = courseFileRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public UploadFileResponse uploadFile(CurrentUser currentUser, Long courseId, MultipartFile file) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        UserEntity uploader = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getId()));

        String storedFilename = generateStoredFilename(file.getOriginalFilename());
        Path storageDir = Path.of(appProperties.getFiles().getStorageDir()).toAbsolutePath().normalize();
        Path destination = storageDir.resolve(storedFilename);

        try {
            Files.createDirectories(storageDir);
            Files.copy(file.getInputStream(), destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }

        CourseFileEntity entity = new CourseFileEntity();
        entity.setOriginalFilename(file.getOriginalFilename());
        entity.setStoredFilename(storedFilename);
        entity.setContentType(file.getContentType());
        entity.setSizeBytes(file.getSize());
        entity.setCourse(course);
        entity.setUploader(uploader);
        entity.setUploadedAt(Instant.now());

        CourseFileEntity saved = courseFileRepository.save(entity);
        log.info("Stored file {} for course {}", saved.getId(), courseId);
        return toUploadResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseFileResponse> listFiles(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        return courseFileRepository.findByCourseIdOrderByUploadedAtDesc(courseId)
                .stream()
                .map(this::toCourseFileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId) {
        CourseFileEntity entity = getFileMetadata(fileId);
        Path storageDir = Path.of(appProperties.getFiles().getStorageDir()).toAbsolutePath().normalize();
        Path filePath = storageDir.resolve(entity.getStoredFilename());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found on disk: " + fileId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid file path", e);
        }
    }

    @Transactional(readOnly = true)
    public CourseFileEntity getFileMetadata(Long fileId) {
        return courseFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
    }

    private String generateStoredFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private UploadFileResponse toUploadResponse(CourseFileEntity entity) {
        UploadFileResponse response = new UploadFileResponse();
        response.setId(entity.getId());
        response.setOriginalFilename(entity.getOriginalFilename());
        response.setContentType(entity.getContentType());
        response.setSizeBytes(entity.getSizeBytes());
        response.setUploadedAt(entity.getUploadedAt());
        return response;
    }

    private CourseFileResponse toCourseFileResponse(CourseFileEntity entity) {
        CourseFileResponse response = new CourseFileResponse();
        response.setId(entity.getId());
        response.setOriginalFilename(entity.getOriginalFilename());
        response.setContentType(entity.getContentType());
        response.setSizeBytes(entity.getSizeBytes());
        response.setUploadedAt(entity.getUploadedAt());
        response.setUploaderId(entity.getUploader() != null ? entity.getUploader().getId() : null);
        return response;
    }
}
