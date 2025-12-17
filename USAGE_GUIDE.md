# Database Backup Utility - Usage Guide

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Run the Application

```bash
java -jar target/database-backup-utility-1.0.0.jar
```

The application will start in interactive mode with a Spring Shell prompt.

## Available Commands

### Help Command

```bash
help
```

Lists all available commands and their descriptions.

### MySQL Backup

**Basic MySQL Backup:**
```bash
backup-mysql --host localhost --port 3306 --database mydb --username root --password secret
```

**MySQL Backup with Custom Path:**
```bash
backup-mysql --host localhost --database mydb --username root --password secret --backup-path /var/backups/mysql
```

**MySQL Backup without Compression:**
```bash
backup-mysql --host localhost --database mydb --username root --password secret --compress false
```

### PostgreSQL Backup

**Basic PostgreSQL Backup:**
```bash
backup-postgresql --host localhost --port 5432 --database mydb --username postgres --password secret
```

**PostgreSQL Backup to Specific Directory:**
```bash
backup-postgresql --host localhost --database mydb --username postgres --password secret --backup-path /opt/backups/postgresql
```

### MongoDB Backup

**Basic MongoDB Backup:**
```bash
backup-mongodb --host localhost --port 27017 --database mydb --username admin --password secret
```

**MongoDB Backup with Custom Options:**
```bash
backup-mongodb --host localhost --database mydb --username admin --password secret --backup-path /data/backups/mongo --compress true
```

### SQLite Backup

**Basic SQLite Backup:**
```bash
backup-sqlite --database-file /path/to/database.db
```

**SQLite Backup to Custom Location:**
```bash
backup-sqlite --database-file /var/lib/app/app.db --backup-path /backups/sqlite
```

### Cloud Storage (AWS S3)

**Backup MySQL to S3:**
```bash
backup-to-s3 --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --bucket my-backup-bucket --region us-east-1 --access-key YOUR_AWS_ACCESS_KEY --secret-key YOUR_AWS_SECRET_KEY
```

**Backup PostgreSQL to S3:**
```bash
backup-to-s3 --db-type POSTGRESQL --host localhost --port 5432 --database mydb --username postgres --password secret --bucket my-backup-bucket --region us-west-2 --access-key YOUR_AWS_ACCESS_KEY --secret-key YOUR_AWS_SECRET_KEY
```

### Scheduled Backups

**Schedule Daily Backup at 2 AM:**
```bash
schedule-backup --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --cron "0 0 2 * * ?"
```

**Schedule Backup Every 6 Hours:**
```bash
schedule-backup --db-type POSTGRESQL --host localhost --port 5432 --database mydb --username postgres --password secret --cron "0 0 */6 * * ?"
```

**Schedule Weekday Backups at Noon:**
```bash
schedule-backup --db-type MONGODB --host localhost --port 27017 --database mydb --username admin --password secret --cron "0 0 12 * * MON-FRI"
```

**Cancel Scheduled Backup:**
```bash
cancel-schedule --database mydb
```

**List All Scheduled Backups:**
```bash
list-schedules
```

## Cron Expression Examples

| Expression | Description |
|------------|-------------|
| `0 0 2 * * ?` | Daily at 2:00 AM |
| `0 0 */6 * * ?` | Every 6 hours |
| `0 0 12 * * MON-FRI` | Weekdays at noon |
| `0 0 0 1 * ?` | First day of every month at midnight |
| `0 0 3 ? * SUN` | Every Sunday at 3:00 AM |
| `0 */30 * * * ?` | Every 30 minutes |
| `0 0 0,12 * * ?` | Twice daily (midnight and noon) |

## Cron Expression Format

```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
│ │ │ │ │ ┌───────────── day of week (0-6 or SUN-SAT)
│ │ │ │ │ │
* * * * * ?
```

## Command Options

### Common Options

- `--host`: Database server hostname (default: localhost)
- `--port`: Database server port (varies by database type)
- `--database`: Name of the database to backup
- `--username`: Database username
- `--password`: Database password
- `--backup-path`: Directory to store backups (default: ./backups)
- `--compress`: Enable compression (default: true)

### Default Ports

- MySQL/MariaDB: 3306
- PostgreSQL: 5432
- MongoDB: 27017
- SQLite: N/A (file-based)

### AWS S3 Options

- `--bucket`: S3 bucket name
- `--region`: AWS region (e.g., us-east-1, us-west-2, eu-west-1)
- `--access-key`: AWS access key ID
- `--secret-key`: AWS secret access key

## Output and Logs

### Backup Files

Backup files are automatically named with timestamps:

- MySQL: `{database}_{timestamp}_mysql.sql.gz`
- PostgreSQL: `{database}_{timestamp}_postgresql.sql.gz`
- MongoDB: `{database}_{timestamp}_mongodb.tar.gz`
- SQLite: `{database}_{timestamp}_sqlite.db.gz`

### Logs

- **Console Output**: Real-time progress and results
- **Application Log**: `logs/backup-utility.log`
- **Backup Activity Log**: `backup_log.txt`

## Examples

### Example 1: Simple Local Backup

