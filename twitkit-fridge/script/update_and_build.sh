basepath=$(cd `dirname $0`; pwd)
git pull -b dev
cd "${basepath}/../twitkit-fridge-src"
mvn clean
mvn package
