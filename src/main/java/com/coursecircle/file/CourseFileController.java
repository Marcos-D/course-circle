package com.coursecircle.file;

import com.coursecircle.auth.CurrentUser;
import com.coursecircle.auth.CurrentUserService;
import com.coursecircle.dto.CourseFileResponse;
import com.coursecircle.dto.UploadFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/files")
@Validated
public class CourseFileController {

    private final CourseFileService courseFileService;
    private final CurrentUserService currentUserService;

    public CourseFileController(CourseFileService courseFileService, CurrentUserService currentUserService) {
        this.courseFileService = courseFileService;
        this.currentUserService = currentUserService;
    }

    /**
     * Intended for legitimate study resources; schools' content policies must be respected by uploaders.
     */
    @PostMapping
    public ResponseEntity<UploadFileResponse> uploadFile(@PathVariable("courseId") Long courseId,
                                                         @RequestParam("file") MultipartFile file) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        UploadFileResponse response = courseFileService.uploadFile(currentUser, courseId, file);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseFileResponse>> listFiles(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseFileService.listFiles(courseId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("courseId") Long courseId,
                                                 @PathVariable("fileId") Long fileId) {
        CourseFileEntity metadata = courseFileService.getFileMetadata(fileId);
        Resource resource = courseFileService.downloadFile(fileId);
        String contentDisposition = "attachment; filename=\"" + metadata.getOriginalFilename() + "\"";
        MediaType mediaType = metadata.getContentType() != null
                ? MediaType.parseMediaType(metadata.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(mediaType)
                .body(resource);
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable("courseId") Long courseId,
                                                @PathVariable("fileId") Long fileId) {
        CourseFileEntity metadata = courseFileService.getFileMetadata(fileId);
        Resource resource = courseFileService.downloadFile(fileId);
        String contentDisposition = "inline; filename=\"" + metadata.getOriginalFilename() + "\"";
        MediaType mediaType = metadata.getContentType() != null
                ? MediaType.parseMediaType(metadata.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(mediaType)
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable("courseId") Long courseId,
                                           @PathVariable("fileId") Long fileId) {
        courseFileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
