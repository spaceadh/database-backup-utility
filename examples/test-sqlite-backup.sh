#!/bin/bash

# Test SQLite backup functionality

echo "==================================================================="
echo "Testing SQLite Backup Functionality"
echo "==================================================================="
echo ""

# Create test database
echo "Creating test SQLite database..."
mkdir -p /tmp/test-db
echo "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT); INSERT INTO users (name) VALUES ('John Doe'), ('Jane Smith');" | sqlite3 /tmp/test-db/test.db

echo "Test database created at: /tmp/test-db/test.db"
echo ""

# Create backup directory
mkdir -p ./test-backups

echo "Starting backup with Spring Boot CLI..."
echo ""
echo "Command: backup-sqlite --database-file /tmp/test-db/test.db --backup-path ./test-backups --compress true"
echo ""
echo "To run this test:"
echo "1. Start the application: java -jar target/database-backup-utility-1.0.0.jar"
echo "2. Run command: backup-sqlite --database-file /tmp/test-db/test.db --backup-path ./test-backups --compress true"
echo "3. Check ./test-backups for the backup file"
echo ""

ls -lh /tmp/test-db/test.db
