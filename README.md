# Feedback Hub

A Java REST API and web front end for collecting, browsing and summarising user feedback.
Users submit feedback through a web form; the API validates it, stores it in MySQL, and exposes
endpoints to filter, update, delete and summarise the data.

## Tech stack
- Java 17, Spring Boot 3 (Spring Web, Spring Data JPA, Bean Validation)
- MySQL 8 for storage (H2 in-memory database for tests)
- JUnit 5 and MockMvc for API tests
- Plain HTML/JavaScript front end served by the same application

## Features
- Full CRUD REST API with request validation and consistent JSON error responses
- Filtering by category and minimum rating, with pagination and sorting
- Summary endpoint: total count, average rating, counts per category and per rating
- Web page with a submission form, filter controls, paginated list and live summary
- Layered design: controller, service, repository, DTOs, global exception handler

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/feedback` | Create feedback (201 + `Location` header) |
| GET | `/api/feedback` | List, with `category`, `minRating`, `page`, `size`, `sort` params |
| GET | `/api/feedback/{id}` | Get one entry (404 if missing) |
| PUT | `/api/feedback/{id}` | Update an entry |
| DELETE | `/api/feedback/{id}` | Delete an entry (204) |
| GET | `/api/feedback/summary` | Aggregate statistics |

Example request:

```bash
curl -X POST http://localhost:8080/api/feedback \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","category":"BUG","rating":4,"message":"Button is misaligned"}'
```

Validation rules: name and message required, valid email, category one of
`GENERAL | BUG | FEATURE | PRAISE`, rating from 1 to 5. Invalid requests return 400 with a
`details` object listing each failing field.

## Run it locally

Requirements: JDK 17+, Maven, and a MySQL instance (Docker is the quickest way).

```bash
docker compose up -d        # starts MySQL on port 3306
mvn spring-boot:run         # starts the app on http://localhost:8080
```

Open http://localhost:8080 for the web page. If your MySQL settings differ, set the
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` environment variables.

## Run the tests

```bash
mvn test
```

Tests use an in-memory H2 database, so MySQL is not needed.

## Project structure

```
src/main/java/com/feedbackhub
  controller/   REST endpoints
  service/      business logic and summary calculation
  repository/   Spring Data JPA queries
  model/        JPA entity and enum
  dto/          request and response objects
  exception/    custom exception and global error handler
src/main/resources/static/index.html   web front end
src/test/java/...                      API tests
```

## Possible next steps
- Authentication for the delete and update endpoints
- Export feedback as CSV
- Docker image for the application itself
