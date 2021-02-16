#!/bin/sh
# Generates a New Private Key
openssl genrsa -out CA_I.key 4096
# Converts to gRPC Format
openssl pkcs8 -topk8 -in CA_I.key -nocrypt -out CA.key
# Generates a New Self Signing Certificate
openssl req -new -x509 -key CA.key -sha256 -subj "/C=PT/ST=LX/O=MTR" -days 365 -out CA.crt
echo 01 > CA.srl
# Cleanup
rm CA_I.key

