package com.brumkul.studentapi.student;

import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<StudentResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(StudentResponse::from)
                .toList();
    }

    public StudentResponse findById(Long id) {
        return StudentResponse.from(getStudent(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        Student student = new Student(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                request.groupName().trim());
        return StudentResponse.from(repository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getStudent(id));
    }

    private Student getStudent(Long id) {
        return repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
