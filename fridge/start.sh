#!/bin/bash
basepath=$(cd `dirname $0`; pwd)
java -jar "${basepath}/twitkit-fridge-src/target/twitkit-fridge-0.0.1-SNAPSHOT.jar"
