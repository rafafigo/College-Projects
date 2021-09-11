# Launching Server
gnome-terminal -- bash -c "
cd Server;
sh runServer.sh 0 2>/dev/null;
"

# Launching Users
gnome-terminal -- bash -c "
cd User;
read -p \"Press Enter to Launch Users!\";
bash runUsers.bash 10 0 5 60 1000 2>/dev/null;
"

# Running HA Tests
gnome-terminal -- bash -c "
cd HA;
read -p \"Press Enter to Run HA Tests!\";
mvn clean test 2>/dev/null;
read -p \"Press Enter to Exit!\";
"
