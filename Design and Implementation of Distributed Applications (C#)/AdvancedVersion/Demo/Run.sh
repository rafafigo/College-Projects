if [ "$#" -ne 1 ]; then
  echo "Usage: Takes Client Number As Argument" >&2
  exit 1
fi

# Run
../GSTORE/Server/bin/Debug/netcoreapp3.1/Server.exe < Servers/1/Args.txt & S1PID=$!
../GSTORE/Server/bin/Debug/netcoreapp3.1/Server.exe < Servers/2/Args.txt & S2PID=$!
../GSTORE/Server/bin/Debug/netcoreapp3.1/Server.exe < Servers/3/Args.txt & S3PID=$!
../GSTORE/Client/bin/Debug/netcoreapp3.1/Client.exe < Clients/$1/Args.txt

# Cleanup
kill -9 $S1PID
kill -9 $S2PID
kill -9 $S3PID
