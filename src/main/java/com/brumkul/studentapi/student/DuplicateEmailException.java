package com.brumkul.studentapi.student;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Student with email " + email + " already exists");
    }
}
