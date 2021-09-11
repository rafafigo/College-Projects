cd Server/src/main/resources || exit
sh newDB.sh
cd ../../../ || exit
echo "Starting Server (...)"
mvn exec:java -DmyNByzantineUsers=0 >/dev/null 2>&1 &
PID=$!
cd ../User || exit
sleep 5
echo "Starting User (...)"
echo 3 | mvn exec:java -DmyNByzantineUsers=0
echo "Killing Server (...)"
kill "${PID}"
cd ../Server || exit
echo "Starting Server Again (...)"
mvn exec:java -DmyNByzantineUsers=0 >/dev/null 2>&1 &
PID=$!
cd ../User || exit
sleep 5
echo "Starting User Again (...)"
echo 3 | mvn exec:java -DmyNByzantineUsers=0
kill "${PID}"
