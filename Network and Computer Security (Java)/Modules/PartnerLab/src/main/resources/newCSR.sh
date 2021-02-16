#!/bin/sh
# Generates a New Private Key
openssl genrsa -out PL_I.key 4096
# Converts to gRPC Format
openssl pkcs8 -topk8 -in PL_I.key -nocrypt -out PL.key
# Generates a New CSR
openssl req -new -key PL.key -out PL.csr -config PL.cnf
# Converts Private Key in Java Format
openssl pkcs8 -topk8 -inform PEM -outform DER -in PL.key -nocrypt > PL.pkcs8
# Cleanup
rm PL_I.key
