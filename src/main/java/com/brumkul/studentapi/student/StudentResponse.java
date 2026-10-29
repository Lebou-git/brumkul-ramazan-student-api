package com.brumkul.studentapi.student;

import java.time.Instant;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String groupName,
        Instant createdAt,
        Instant updatedAt) {

    static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getGroupName(),
                student.getCreatedAt(),
                student.getUpdatedAt());
    }
}
