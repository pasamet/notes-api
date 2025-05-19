#!/bin/sh

certsDirectory="src/main/resources/certs"
mkdir -p "$certsDirectory"


# create rsa key pair
openssl genrsa -out "$certsDirectory"/keypair.pem 2048

# extract public key
openssl rsa -in "$certsDirectory"/keypair.pem -pubout -out "$certsDirectory"/public.pem

# create private key in PKCS#8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$certsDirectory"/keypair.pem -out "$certsDirectory"/private.pem

# remove unused keypair
rm "$certsDirectory"/keypair.pem
