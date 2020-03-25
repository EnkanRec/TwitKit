basepath=$(cd `dirname $0`; pwd)
git checkout dev
git pull
cd "${basepath}/../twitkit-fridge-src"
mvn clean
mvn package #-Dmaven.test.skip=true
