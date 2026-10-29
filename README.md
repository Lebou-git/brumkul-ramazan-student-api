# brumkul-ramazan-student-api

Description: Spring Boot CRUD REST API for managing student records stored in PostgreSQL, documented by Ramazan Brumkul.
| Field | Value |
| --- | --- |
| Author | Ramazan Brumkul |
| Group | IT2-2301 |
| Student ID | 37911 |
| GitHub | Lebou-git |

## Technology stack

- Java 17, Spring Boot 3.5, Gradle wrapper
- PostgreSQL 16
- Docker and Docker Compose
- Jenkins CI

## Project structure

```
brumkul-ramazan-student-api/
├── scripts/            Bash scripts: system report and API test
├── logs/               runtime logs, ignored by Git
├── backups/            backups, ignored by Git
├── src/                Spring Boot source code and tests
├── Dockerfile          multi-stage image build
├── docker-compose.yml  application and PostgreSQL
├── Jenkinsfile         CI pipeline
├── build.gradle        Gradle build
└── README.md
```

## Run with Docker Compose

```
cp .env.example .env
docker compose up -d --build
```

Application URL: http://localhost:8081/brumkul-ramazan

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | /api/info | Owner and version of the application |
| GET | /api/students | List all students |
| GET | /api/students/{id} | Get one student |
| POST | /api/students | Create a student |
| PUT | /api/students/{id} | Update a student |
| DELETE | /api/students/{id} | Delete a student |

## Changes by Ramazan Brumkul

### feature/brumkul-ramazan-crud

Implemented the update operation `PUT /api/students/{id}`. It replaces the first name, last name, email and group of an existing student. The request body is validated (400 Bad Request), a nonexistent ID returns 404 Not Found, and an email that already belongs to another student returns 409 Conflict. MockMvc tests cover a successful update and a nonexistent student.

## Error responses

Errors are returned in the RFC 9457 Problem Details format (`application/problem+json`).

| Status | When |
| --- | --- |
| 400 Bad Request | Invalid request body, for example an empty name or a wrong email |
| 404 Not Found | A student with this ID does not exist |
| 409 Conflict | The email already belongs to another student |
