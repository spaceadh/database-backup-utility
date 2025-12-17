# Quick Start Guide

## Get Started in 3 Steps

### Step 1: Build the Application

```bash
mvn clean package
```

This will create: `target/database-backup-utility-1.0.0.jar`

### Step 2: Run the Application

```bash
java -jar target/database-backup-utility-1.0.0.jar
```

You'll see the Spring Shell prompt:
```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║   Database Backup Utility                                 ║
║   Version 1.0.0                                           ║
║                                                           ║
║   Supported Databases:                                    ║
║   • MySQL / MariaDB                                       ║
║   • PostgreSQL                                            ║
║   • MongoDB                                               ║
║   • SQLite                                                ║
║                                                           ║
║   Type 'help' to see available commands                   ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝

shell:>
```

### Step 3: Run Your First Backup

Try these simple commands:

#### Backup MySQL
```bash
shell:> backup-mysql --host localhost --database mydb --username root --password secret
```

#### Backup PostgreSQL
```bash
shell:> backup-postgresql --host localhost --database mydb --username postgres --password secret
```

#### Backup SQLite (No Server Required!)
```bash
shell:> backup-sqlite --database-file /path/to/database.db
```

#### Schedule Daily Backup
```bash
shell:> schedule-backup --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --cron "0 0 2 * * ?"
```

## Example Output

```
shell:> backup-sqlite --database-file /tmp/test.db --backup-path ./backups

✓ Backup completed successfully!
File: ./backups/test_20231217_143022_sqlite.db.gz
Size: 0.01 MB
Duration: 0.15 seconds
```

## Quick Test

Try this complete example:

```bash
# 1. Create a test SQLite database
mkdir -p /tmp/test-db
echo "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT);" | sqlite3 /tmp/test-db/test.db

# 2. Start the backup utility
java -jar target/database-backup-utility-1.0.0.jar

# 3. In the shell prompt, run:
shell:> backup-sqlite --database-file /tmp/test-db/test.db --backup-path ./backups

# 4. Check your backup
shell:> exit
ls -lh ./backups/
```

## Common Commands

| Command | Description |
|---------|-------------|
| `help` | Show all available commands |
| `backup-mysql` | Backup a MySQL database |
| `backup-postgresql` | Backup a PostgreSQL database |
| `backup-mongodb` | Backup a MongoDB database |
| `backup-sqlite` | Backup a SQLite database |
| `backup-to-s3` | Backup and upload to AWS S3 |
| `schedule-backup` | Schedule automatic backups |
| `cancel-schedule` | Cancel a scheduled backup |
| `list-schedules` | List all scheduled backups |
| `exit` | Exit the application |

## Need More Help?

- **Detailed Usage**: See [USAGE_GUIDE.md](USAGE_GUIDE.md)
- **Architecture**: See [ARCHITECTURE.md](ARCHITECTURE.md)
- **Examples**: Check the `examples/` directory

## Requirements

### Database Tools (Install as needed)

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

**SQLite:**
- Already included! No additional installation needed.

## Troubleshooting

### "Command not found" errors
Install the appropriate database client tools (see Requirements above).

### "Connection refused" errors
- Check that the database server is running
- Verify the host and port are correct
- Ensure firewall allows connections

### "Permission denied" errors
- Ensure you have write permissions to the backup directory
- Check database user has required privileges

## Next Steps

1. ✅ **You've completed the Quick Start!**
2. 📖 Read the [USAGE_GUIDE.md](USAGE_GUIDE.md) for advanced features
3. 🏗️ Learn about the architecture in [ARCHITECTURE.md](ARCHITECTURE.md)
4. ⚙️ Configure automatic backups with scheduling
5. ☁️ Set up cloud storage integration

## Support

- 📝 [Open an Issue](https://github.com/spaceadh/database-backup-utility/issues)
- 📚 [Read the Docs](README.md)
- 💬 [Discussions](https://github.com/spaceadh/database-backup-utility/discussions)

---

**Happy Backing Up! 🚀**
