# Database Backup Utility

A powerful Spring Boot-based command-line interface (CLI) utility for backing up various types of databases. This tool supports multiple database management systems, automatic scheduling, compression, and cloud storage integration.

## Features

- **Multi-Database Support**: MySQL, PostgreSQL, MongoDB, SQLite, and MariaDB
- **Automatic Scheduling**: Schedule backups using cron expressions with Quartz
- **Compression**: Automatic GZIP/TAR.GZ compression of backup files
- **Cloud Storage**: Upload backups to AWS S3 (Azure and Google Cloud support planned)
- **Local Storage**: Store backups on local filesystem
- **Activity Logging**: Comprehensive logging of all backup operations
- **Interactive CLI**: User-friendly command-line interface powered by Spring Shell

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Database client tools installed:
  - `mysqldump` for MySQL/MariaDB backups
  - `pg_dump` for PostgreSQL backups
  - `mongodump` for MongoDB backups
  - SQLite backups use file copy (no additional tools needed)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/spaceadh/database-backup-utility.git
cd database-backup-utility
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
java -jar target/database-backup-utility-1.0.0.jar
```

## Usage

### Starting the CLI

```bash
java -jar target/database-backup-utility-1.0.0.jar
```

The application will start in interactive mode with a shell prompt.

### Available Commands

#### Backup MySQL Database

```bash
backup-mysql --host localhost --port 3306 --database mydb --username root --password secret --backup-path ./backups --compress true
```

#### Backup PostgreSQL Database

```bash
backup-postgresql --host localhost --port 5432 --database mydb --username postgres --password secret --backup-path ./backups --compress true
```

#### Backup MongoDB Database

```bash
backup-mongodb --host localhost --port 27017 --database mydb --username admin --password secret --backup-path ./backups --compress true
```

#### Backup SQLite Database

```bash
backup-sqlite --database-file /path/to/database.db --backup-path ./backups --compress true
```

#### Backup to AWS S3

```bash
backup-to-s3 --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --backup-path ./backups --bucket my-backup-bucket --region us-east-1 --access-key YOUR_ACCESS_KEY --secret-key YOUR_SECRET_KEY --compress true
```

#### Schedule Automatic Backups

Schedule daily backups at 2 AM:
```bash
schedule-backup --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --backup-path ./backups --cron "0 0 2 * * ?" --compress true
```

Common cron expressions:
- `0 0 2 * * ?` - Daily at 2:00 AM
- `0 0 */6 * * ?` - Every 6 hours
- `0 0 12 * * MON-FRI` - Weekdays at noon
- `0 0 0 1 * ?` - First day of every month at midnight

#### Cancel Scheduled Backup

```bash
cancel-schedule --database mydb
```

#### List Scheduled Backups

```bash
list-schedules
```

### Command Options

Most commands support these common options:

- `--host`: Database server hostname (default: localhost)
- `--port`: Database server port (default varies by database type)
- `--database`: Name of the database to backup
- `--username`: Database username
- `--password`: Database password
- `--backup-path`: Directory to store backups (default: ./backups)
- `--compress`: Enable compression (default: true)

## Configuration

You can customize default settings in `src/main/resources/application.yml`:

```yaml
backup:
  default-path: ./backups
  compression-enabled: true
  retention-days: 30

logging:
  level:
    com.dbbackup: INFO
```

## Backup File Naming

Backup files are automatically named with timestamps:

- MySQL: `{database}_{timestamp}_mysql.sql.gz`
- PostgreSQL: `{database}_{timestamp}_postgresql.sql.gz`
- MongoDB: `{database}_{timestamp}_mongodb.tar.gz`
- SQLite: `{database}_{timestamp}_sqlite.db.gz`

## Logging

All backup operations are logged to:
- Console output
- `logs/backup-utility.log` - Application logs
- `backup_log.txt` - Backup operation history

## Architecture

The application follows a modular architecture:

```
com.dbbackup
├── model/              # Data models (BackupConfig, BackupResult, etc.)
├── service/            # Business logic
│   ├── BackupService   # Interface for backup operations
│   ├── *BackupService  # Database-specific implementations
│   ├── BackupOrchestrator  # Coordinates backup workflow
│   ├── CompressionService  # File compression
│   ├── StorageService      # Cloud storage integration
│   ├── SchedulerService    # Backup scheduling
│   └── BackupLogService    # Activity logging
├── command/            # CLI command handlers
└── config/             # Spring configuration
```

## Cloud Storage Integration

### AWS S3

To backup to AWS S3, you need:
1. An AWS account with S3 access
2. An S3 bucket created
3. AWS access key and secret key with appropriate permissions

Minimum required S3 permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject"
      ],
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

### Azure Blob Storage (Planned)

Azure Blob Storage support is planned for a future release.

### Google Cloud Storage (Planned)

Google Cloud Storage support is planned for a future release.

## Security Considerations

- **Credentials**: Never hardcode credentials. Use environment variables or secure configuration management.
- **Backup Files**: Ensure backup directories have appropriate permissions.
- **Cloud Storage**: Use IAM roles and least-privilege access for cloud credentials.
- **Network**: Use SSL/TLS for database connections when possible.

## Troubleshooting

### Command Not Found Errors

If you get "command not found" errors for database tools:

**MySQL/MariaDB:**
```bash
# Ubuntu/Debian
sudo apt-get install mysql-client

# macOS
brew install mysql-client
```

**PostgreSQL:**
```bash
# Ubuntu/Debian
sudo apt-get install postgresql-client

# macOS
brew install postgresql
```

**MongoDB:**
```bash
# Ubuntu/Debian
sudo apt-get install mongodb-database-tools

# macOS
brew tap mongodb/brew
brew install mongodb-database-tools
```

### Connection Issues

- Verify database server is running and accessible
- Check firewall rules
- Verify credentials
- Ensure database user has appropriate privileges

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is open source and available under the MIT License.

## Support

For issues and questions, please open an issue on GitHub.
