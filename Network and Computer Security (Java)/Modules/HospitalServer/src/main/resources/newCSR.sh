#!/bin/sh
# Generates a New Private Key
openssl genrsa -out HS_I.key 4096
# Converts to gRPC Format
openssl pkcs8 -topk8 -in HS_I.key -nocrypt -out HS.key
# Generates a New CSR
openssl req -new -key HS.key -out HS.csr -config HS.cnf
# Converts Private Key in Java Format
openssl pkcs8 -topk8 -inform PEM -outform DER -in HS.key -nocrypt > HS.pkcs8
# Cleanup
rm HS_I.key
