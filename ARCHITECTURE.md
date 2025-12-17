# Database Backup Utility - Architecture Documentation

## Overview

The Database Backup Utility is a Spring Boot-based CLI application that provides a unified interface for backing up multiple types of databases. The architecture follows Clean Architecture principles with clear separation of concerns.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Spring Shell CLI                          │
│                      (User Interface Layer)                      │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Command Handlers                             │
│                  (BackupCommands.java)                          │
│  ┌────────────────────────────────────────────────────┐        │
│  │ • backup-mysql         • backup-to-s3              │        │
│  │ • backup-postgresql    • schedule-backup           │        │
│  │ • backup-mongodb       • cancel-schedule           │        │
│  │ • backup-sqlite        • list-schedules            │        │
│  └────────────────────────────────────────────────────┘        │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Service Layer                                 │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              BackupOrchestrator                        │    │
│  │  (Coordinates backup workflow)                         │    │
│  └─────────────────────┬──────────────────────────────────┘    │
│                        │                                         │
│        ┌───────────────┼───────────────┐                       │
│        ▼               ▼               ▼                        │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐                   │
│  │  Backup   │  │ Storage   │  │ Logging  │                   │
│  │ Services  │  │ Service   │  │ Service  │                   │
│  └───────────┘  └───────────┘  └──────────┘                   │
│        │                                                         │
│        ▼                                                         │
│  ┌─────────────────────────────────────────────┐              │
│  │ • MySQLBackupService                        │              │
│  │ • PostgreSQLBackupService                   │              │
│  │ • MongoDBBackupService                      │              │
│  │ • SQLiteBackupService                       │              │
│  │ • CompressionService                        │              │
│  │ • SchedulerService (Quartz)                 │              │
│  └─────────────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   External Systems                               │
│  ┌─────────────┐  ┌──────────┐  ┌────────────┐  ┌──────────┐  │
│  │   MySQL     │  │PostgreSQL│  │  MongoDB   │  │  SQLite  │  │
│  │  (mysqldump)│  │(pg_dump) │  │(mongodump) │  │ (file)   │  │
│  └─────────────┘  └──────────┘  └────────────┘  └──────────┘  │
│                                                                  │
│  ┌─────────────┐  ┌──────────┐  ┌────────────┐                │
│  │   AWS S3    │  │  Azure   │  │   GCP      │                │
│  │  (Storage)  │  │  (Future)│  │  (Future)  │                │
│  └─────────────┘  └──────────┘  └────────────┘                │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Model Layer (`com.dbbackup.model`)

#### BackupConfig
- Contains all configuration for a backup operation
- Fields: database type, connection details, credentials, storage settings
- Used to pass configuration between layers

#### BackupResult
- Represents the outcome of a backup operation
- Fields: success status, file path, size, duration, error messages
- Returned by backup services to report results

#### DatabaseType (Enum)
- Supported database types: MYSQL, POSTGRESQL, MONGODB, SQLITE, MARIADB

#### StorageType (Enum)
- Storage options: LOCAL, AWS_S3, AZURE_BLOB, GOOGLE_CLOUD

### 2. Service Layer (`com.dbbackup.service`)

#### BackupOrchestrator
**Purpose**: Main coordinator for backup operations

**Responsibilities**:
- Selects appropriate backup service based on database type
- Coordinates backup execution workflow
- Handles cloud storage uploads
- Manages logging of backup activities

**Dependencies**:
- List<BackupService> - All backup service implementations
- StorageService - Cloud and local storage operations
- BackupLogService - Activity logging

#### BackupService (Interface)
**Purpose**: Contract for all database-specific backup implementations

**Methods**:
- `backup(BackupConfig)` - Performs the backup
- `supports(BackupConfig)` - Checks if service can handle the database type

#### Database-Specific Services

##### MySQLBackupService
- Uses `mysqldump` command-line tool
- Exports database to SQL file
- Supports single-transaction backups
- Includes routines and triggers
- Security: Uses environment variable for password (MYSQL_PWD)

##### PostgreSQLBackupService
- Uses `pg_dump` command-line tool
- Exports database in plain SQL format
- Verbose logging for debugging
- Security: Uses PGPASSWORD environment variable

##### MongoDBBackupService
- Uses `mongodump` command-line tool
- Creates directory with BSON dumps
- Supports tar.gz compression of entire backup directory
- Handles collection-level backups

##### SQLiteBackupService
- Simple file copy operation
- No external tools required
- Fast and reliable
- Ideal for small to medium databases

#### CompressionService
**Purpose**: Handles file compression

