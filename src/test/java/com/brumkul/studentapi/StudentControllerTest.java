package com.brumkul.studentapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brumkul.studentapi.student.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.student.full-name=Ramazan Brumkul",
        "app.student.group=IT2-2301",
        "app.student.id=37911"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void infoShowsStudentName() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Ramazan Brumkul"))
                .andExpect(jsonPath("$.group").value("IT2-2301"))
                .andExpect(jsonPath("$.studentId").value("37911"));
    }

    @Test
    void createStudentReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ramazan", "Brumkul", "Ramazan.Brumkul@Example.com", "IT2-2301")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/students/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Ramazan"))
                .andExpect(jsonPath("$.lastName").value("Brumkul"))
                .andExpect(jsonPath("$.email").value("ramazan.brumkul@example.com"))
                .andExpect(jsonPath("$.groupName").value("IT2-2301"));
    }

    @Test
    void findAllReturnsEveryStudent() throws Exception {
        createStudent("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301");
        createStudent("Lebou", "Brumkul", "lebou.brumkul@example.com", "IT2-2301");

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Ramazan"))
                .andExpect(jsonPath("$[1].firstName").value("Lebou"));
    }

    @Test
    void findByIdReturnsStudent() throws Exception {
        long id = createStudent("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301");

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.lastName").value("Brumkul"));
    }

    @Test
    void updateStudentChangesData() throws Exception {
        long id = createStudent("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301");

        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301-DevOps")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.groupName").value("IT2-2301-DevOps"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updateMissingStudentReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/students/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Student not found"));
    }

    @Test
    void deleteStudentRemovesRecord() throws Exception {
        long id = createStudent("Temp", "Brumkul", "temp.brumkul@example.com", "IT2-2301");

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingStudentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Student not found"))
                .andExpect(jsonPath("$.detail").value("Student with id 999999 not found"));
    }

    @Test
    void invalidStudentReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "Brumkul", "not-an-email", "IT2-2301")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid student data"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        createStudent("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ramazan", "Brumkul", "ramazan.brumkul@example.com", "IT2-2301")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate email"));
    }

    private long createStudent(String firstName, String lastName, String email, String groupName) throws Exception {
        String body = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(firstName, lastName, email, groupName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private static String json(String firstName, String lastName, String email, String groupName) {
        return """
                {"firstName":"%s","lastName":"%s","email":"%s","groupName":"%s"}
                """.formatted(firstName, lastName, email, groupName);
    }
}
