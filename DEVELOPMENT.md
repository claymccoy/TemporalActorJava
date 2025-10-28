# Development Guide

## Getting Started

### Prerequisites
- Java 17 or later
- Docker (optional, for Temporal server)
- IDE with Java support (IntelliJ IDEA, VS Code, etc.)

### Setup
1. Clone the repository
2. Start Temporal server (see options below)
3. Run the application

### Running Temporal Server

#### Option 1: Docker Compose (Recommended)
```bash
docker-compose up -d temporal postgresql
```

#### Option 2: Local Temporal CLI
```bash
# Install Temporal CLI
curl -sSf https://temporal.download/cli.sh | sh

# Start local server
temporal server start-dev
```

#### Option 3: Docker only
```bash
docker run -p 7233:7233 -p 8233:8233 temporalio/auto-setup:latest
```

## Development Workflow

### Building and Testing
```bash
# Clean build
./gradlew clean build

# Run tests only
./gradlew test

# Continuous testing
./gradlew test --continuous
```

### Code Quality
```bash
# Run all static analysis
./gradlew staticAnalysis

# Individual tools
./gradlew spotbugsMain     # Bug detection
./gradlew pmdMain          # Code analysis
./gradlew checkstyleMain   # Style checking

# Security scan
./gradlew dependencyCheckAnalyze
```

### Running the Application
```bash
# Development mode (auto-restart)
./gradlew bootRun

# Build and run JAR
./gradlew build
java -jar build/libs/temporal-example-1.0.0.jar

# With custom config
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Docker Development
```bash
# Build image
docker build -t temporal-example .

# Run with compose (includes Temporal)
docker-compose up

# Run app only
docker run -p 8080:8080 temporal-example
```

## IDE Setup

### IntelliJ IDEA
1. Import as Gradle project
2. Install plugins: Checkstyle, PMD, SpotBugs
3. Configure code style: `checkstyle.xml`
4. Enable annotation processing

### VS Code
1. Install Java Extension Pack
2. Install Gradle for Java
3. Configure Java 17+ in settings

## Testing

### Unit Tests
- Located in `src/test/java`
- Use JUnit 5 and Spring Boot Test
- Mock external dependencies

### Integration Tests
- Test Temporal workflows with `temporal-testing`
- Use TestContainers for database tests (if needed)
- Test REST endpoints with MockMvc

### Running Specific Tests
```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests MoneyTransferWorkflowTest

# Test pattern
./gradlew test --tests "*Workflow*"
```

## Configuration

### Application Properties
- `src/main/resources/application.properties`
- Environment-specific: `application-{profile}.properties`

### Temporal Configuration
- Client setup in `MoneyTransferModule.java`
- Worker configuration in `MoneyTransferWorker.java`

## Debugging

### Application Debugging
1. Run with `--debug-jvm` flag
2. Attach debugger to port 5005
3. Set breakpoints in IDE

### Temporal Debugging
1. Access Temporal Web UI: http://localhost:8233
2. View workflow executions and history
3. Check worker logs in application output

## Common Issues

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### Temporal Connection Issues
- Ensure Temporal server is running on port 7233
- Check application.properties for correct host/port
- Verify network connectivity

### Build Issues
```bash
# Clean Gradle cache
./gradlew --stop
rm -rf ~/.gradle/caches/

# Refresh dependencies
./gradlew build --refresh-dependencies
```

## Contributing

1. Create feature branch from `develop`
2. Make changes and add tests
3. Run quality checks: `./gradlew ciBuild`
4. Submit pull request
5. All CI checks must pass

## Useful Commands

```bash
# Generate project reports
./gradlew build --scan

# Dependency tree
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Clean everything
./gradlew clean
```
