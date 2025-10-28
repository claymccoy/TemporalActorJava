# GitHub Workflows Documentation

This project includes several GitHub Actions workflows for continuous integration and security.

## Workflows

### 1. Build and Test (`build.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` branch

**Jobs:**
- **test**: Runs tests on Java 17 and 21
- **build**: Builds the application after successful tests
- **release**: Creates releases on main branch pushes

**Features:**
- Multi-version Java testing (17, 21)
- Test result reporting with `dorny/test-reporter`
- Artifact uploads for build outputs
- Automated releases with versioning

### 2. CI/CD Pipeline (`ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches  
- Pull requests to `main` branch

**Jobs:**
- **test**: Comprehensive testing with matrix strategy
- **build-and-publish**: Build and release management
- **security-scan**: OWASP dependency vulnerability scanning

**Features:**
- Gradle caching for faster builds
- Test result uploads
- Security vulnerability reporting
- Release artifact management

### 3. Static Code Analysis (`static-analysis.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` branch

**Jobs:**
- **static-analysis**: SpotBugs, PMD, and Checkstyle analysis
- **security-scan**: OWASP dependency vulnerability scanning

**Features:**
- SpotBugs: Bug pattern detection and code quality
- PMD: Code style and potential issues
- Checkstyle: Code formatting and style compliance
- OWASP dependency scanning for vulnerabilities
- Detailed reporting and artifact uploads

## Configuration Files

### Dependabot (`dependabot.yml`)

Automatically creates pull requests for:
- Gradle dependency updates (weekly on Mondays)
- GitHub Actions updates (weekly on Mondays)

### Pull Request Template

Located at `.github/pull_request_template.md`, provides:
- Structured PR descriptions
- Change type categorization
- Testing checklists
- Review guidelines

## Security Features

1. **OWASP Dependency Check**: Scans for known vulnerabilities
2. **SpotBugs Analysis**: Static analysis for bug patterns and code quality
3. **PMD Analysis**: Code style and potential issue detection
4. **Checkstyle**: Code formatting and style compliance
5. **Dependabot**: Automated dependency updates
6. **JaCoCo Coverage**: Code coverage reporting and verification

## Usage Examples

### Local Development
```bash
# Run the same checks as CI
./gradlew ciBuild

# Run only static analysis
./gradlew staticAnalysis

# Generate coverage report
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html

# Run security scan
./gradlew dependencyCheckAnalyze
open build/reports/dependency-check-report.html

# Run individual static analysis tools
./gradlew spotbugsMain pmdMain checkstyleMain
```

### GitHub Actions
- All pushes trigger the build pipeline
- PRs run tests and security scans
- Main branch pushes create releases
- Weekly security scans run automatically
