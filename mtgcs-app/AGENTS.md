# Java Spring Boot Project Context

- These instructions apply to all files under this directory.

## Project Information

- **Tech Stack:** Java 21, Spring Boot 3.4+, Maven 3.9+
- **Architecture:** Layered (Controller → Service → Repository) with DTOs, MapStruct for mapping
- **Naming Convention:** PascalCase for classes, camelCase for methods/variables.

## Common Development Commands

- **Run Application:** `./mvnw spring-boot:run`
- **Run All Tests:** `./mvnw test`
- **Build & Package:** `./mvnw clean package -DskipTests`

## Coding Conventions

- **Dependency Injection:** Always use constructor-based injection; avoid `@Autowired` on fields.
- **Data Handling:** Use Java Records for DTOs and immutable data.
- **Validation:** Use `@Valid` and Bean Validation annotations for input validation.
- **Errors:** Centralize error handling with `@ControllerAdvice`.
- **Database:** Use Spring Data JPA repositories for data access and transactions.
