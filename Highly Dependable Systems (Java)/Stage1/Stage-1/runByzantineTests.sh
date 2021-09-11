# Launching Server
gnome-terminal -- bash -c "
cd Server;
sh runServer.sh 2 2>/dev/null;
"

# Running Byzantine Tests
gnome-terminal -- bash -c "
cd Byzantine;
read -p \"Press Enter to Run Byzantine Tests!\";
mvn clean test 2>/dev/null;
read -p \"Press Enter to Exit!\";
"
