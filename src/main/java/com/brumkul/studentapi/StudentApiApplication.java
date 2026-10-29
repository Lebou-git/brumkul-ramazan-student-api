package com.brumkul.studentapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StudentApiApplication {

    private static final Logger log = LoggerFactory.getLogger(StudentApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StudentApiApplication.class, args);
    }

    @Bean
    ApplicationRunner startupInfo(@Value("${app.student.full-name}") String fullName,
                                  @Value("${app.student.group}") String group,
                                  @Value("${app.student.id}") String studentId,
                                  ObjectProvider<BuildProperties> buildProperties) {
        return args -> {
            BuildProperties build = buildProperties.getIfAvailable();
            String version = build != null ? build.getVersion() : "dev";
            log.info("Student API of {} (group {}, ID {}) started, version {}", fullName, group, studentId, version);
        };
    }
}
