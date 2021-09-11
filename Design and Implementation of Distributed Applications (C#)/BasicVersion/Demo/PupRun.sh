# Run
../GSTORE/PCS/bin/Debug/netcoreapp3.1/PCS.exe < PCS/1/Args.txt & PCSPID=$!
../GSTORE/PuppetMaster/bin/Debug/netcoreapp3.1/PuppetMaster.exe

# Cleanup
kill -9 $PCSPID
