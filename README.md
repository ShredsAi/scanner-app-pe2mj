# Scanner Hardware Discovery Service

Microservice for discovering TWAIN/WIA scanners

## Architecture
Hexagonal Architecture with layers:
- Shared (DTOs, Value Objects, Utils)
- Adapters (Primary ports/controllers)
- Application (Services, Ports)
- Domain (Entities, Value Objects, Services, Ports)
- Infrastructure (Repositories, External Services, Config)

## Prerequisites
- Java 17
- Maven
- Redis
- Native libraries for TWAIN/WIA (in `native-libs`)

## Setup
1. Build the project: `mvn clean package`
2. Run Redis (e.g., via `docker-compose up redis`)
3. Configure environment variables in `.env` or via Docker Compose:
   - `REDIS_HOST`
   - `REDIS_PORT`
   - `TWAIN_PATH`
   - `WIA_ENABLED`
4. Run the application:
   - Local: `java -jar target/scanner-hardware-discovery-1.0.0.jar`
   - Docker: `docker-compose up --build`

## REST API
- Trigger discovery: POST `/api/scanners/discovery/trigger`
- Health metrics: GET `/api/scanners/health/metrics`
- Connected device count: GET `/api/scanners/health/connected-count`

## Configuration
Spring Boot properties are in `src/main/resources/application*.yml`

## Native Libraries
Place TWAIN/WIA native libraries in `native-libs` directory

## Troubleshooting
- Check logs in `logs/`
- Ensure native libraries are accessible
- Verify Redis connectivity
