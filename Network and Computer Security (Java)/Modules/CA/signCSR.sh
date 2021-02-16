#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Common Name Missing!"
    exit 1
fi
# CA Signs a CSR
openssl x509 -req -days 365 -in $1.csr -CA CA.crt -CAkey CA.key -out $1.crt -extensions req_ext -extfile $1.cnf

