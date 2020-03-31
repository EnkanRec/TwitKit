#!/bin/bash -e
BASEPATH=$(cd `dirname $0`; pwd)
sudo mysql << MYSQL_SCRIPT
CREATE DATABASE fridge_test;
CREATE USER 'fridge_test'@'localhost' IDENTIFIED BY 'fridge_test';
GRANT ALL PRIVILEGES ON fridge_test.* TO 'fridge_test'@'localhost';
FLUSH PRIVILEGES;
USE fridge_test;
SOURCE $BASEPATH/schema/test_init.sql;
MYSQL_SCRIPT

echo "Test database created."
echo "Database:   fridge_test"
echo "Username:   fridge_test"
echo "Password:   fridge_test"
