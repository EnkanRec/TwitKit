#!/bin/bash
basepath=$(cd `dirname $0`; pwd)
java -jar "${basepath}/fridge-src/target/twitkit-fridge-0.0.1.jar"
