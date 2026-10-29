# brumkul-ramazan-student-api

Description: Spring Boot CRUD API for student records.

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
| DELETE | /api/students/{id} | Delete a student |
