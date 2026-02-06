# AGENTS.md - Guide for AI Coding Agents

This document provides guidelines and instructions for AI agents working on the miniEMS project.

## Project Overview

miniEMS is a Spring Boot 4.0.1 application (Java 21) that provides energy management system functionality including:
- MQTT message processing for BMS (Battery Management System) telemetry
- InfluxDB time-series data storage
- WebSocket real-time data push
- Modbus communication via j2mod

## Build Commands

### Full Build
```bash
mvn clean package -DskipTests
```

### Run Tests
```bash
mvn test
```

### Run Single Test Class
```bash
mvn test -Dtest=TelemetryServiceTest
```

### Run Single Test Method
```bash
mvn test -Dtest=TelemetryServiceTest#testSaveTelemetry
```

### Compile Only (Skip Tests)
```bash
mvn compile -DskipTests
```

### Package with Assembly (Generate bin Directory)
```bash
mvn clean package -DskipTests
# Output: target/ems-0.0.1-bin/
```

### Run Application Locally
```bash
cd target/ems-0.0.1-bin
./bin/start.sh
./bin/stop.sh
```

## Code Style Guidelines

### Imports
- Organize imports: standard library → third-party → project imports
- No wildcard imports (*)
- Each import on its own line
- Alphabetical order within groups

### Naming Conventions
- **Classes**: PascalCase (e.g., `MqttMessageHandler`, `TelemetryService`)
- **Methods**: camelCase (e.g., `saveTelemetry()`, `handleMessage()`)
- **Variables**: camelCase (e.g., `influxDBClient`, `mqttMessageHandler`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRIES`)
- **Packages**: lowercase (e.g., `com.sysbreak.ems.config`)

### Logging
- Use Lombok `@Slf4j` for all classes
- Use log levels appropriately: `log.debug()`, `log.info()`, `log.warn()`, `log.error()`
- Log messages should be clear and include context

### Error Handling
- Catch specific exceptions, not generic `Exception`
- Log errors with context using `log.error("message: {}", detail)`
- Never swallow exceptions silently

### Dependency Injection
- Use constructor injection (not field injection with `@Autowired`)
- Final fields for injected dependencies
- All beans properly annotated: `@Component`, `@Service`, `@Configuration`

### Comments
- Use Chinese comments (as project convention)
- Javadoc for public methods and classes
- Inline comments for complex logic

### Code Structure
```
src/main/java/com/sysbreak/ems/
├── config/          # Configuration classes
├── mqtt/           # MQTT related classes
├── service/        # Business logic services
├── model/          # Domain models
└── websocket/     # WebSocket handlers
```

### Configuration Files
- `application.yaml`: Spring Boot configuration
- `assembly.xml`: Maven Assembly descriptor for packaging
- `bin/start.sh` / `bin/stop.sh`: Application startup/shutdown scripts

### MQTT Configuration Notes
- Topic: configured via `mqtt.topic` property
- Broker URL: configured via `mqtt.broker.url` property
- When working with Spring Integration MQTT, use explicit channel binding to avoid naming prefix issues

## Common Issues

### Spring Integration Channel Naming
When using `@ServiceActivator` and `MqttPahoMessageDrivenChannelAdapter`:
- Channel bean names automatically get application prefix (e.g., `miniEMS.emsInboundChannel`)
- Use consistent channel names in both `@Bean` definitions and `@ServiceActivator`
- For complex scenarios, directly set handler on adapter to avoid channel subscription issues

### Docker Compose
- Docker Compose support is disabled (`excludeDockerCompose: true` in pom.xml)
- If enabling, ensure `docker-compose.yaml` exists in the runtime directory
