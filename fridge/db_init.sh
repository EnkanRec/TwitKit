#!/bin/bash -e
BASEPATH=$(cd `dirname $0`; pwd)
if [ -x "/usr/bin/pwgen" ]; then
    PASS=`pwgen 16`
else
    PASS=`uuidgen`
fi
if [ -n "$1" ]; then
    DB="$1"
else
    DB="$USER"
fi

sudo mysql << MYSQL_SCRIPT
CREATE DATABASE $DB;
CREATE USER '$DB'@'localhost' IDENTIFIED BY '$PASS';
GRANT ALL PRIVILEGES ON $DB.* TO '$DB'@'localhost';
FLUSH PRIVILEGES;
USE $DB;
SOURCE $BASEPATH/schema/db_init.sql;
MYSQL_SCRIPT

echo "# 数据源
spring.datasource.yui.jdbc-url=jdbc:mysql://localhost:3306/$DB?characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
spring.datasource.yui.username=$DB
spring.datasource.yui.password=$PASS"