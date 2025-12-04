# CourseCircle

Spring Boot backend (Java 17) with minimal static UIs for a study-session tracker, course file uploads, and basic course/user management.

## Features
- Health check: `GET /api/health`
- Sessions: start (`POST /api/sessions`), end (`PATCH /api/sessions/{id}/end`), list (`GET /api/sessions`)
- Courses: list/create (`GET|POST /api/courses`)
- Users: list/create (`GET|POST /api/users`) — needed because `CurrentUserService` returns user id `1`
- Course files: upload/list/download under `/api/courses/{courseId}/files`
- Static UIs: student dashboard at `/`, dev console at `/dev.html`

## Prereqs
- JDK 17
- Maven
- PostgreSQL running locally (default URL `jdbc:postgresql://localhost:5432/coursecircle`, user/pass `postgres` unless you change `application.yml`)

## Setup
1) Install dependencies and run:
```
mvn spring-boot:run
```
2) Seed data (until real auth/CRUD are added):
   - Create user id 1 via dev console (`/dev.html`) or:
     ```
     curl -X POST http://localhost:8080/api/users \
       -H "Content-Type: application/json" \
       -d '{"email":"student@example.edu","displayName":"Sample Student"}'
     ```
     Ensure the first user is id 1 because `CurrentUserService` stubs that user.
   - Create a course via dev console or:
     ```
     curl -X POST http://localhost:8080/api/courses \
       -H "Content-Type: application/json" \
       -d '{"code":"CSDSA","name":"Data Structures & Algorithms","term":"Fall 2025"}'
     ```
     Note the returned `id` for sessions/uploads.

3) Try the UIs:
   - Student page `/`: enter a course ID, start/end a session, upload a file. Timer pauses on tab blur/hidden.
   - Dev page `/dev.html`: health, list/create users, list/create courses, start/end sessions, list sessions, upload/list/download files.

## Notes
- `.env` is ignored; configure secrets locally. Rotate any credentials that were previously committed.
- `ddl-auto: update` is on for now; consider Flyway/Liquibase for migrations.
- Auth is stubbed; do not use as-is in production.
