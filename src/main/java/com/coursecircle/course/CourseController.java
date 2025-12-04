package com.coursecircle.course;

import com.coursecircle.dto.CourseResponse;
import com.coursecircle.dto.CreateCourseRequest;
import com.coursecircle.exception.ResourceNotFoundException;
import com.coursecircle.school.SchoolEntity;
import com.coursecircle.school.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final SchoolRepository schoolRepository;

    public CourseController(CourseRepository courseRepository, SchoolRepository schoolRepository) {
        this.courseRepository = courseRepository;
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public List<CourseResponse> listCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseEntity entity = new CourseEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setTerm(request.getTerm());

        if (request.getSchoolId() != null) {
            SchoolEntity school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found: " + request.getSchoolId()));
            entity.setSchool(school);
        }

        CourseEntity saved = courseRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    private CourseResponse toResponse(CourseEntity entity) {
        CourseResponse response = new CourseResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setTerm(entity.getTerm());
        response.setSchoolId(entity.getSchool() != null ? entity.getSchool().getId() : null);
        return response;
    }
}
