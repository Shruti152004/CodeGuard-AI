# System Architecture — CodeGuard AI

CodeGuard AI is structured as a robust, containerized monorepo microservice platform.

```
                         CODEGUARD AI

                ┌────────────────────────┐
                │       FRONTENDS        │
                │                        │
                │ React + TypeScript     │
                │ Developer Portal       │
                │                        │
                │ Angular + TypeScript   │
                │ Admin Portal           │
                └───────────┬────────────┘
                            │
                            ↓
                     Spring Boot API
                            │
             ┌──────────────┼──────────────┐
             ↓              ↓              ↓
        PostgreSQL        Redis           Kafka
                                           │
                 ┌─────────────────────────┼───────────────┐
                 ↓                         ↓               ↓
          NodeJS Service             Flask Webhooks    Analysis Jobs
                 │                                         │
                 ↓                                         ↓
          WebSockets                             Language Analyzers
                                                         │
                         ┌───────────────────────────────┼───────────────┐
                         ↓                               ↓               ↓
                    C# Analyzer                      Python AI      Java Analyzer
                    + Roslyn                          + FastAPI
                         │                               │
                         └───────────────┬───────────────┘
                                         ↓
                                  Analysis Results
                                         ↓
                                  Spring Boot
                                         ↓
                                    PostgreSQL
                                         ↓
                                  React / Angular
```

## System Components

1. **Spring Boot Core Backend**:
   - Manages relational domain logic: user registration, auth (JWT), database schemas, scoring engine computations, and audit logs.
2. **React Developer Portal**:
   - The primary interface for engineers to monitor code health scorecards, view inline code quality issues, and trigger analysis runs.
3. **Angular Admin Portal**:
   - Provides organization management, role updates, platform health metrics, and audit logs analysis.
4. **Flask Webhook Receiver**:
   - Validates push notifications from GitHub using HMAC-SHA256 signature checking and dispatches analysis requests to Apache Kafka.
5. **Python AI & AST Analyzer (FastAPI)**:
   - Evaluates python code syntax using Abstract Syntax Trees (AST) and handles LLM code reviews (mock or real Gemini integration).
6. **C# Roslyn Static Analyzer**:
   - Inspects C# source code for dangerous API usages, syntax irregularities, and code smells.
7. **Node.js Notification Service**:
   - Subscribes to events and broadcasts status notifications to frontend portals via WebSockets.
