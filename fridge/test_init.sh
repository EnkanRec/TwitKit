#!/bin/bash -e
if [ -z "$1" ]; then 
    BASEPATH=$(cd `dirname $0`; pwd)
    sudo mysql << MYSQL_SCRIPT
DROP DATABASE IF EXISTS fridge_test;
DROP USER IF EXISTS 'fridge_test'@'localhost';
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

elif [ "$1" = "--remove" ]; then
    BASEPATH=$(cd `dirname $0`; pwd)
    sudo mysql << MYSQL_SCRIPT
DROP DATABASE IF EXISTS fridge_test;
DROP USER IF EXISTS 'fridge_test'@'localhost';
MYSQL_SCRIPT
    echo "Test database removed."
    
else
    echo "Wrong argument."
    exit 1
fi
