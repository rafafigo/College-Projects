PrivPath=$1
PubPath=$2

# Cleanup
rm "${PrivPath}.der" "${PubPath}.der"
# Generates RSA Key Pair (PEM)
openssl genrsa 4096 >"${PrivPath}.pem"
openssl rsa -in "${PrivPath}.pem" -pubout >"${PubPath}.pem"
# Converts Keys to Java Format (DER)
openssl pkcs8 -topk8 -inform PEM -outform DER -in "${PrivPath}.pem" -nocrypt >"${PrivPath}.der"
openssl rsa -pubin -inform PEM -outform DER -in "${PubPath}.pem" >"${PubPath}.der"
# Cleanup
rm "${PrivPath}.pem" "${PubPath}.pem"