```bash
# Start the application
java -jar target/database-backup-utility-1.0.0.jar

# In the shell prompt
shell:> backup-mysql --host localhost --database ecommerce --username backup_user --password backup123

# Output
✓ Backup completed successfully!
File: ./backups/ecommerce_20231217_143022_mysql.sql.gz
Size: 15.23 MB
Duration: 12.45 seconds
```

### Example 2: Scheduled Cloud Backup

```bash
# Schedule daily backup to S3 at 3 AM
shell:> schedule-backup --db-type POSTGRESQL --host db.example.com --port 5432 --database production --username admin --password secret123 --cron "0 0 3 * * ?" --backup-path /tmp/backups

# Output
✓ Backup scheduled successfully for database 'production' with cron: 0 0 3 * * ?
```

### Example 3: SQLite Backup

```bash
# Backup SQLite database
shell:> backup-sqlite --database-file /var/lib/app/app.db --backup-path /backups/sqlite --compress true

# Output
✓ Backup completed successfully!
File: /backups/sqlite/app_20231217_143022_sqlite.db.gz
Size: 2.15 MB
Duration: 1.23 seconds
```

## Troubleshooting

### Command Not Found Errors

**Issue**: `mysqldump: command not found`

**Solution**: Install the MySQL client tools
```bash
# Ubuntu/Debian
sudo apt-get install mysql-client

# CentOS/RHEL
sudo yum install mysql

# macOS
brew install mysql-client
```

**Issue**: `pg_dump: command not found`

**Solution**: Install PostgreSQL client tools
```bash
# Ubuntu/Debian
sudo apt-get install postgresql-client

# CentOS/RHEL
sudo yum install postgresql

# macOS
brew install postgresql
```

**Issue**: `mongodump: command not found`

**Solution**: Install MongoDB database tools
```bash
# Ubuntu/Debian
sudo apt-get install mongodb-database-tools

# CentOS/RHEL
sudo yum install mongodb-database-tools

# macOS
brew tap mongodb/brew
brew install mongodb-database-tools
```

### Connection Errors

**Issue**: Connection refused or timeout

**Solutions**:
1. Verify the database server is running
2. Check firewall rules allow connections
3. Verify the host and port are correct
4. Ensure the database user has remote access permissions

**Issue**: Authentication failed

**Solutions**:
1. Verify username and password are correct
2. Check database user has required privileges
3. For MySQL, user may need `SELECT`, `LOCK TABLES`, and `SHOW VIEW` privileges
4. For PostgreSQL, user needs read access to the database

### Backup Failures

**Issue**: Permission denied when creating backup directory

**Solution**: Ensure the user running the application has write permissions
```bash
mkdir -p /path/to/backups
chmod 755 /path/to/backups
```

**Issue**: Disk space errors

**Solution**: Check available disk space
```bash
df -h
```

### AWS S3 Upload Errors

**Issue**: Access denied

**Solutions**:
1. Verify AWS credentials are correct
2. Ensure the IAM user/role has S3 permissions
3. Check the bucket exists and is in the specified region

**Issue**: Bucket not found

**Solutions**:
1. Verify the bucket name is correct
2. Ensure you have access to the bucket
3. Check the region is correct

## Security Best Practices

### 1. Credential Management

**DON'T** hardcode credentials:
```bash
# Bad - credentials visible in command history
backup-mysql --password MyPassword123
```

**DO** use environment variables or configuration files:
```bash
# Better - use environment variable
export DB_PASSWORD=MyPassword123
# Then use a wrapper script that reads from env
```

### 2. File Permissions

Ensure backup files have appropriate permissions:
```bash
chmod 600 /path/to/backups/*
```

### 3. Cloud Storage

- Use IAM roles when running on AWS EC2 instead of access keys
- Rotate access keys regularly
- Use bucket encryption at rest
- Enable bucket versioning for backup protection

### 4. Network Security

- Use SSL/TLS for database connections when possible
- Restrict database access to specific IP addresses
- Use VPN or SSH tunnels for remote backups

## Performance Tips

### Large Databases

For large databases (>100GB):

1. **Schedule during off-peak hours** to minimize impact
2. **Use compression** to reduce storage space
3. **Consider incremental backups** for very large databases
4. **Monitor backup duration** and adjust scheduling accordingly

### Network Transfers

For cloud uploads:

1. **Compress before uploading** to reduce transfer time
2. **Use appropriate AWS region** closest to your server
3. **Consider direct upload** to cloud instead of local then upload
4. **Monitor bandwidth usage** during business hours

## Advanced Configuration

### Custom Backup Directory Structure

You can organize backups by date:
```bash
backup-mysql --database mydb --username root --password secret --backup-path ./backups/$(date +%Y-%m-%d)
```

### Backup Retention

Implement backup rotation with a cron job:
```bash
# Keep only last 7 days of backups
find /path/to/backups -type f -mtime +7 -delete
```

### Multiple Database Backups

Create a script to backup multiple databases:
```bash
#!/bin/bash
databases=("db1" "db2" "db3")
for db in "${databases[@]}"; do
    echo "backup-mysql --host localhost --database $db --username root --password secret"
done
```

## Support

For issues, questions, or contributions:
- GitHub Issues: https://github.com/spaceadh/database-backup-utility/issues
- Documentation: README.md
