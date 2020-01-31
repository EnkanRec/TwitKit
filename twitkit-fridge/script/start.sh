basepath=$(cd `dirname $0`; pwd)
cd "${basepath}/.."
nohup java -jar "${basepath}/../twitkit-fridge-src/target/twitkit-fridge-0.0.1-SNAPSHOT.jar" > std.output.log 2>&1& echo $! > "${basepath}/../run.pid"
