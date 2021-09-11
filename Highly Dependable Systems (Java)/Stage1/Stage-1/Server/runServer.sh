NByzantine=$1

# Cleaning & Starting DB
cd src/main/resources || exit
sh newDB.sh

cd - || exit
mvn exec:java -DmyNByzantineUsers="${NByzantine}" 2>/dev/null
