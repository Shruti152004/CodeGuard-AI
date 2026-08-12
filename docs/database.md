# Database Schema Documentation — CodeGuard AI

The database is built on **PostgreSQL** (version 15) and managed using **Flyway Migrations** in the Spring Boot backend.

## Schema Layout

### 1. `users`
- Stores user credentials and association to organizations.
- Primary Key: `id (SERIAL)`
- Fields: `username (VARCHAR(50) UNIQUE)`, `email (VARCHAR(100) UNIQUE)`, `password (VARCHAR(255))`, `active (BOOLEAN)`, `organization_id (INT FK)`.

### 2. `roles` & `user_roles`
- Controls access permissions (RBAC).
- `roles` Table: `id (SERIAL)`, `name (VARCHAR(50) UNIQUE)`.
- `user_roles` Table: `user_id (FK)`, `role_id (FK)`.

### 3. `organizations`
- Groups repositories and users.
- Primary Key: `id (SERIAL)`
- Fields: `name (VARCHAR(100) UNIQUE)`.

### 4. `analyses`
- Records code reviews triggered via REST or webhook integrations.
- Primary Key: `id (SERIAL)`
- Fields: `repository_name (VARCHAR(150))`, `branch (VARCHAR(100))`, `status (VARCHAR(50))`, `overall_score (INT)`, `technical_debt_hours (DOUBLE PRECISION)`.

### 5. `issues`
- Individual code irregularities flagged by static/AI engines.
- Primary Key: `id (SERIAL)`
- Fields: `analysis_id (INT FK)`, `title (VARCHAR(150))`, `category (VARCHAR(50))`, `severity (VARCHAR(50))`, `file_path (VARCHAR(255))`, `line_number (INT)`, `source (VARCHAR(50))`.

### 6. `technical_debt`
- Configurable debt hours accumulated per repository.
- Primary Key: `id (SERIAL)`
- Fields: `repository_name (VARCHAR(150) UNIQUE)`, `total_hours (DOUBLE PRECISION)`.
