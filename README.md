# Agni
> A personal finance tracking app to visualize your spending habits and raise awareness about your financial behavior.

This project is an application designed to help you track your expenses, analyze your financial behavior through graphs, and develop better money habits. It offers semi-automated features, and future versions will include AI and workflow automation.

To get the most out of this tool, treat it like a financial notebook. Consistent usage is key to long-term insight and improvement.

## Why the Name "Agni"?
In my culture, the Agni ethnic group is often stereotyped as stingy. However, I believe that being "stingy" isn't necessarily a bad trait, as long as it doesn't lead to a miserable lifestyle. Instead, it can be a mindset focused on financial health and responsibility. I see it as being economical and intentional with money.

## Stack

**Frontend**
- Vue.js with Nuxt

**Backend**
- Kotlin with Spring Boot

**Database**
- PostgreSQL 17

**Mobile**
- Swift with SwiftUI *(coming soon)*
- Kotlin for Android *(coming soon)*

## Architecture

The backend (Kotlin with Spring Boot) follows **Clean Architecture** principles combined with a **Domain-Driven Design (DDD)** approach for modeling business logic.

## Features
<!--TODO: list features and future features-->

---

## Quick Start

### Prerequisites
- [Docker](https://www.docker.com/) & Docker Compose

That's it — no local JDK or Node.js required for development.

### Clone the repository
```bash
git clone https://github.com/xx-code/agni-think.git
cd agni-think/
```

---

##  Development

The dev environment is fully Dockerized. It includes:
- **Frontend** (Nuxt, with HMR on port `3000` + `24678`)
- **Backend** (Spring Boot with DevTools auto-reload, port `8080`)
- **PostgreSQL 17** (port `5432`)
- **Adminer** — database UI at [http://localhost:8888](http://localhost:8888)

### Start the dev environment
```bash
docker compose -f docker/docker-compose.development.yml up --build
```

| Service   | URL                          |
|-----------|------------------------------|
| Frontend  | http://localhost:3000        |
| Backend   | http://localhost:8080        |
| Adminer   | http://localhost:8888        |
| DB        | localhost:5432               |

Default dev DB credentials (pre-configured, no `.env` needed):
- **User:** `dev`
- **Password:** `dev`
- **Database:** `agni_dev`

### Stop the dev environment
```bash
docker compose -f docker/docker-compose.development.yml down
```

To also remove the dev database volume:
```bash
docker compose -f docker/docker-compose.development.yml down -v
```

---

## Deployment

### 1. Configure environment variables

Copy the example file and fill in your values:
```bash
cp .env.example .env
```

See [`.env.example`](.env.example) for all required variables.

### 2. Build & push Docker images

Use the release scripts to build and push versioned images to your registry:

```bash
# Backend
VERSION=1.0.0 REGISTRY=your-registry.io ./bin/build_push_release_backend.sh

# Frontend
VERSION=1.0.0 REGISTRY=your-registry.io ./bin/build_push_release_frontend.sh
```

This will tag and push:
- `your-registry.io/agni-api-spring-boot:1.0.0`
- `your-registry.io/agni-web:1.0.0`

### 3. Deploy with Docker Compose

```bash
docker compose -f docker/docker-compose.yml up -d
```

The production stack includes:
- **Frontend** (Nuxt, served from registry image)
- **Backend** (Spring Boot with `prod` profile, served from registry image)
- **PostgreSQL 17** (persistent volume `agni_data`)
- **Adminer** — accessible on the port defined by `ADMINER_PORT`

### Production image details

The backend uses a **multi-stage build**:
- Build stage: `eclipse-temurin:21-jdk` — compiles the Spring Boot fat JAR via Gradle
- Runtime stage: `eclipse-temurin:21-jre-alpine` — minimal JRE image, runs as a non-root `spring` user

### Stop production
```bash
docker compose -f docker/docker-compose.yml down
```

---

## Project Structure

```
agni-think/
├── apps/
│   ├── agni-web/          # Nuxt frontend
│   └── agni_api/          # Spring Boot backend
├── bin/
│   ├── build_push_release_backend.sh
│   └── build_push_release_frontend.sh
├── config/
│   ├── api/
│   │   ├── Dockerfile
│   │   └── Dockerfile.development
│   └── frontend/
│       ├── Dockerfile
│       └── Dockerfile.development
├── docker/
│   ├── docker-compose.yml
│   └── docker-compose.development.yml
└── .env.example
```
