#!/bin/bash -e
basepath=$(cd `dirname $0`; pwd)
cd "${basepath}/twitkit-fridge-src"
mvn clean
mvn package #-Dmaven.test.skip=true