**Features**:
- GZIP compression for single files
- TAR.GZ for directories (MongoDB)
- Automatic cleanup of uncompressed files
- Configurable compression levels

#### StorageService
**Purpose**: Manages backup storage

**Features**:
- Local filesystem storage
- AWS S3 integration with SDK v2
- Secure credential management
- Future: Azure Blob, Google Cloud Storage

**S3 Implementation**:
- Uses AWS SDK for Java v2
- Supports custom regions
- Handles authentication with access keys
- Proper error handling and logging

#### SchedulerService
**Purpose**: Manages scheduled backups using Quartz

**Features**:
- Cron-based scheduling
- Job persistence in memory (configurable)
- Dynamic job creation and cancellation
- Context injection for services

**Quartz Integration**:
- Uses Quartz 2.3.2
- RAMJobStore for in-memory scheduling
- 5 worker threads by default
- SpringBeanJobFactory integration

#### BackupLogService
**Purpose**: Logs all backup activities

**Features**:
- Dual logging (console + file)
- Structured log format with timestamps
- Tracks backup attempts and results
- Creates backup_log.txt for audit trail

### 3. Command Layer (`com.dbbackup.command`)

#### BackupCommands
**Purpose**: Spring Shell command handlers

**Commands Implemented**:

1. **backup-mysql** - MySQL database backup
2. **backup-postgresql** - PostgreSQL database backup
3. **backup-mongodb** - MongoDB database backup
4. **backup-sqlite** - SQLite database backup
5. **backup-to-s3** - Backup with cloud storage
6. **schedule-backup** - Schedule recurring backups
7. **cancel-schedule** - Cancel scheduled backup
8. **list-schedules** - List all scheduled backups

**Features**:
- Descriptive help text for each command
- Default values for common parameters
- Formatted output with success/failure indicators
- Human-readable size and duration formatting

### 4. Configuration Layer (`com.dbbackup.config`)

#### QuartzConfig
**Purpose**: Configures Quartz scheduler

**Features**:
- Creates and configures Scheduler bean
- Injects services into scheduler context
- Enables job access to Spring-managed beans

## Design Patterns Used

### 1. Strategy Pattern
- **BackupService** interface with multiple implementations
- Allows adding new database types without modifying existing code
- Runtime selection of appropriate backup strategy

### 2. Facade Pattern
- **BackupOrchestrator** provides simplified interface to complex subsystem
- Hides complexity of coordinating multiple services
- Single entry point for backup operations

### 3. Dependency Injection
- Constructor-based injection throughout
- Loose coupling between components
- Easy testing and mocking

### 4. Builder Pattern
- **BackupConfig** and **BackupResult** use Lombok @Builder
- Fluent API for object creation
- Immutable objects with all fields

### 5. Template Method Pattern
- **BackupService** interface defines template
- Each implementation provides specific backup algorithm
- Common workflow enforced by interface

## Technology Stack

### Core Framework
- **Spring Boot 3.2.0** - Application framework
- **Spring Shell 3.2.0** - CLI framework
- **Java 17** - Programming language

### Scheduling
- **Quartz Scheduler 2.3.2** - Job scheduling

### Storage
- **AWS SDK for Java 2.21.0** - S3 integration

### Compression
- **Apache Commons Compress 1.25.0** - File compression

### Database Drivers
- **MySQL Connector/J** - MySQL connectivity
- **PostgreSQL JDBC Driver** - PostgreSQL connectivity
- **MongoDB Java Driver** - MongoDB connectivity
- **SQLite JDBC** - SQLite connectivity

### Utilities
- **Lombok** - Reduces boilerplate code
- **SLF4J/Logback** - Logging framework

### Build Tool
- **Maven 3.6+** - Dependency management and build

## Data Flow

### Backup Operation Flow

1. **User Input**
   - User enters command in Spring Shell
   - Command parameters validated
   - BackupConfig object created

2. **Orchestration**
   - BackupOrchestrator receives request
   - Logs backup attempt
   - Selects appropriate BackupService

3. **Backup Execution**
   - Service executes database-specific backup
   - Creates backup file in specified location
   - Applies compression if enabled

4. **Storage**
   - If cloud storage configured, uploads backup
   - Maintains local copy
   - Records file metadata

5. **Logging & Response**
   - Logs backup result
   - Returns formatted response to user
   - Updates backup history log

### Scheduled Backup Flow

1. **Schedule Creation**
   - User provides cron expression
   - Quartz job created with configuration
   - Trigger registered with scheduler

2. **Job Execution**
   - Quartz fires trigger at scheduled time
   - Job retrieves BackupOrchestrator from context
   - Executes normal backup flow

