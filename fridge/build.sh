#!/bin/bash -e
basepath=$(cd `dirname $0`; pwd)
cd "${basepath}/fridge-src"
mvn clean
mvn package #-Dmaven.test.skip=true
