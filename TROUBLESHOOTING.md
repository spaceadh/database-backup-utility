# Troubleshooting Guide

## PostgreSQL Backup: "Cannot run program 'pg_dump'"

### Problem
The application cannot find the `pg_dump` executable because it's not in your system PATH.

### Solution Options

#### Option 1: Add PostgreSQL to System PATH (Recommended)

1. **Find your PostgreSQL installation directory**
   - Typical location: `C:\Program Files\PostgreSQL\<version>\bin`
   - For example: `C:\Program Files\PostgreSQL\16\bin`

2. **Add to System PATH**
   - Open "Environment Variables" (Windows Key + Search for "environment")
   - Under "System variables", find and select "Path"
   - Click "Edit"
   - Click "New"
   - Add the PostgreSQL bin directory (e.g., `C:\Program Files\PostgreSQL\16\bin`)
   - Click "OK" to save

3. **Verify Installation**
   ```powershell
   pg_dump --version
   ```

4. **Restart your terminal/application** for changes to take effect

#### Option 2: Configure Executable Path in Application

You can specify the full path to database executables in `application.yml`:

```yaml
backup:
  executables:
    pg-dump: "C:/Program Files/PostgreSQL/16/bin/pg_dump.exe"
    mysqldump: "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe"
    mongodump: "C:/Program Files/MongoDB/Server/7.0/bin/mongodump.exe"
```

**Important Notes:**
- Use forward slashes (`/`) or double backslashes (`\\`) in paths
- Enclose paths with spaces in quotes
- Restart the application after modifying `application.yml`

#### Option 3: Use Docker PostgreSQL Client

If you have Docker installed, you can use the PostgreSQL client from a container:

```powershell
docker run --rm postgres:16 pg_dump --version
```

### Rebuilding the Application

After making changes to `application.yml`, rebuild:

```powershell
mvn clean package
```

Then run:

```powershell
java -jar target/database-backup-utility-1.0.0.jar
```

## MySQL Backup: "Cannot run program 'mysqldump'"

Same solutions apply - replace `pg_dump` with `mysqldump` and adjust paths:
- Typical location: `C:\Program Files\MySQL\MySQL Server 8.0\bin`

## MongoDB Backup: "Cannot run program 'mongodump'"

Same solutions apply - replace with `mongodump`:
- Typical location: `C:\Program Files\MongoDB\Server\<version>\bin`
- Or MongoDB Tools location: `C:\Program Files\MongoDB\Tools\<version>\bin`

## SQLite Backup

SQLite backups use file copying and don't require external tools. Just ensure the database file path is correct.

## General Tips

1. **Check if tool is installed**
   ```powershell
   where pg_dump
   where mysqldump
   where mongodump
   ```

2. **Test database connectivity**
   ```powershell
   # PostgreSQL
   psql -h hostname -p 5432 -U username -d database
   
   # MySQL
   mysql -h hostname -P 3306 -u username -p
   
   # MongoDB
   mongosh "mongodb://username:password@hostname:27017/database"
   ```

3. **View application logs**
   - Check `logs/backup-utility.log` for detailed error messages
   - Check `backup_log.txt` for backup operation history

4. **Verify permissions**
   - Ensure the application has write permissions to the backup directory
   - Ensure database user has appropriate backup privileges

## Need More Help?

If you continue experiencing issues:
1. Check the logs in `logs/backup-utility.log`
2. Verify your database connection details
3. Ensure the database tools are properly installed
4. Test the backup command manually in PowerShell