3. **Recurring Execution**
   - Job reschedules itself based on cron
   - Continues until explicitly cancelled
   - Survives application restarts (with persistent job store)

## Security Considerations

### 1. Credential Handling
- Database passwords passed via environment variables to CLI tools
- No passwords in process listings
- Recommendations for secure credential storage in documentation

### 2. File Permissions
- Backup files created with restricted permissions
- Configurable backup directory ownership
- Temporary files cleaned up after compression

### 3. Network Security
- Support for SSL/TLS database connections
- Encrypted cloud storage transfers
- No plaintext credentials in logs

### 4. Input Validation
- Command parameters validated by Spring Shell
- Database connection parameters sanitized
- Path traversal prevention

## Extensibility

### Adding a New Database Type

1. Create new enum value in `DatabaseType`
2. Implement `BackupService` interface
3. Use appropriate backup tool or library
4. Add Spring `@Service` annotation
5. Add CLI command in `BackupCommands`

Example:
```java
@Service
@RequiredArgsConstructor
public class OracleBackupService implements BackupService {
    private final CompressionService compressionService;
    
    @Override
    public BackupResult backup(BackupConfig config) {
        // Implementation using Oracle exp/expdp
    }
    
    @Override
    public boolean supports(BackupConfig config) {
        return config.getDatabaseType() == DatabaseType.ORACLE;
    }
}
```

### Adding a New Cloud Storage Provider

1. Create new enum value in `StorageType`
2. Add provider-specific SDK dependency
3. Implement upload logic in `StorageService`
4. Update CLI commands with new parameters

## Performance Characteristics

### MySQL Backup
- **Speed**: ~500 MB/minute (network dependent)
- **Memory**: Minimal (streams to file)
- **CPU**: Low (mostly I/O bound)

### PostgreSQL Backup
- **Speed**: ~400 MB/minute
- **Memory**: Minimal (streams to file)
- **CPU**: Low (mostly I/O bound)

### MongoDB Backup
- **Speed**: ~300 MB/minute
- **Memory**: Moderate (BSON processing)
- **CPU**: Medium (compression intensive)

### SQLite Backup
- **Speed**: Disk I/O limited
- **Memory**: Minimal (file copy)
- **CPU**: Very low

### Compression
- **GZIP**: ~50-70% size reduction
- **TAR.GZ**: Similar to GZIP
- **Performance**: ~100 MB/second compression

## Future Enhancements

### Planned Features
1. **Incremental Backups** - Only backup changes
2. **Encryption** - Encrypt backups at rest
3. **Backup Verification** - Validate backup integrity
4. **Restore Functionality** - Restore from backups
5. **Web UI** - Web-based management interface
6. **Email Notifications** - Alert on backup success/failure
7. **Backup Rotation** - Automatic cleanup of old backups
8. **Differential Backups** - Combine full and incremental
9. **Multi-tenancy** - Support multiple organizations
10. **Metrics & Monitoring** - Prometheus/Grafana integration

### Technology Improvements
1. Reactive programming with Spring WebFlux
2. Native image compilation with GraalVM
3. Container orchestration with Kubernetes
4. Configuration management with Vault
5. Distributed scheduling with Kubernetes CronJobs

## Testing Strategy

### Unit Tests
- Service layer methods
- Utility functions
- Model validation

### Integration Tests
- Database connectivity
- CLI command execution
- File system operations

### End-to-End Tests
- Complete backup workflows
- Scheduled backup execution
- Cloud storage integration

## Monitoring & Observability

### Logging
- **Level**: INFO for normal operations, ERROR for failures
- **Format**: Structured logs with timestamps
- **Location**: Console + file (backup-utility.log)

### Metrics (Future)
- Backup success/failure rate
- Backup duration trends
- Storage usage tracking
- Scheduler job execution stats

### Health Checks (Future)
- Database connectivity status
- Cloud storage availability
- Disk space availability
- Scheduler health

## Deployment Options

### Standalone JAR
```bash
java -jar database-backup-utility-1.0.0.jar
```

### Docker Container (Future)
```bash
docker run -v /backups:/backups dbbackup/utility
```

### Kubernetes CronJob (Future)
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: db-backup
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: dbbackup/utility:latest
```

## Contributing

### Code Style
- Follow Java code conventions
- Use Lombok to reduce boilerplate
- Write meaningful comments for complex logic
- Keep methods small and focused

### Pull Request Process
1. Create feature branch
2. Implement feature with tests
3. Update documentation
4. Submit PR with description
5. Address review feedback

## License

This project is open source under the MIT License.
