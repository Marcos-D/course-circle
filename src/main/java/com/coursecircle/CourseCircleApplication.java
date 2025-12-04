package com.coursecircle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.coursecircle")
public class CourseCircleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseCircleApplication.class, args);
    }
}
