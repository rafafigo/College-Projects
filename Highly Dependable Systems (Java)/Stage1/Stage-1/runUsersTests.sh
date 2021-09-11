# Launching Server
gnome-terminal -- bash -c "
cd Server;
sh runServer.sh 2 2>/dev/null;
"

# Running Users Tests
gnome-terminal -- bash -c "
cd User;
read -p \"Press Enter to Run Users Tests!\";
mvn clean test 2>/dev/null;
read -p \"Press Enter to Exit!\";
"
