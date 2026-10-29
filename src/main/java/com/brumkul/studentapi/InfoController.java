package com.brumkul.studentapi;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    private final AppInfo info;

    public InfoController(@Value("${spring.application.name}") String application,
                          @Value("${app.student.full-name}") String fullName,
                          @Value("${app.student.group}") String group,
                          @Value("${app.student.id}") String studentId,
                          ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties build = buildProperties.getIfAvailable();
        String version = build != null ? build.getVersion() : "dev";
        this.info = new AppInfo(application, fullName, group, studentId, version);
    }

    @GetMapping({"/", "/api/info"})
    public AppInfo info() {
        return info;
    }

    public record AppInfo(String application, String owner, String group, String studentId, String version) {
    }
}
