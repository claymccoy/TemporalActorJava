# Temporal Actor Example

A Spring Boot application demonstrating advanced Temporal workflow patterns, extending the [Temporal Java Getting Started Guide](https://learn.temporal.io/getting_started/java/first_program_in_java/) with a ledger implementation that showcases long-running workflows (actor pattern) and Temporal's "continue as new" feature.

[![Build and Test](https://github.com/claymccoy/TemporalActorJava/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/claymccoy/TemporalActorJava/actions/workflows/build.yml)

## 🚀 Quick Start

### Prerequisites
- Java 21 or later
- [Temporal Server](https://learn.temporal.io/getting_started/java/dev_environment/) running locally

### Build and Run
```bash
# Build the application
./gradlew build

# Run unit tests
./gradlew test

# Start the application
./gradlew bootRun

# Or run the jar directly
java -jar build/libs/temporal-example-1.0.0.jar
```

## 📡 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/service/transfer` | GET | Initiate a money transfer |
| `/v1/service/history` | GET | View transaction history |

### Quick Test URLs
- **Initial transfer**: http://localhost:8080/v1/service/transfer
- **Transaction history**: http://localhost:8080/v1/service/history

## 🔧 Development

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run CI build (includes tests, coverage, and security checks)
./gradlew ciBuild
```

### Code Quality
```bash
# Generate code coverage report
./gradlew jacocoTestReport

# Run static analysis tools (no security scan)
./gradlew pmdMain checkstyleMain

# Verify code coverage meets minimum threshold
./gradlew jacocoTestCoverageVerification

# Run individual analysis tools
./gradlew pmdMain         # Code style analysis  
./gradlew checkstyleMain  # Style compliance
```

## 🛠️ CI/CD

This project includes comprehensive GitHub Actions workflows:

- **Build and Test** (`build.yml`): Tests on Java 21, builds artifacts with Java 21
- **Static Analysis** (`static-analysis.yml`): PMD and Checkstyle code quality analysis with Java 21
- **Dependency Updates** (`dependabot.yml`): Automated dependency updates

## 🌐 Temporal Dashboard

- **Temporal Web UI**: http://localhost:8233/namespaces/default/workflows
- **Application**: http://localhost:8080

## 📚 Architecture

This application extends the [Temporal Java Getting Started Guide](https://learn.temporal.io/getting_started/java/first_program_in_java/) by adding:

- **Ledger Workflow**: A long-running workflow using the actor pattern that maintains transaction history
- **Continue As New**: Demonstrates proper implementation of Temporal's continue-as-new feature to handle unlimited workflow execution
- **Unit Testing**: Comprehensive tests for long-running workflows and continue-as-new scenarios using advanced testing techniques
  - **Continue-as-New Testing**: Verifies workflow history events to ensure continue-as-new is triggered correctly
  - **Workflow History Inspection**: Tests examine `WorkflowExecutionContinuedAsNewEventAttributes` in execution history
  - **State Persistence Testing**: Validates that workflow state is properly maintained across continue-as-new boundaries
- **Money Transfer Pattern**: Distributed transaction implementation with compensation
- **Activity Implementation**: External service calls with retry policies

### Key Features

1. **Actor Pattern**: The ledger workflow acts as a stateful actor that processes events over time
2. **Continue As New**: Prevents workflow history from growing too large by resetting execution state
3. **Event Sourcing**: Transaction history is maintained within the workflow state
4. **Comprehensive Testing**: Unit tests that verify continue-as-new behavior and workflow state management
   - **Event History Verification**: Tests inspect workflow execution history for continue-as-new events
   - **State Boundary Testing**: Validates data persistence across workflow restarts
   - **Long-Running Workflow Patterns**: Demonstrates testing strategies for workflows that may run indefinitely

## 📖 Documentation

- **[Development Guide](DEVELOPMENT.md)**: Detailed setup and development workflow
- **[Workflow Documentation](.github/workflows/README.md)**: CI/CD pipeline details

## 🏗️ Project Structure

```
src/main/java/com/claymccoy/meteor/shower/
├── Service.java                    # Spring Boot main application
├── ServiceResource.java            # REST API endpoints
├── ServiceConfig.java              # Application configuration
├── moneytransfer/
│   ├── AccountActivity.java        # Temporal activity interface
│   ├── AccountActivityImpl.java    # Activity implementation
│   ├── LedgerService.java          # Business service layer
│   ├── LedgerWorkflow*.java        # Ledger workflow definitions
│   ├── MoneyTransferWorkflow*.java # Transfer workflow definitions
│   └── MoneyTransferWorker.java    # Temporal worker setup
└── temporal/
    └── TemporalConfig.java         # Temporal client configuration
```
