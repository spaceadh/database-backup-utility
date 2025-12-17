#!/bin/bash

# Database Backup Utility - Usage Examples

echo "==================================================================="
echo "Database Backup Utility - Command Examples"
echo "==================================================================="
echo ""

echo "1. Backup MySQL Database:"
echo "   backup-mysql --host localhost --port 3306 --database mydb --username root --password secret"
echo ""

echo "2. Backup PostgreSQL Database:"
echo "   backup-postgresql --host localhost --port 5432 --database mydb --username postgres --password secret"
echo ""

echo "3. Backup MongoDB Database:"
echo "   backup-mongodb --host localhost --port 27017 --database mydb --username admin --password secret"
echo ""

echo "4. Backup SQLite Database:"
echo "   backup-sqlite --database-file /path/to/database.db"
echo ""

echo "5. Backup to AWS S3:"
echo "   backup-to-s3 --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --bucket my-backup-bucket --region us-east-1 --access-key YOUR_ACCESS_KEY --secret-key YOUR_SECRET_KEY"
echo ""

echo "6. Schedule Daily Backup at 2 AM:"
echo "   schedule-backup --db-type MYSQL --host localhost --port 3306 --database mydb --username root --password secret --cron \"0 0 2 * * ?\""
echo ""

echo "7. Cancel Scheduled Backup:"
echo "   cancel-schedule --database mydb"
echo ""

echo "8. List All Scheduled Backups:"
echo "   list-schedules"
echo ""

echo "==================================================================="
echo "Common Cron Expressions:"
echo "==================================================================="
echo "  Daily at 2 AM:             0 0 2 * * ?"
echo "  Every 6 hours:             0 0 */6 * * ?"
echo "  Weekdays at noon:          0 0 12 * * MON-FRI"
echo "  First of month at midnight: 0 0 0 1 * ?"
echo "  Every Sunday at 3 AM:      0 0 3 ? * SUN"
echo "==================================================================="
