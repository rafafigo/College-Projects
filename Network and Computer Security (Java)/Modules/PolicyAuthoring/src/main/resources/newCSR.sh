#!/bin/sh
# Generates a New Private Key
openssl genrsa -out PA_I.key 4096
# Converts to gRPC Format
openssl pkcs8 -topk8 -in PA_I.key -nocrypt -out PA.key
# Generates a New CSR
openssl req -new -key PA.key -out PA.csr -config PA.cnf
# Cleanup
rm PA_I.key
