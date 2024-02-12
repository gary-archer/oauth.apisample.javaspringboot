#!/bin/bash

########################################################
# A script to run Wiremock in Docker in a child terminal
########################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Run Wiremock over HTTPS using Docker
#
docker run -it --rm \
  --name wiremock \
  -p 447:447 \
  -v $(pwd)/certs/authsamples-dev.ssl.p12:/certs/authsamples-dev.ssl.p12 \
  wiremock/wiremock:3.3.1 \
  --root-dir test/integration \
  --https-port 447 \
  --disable-http \
  --https-keystore '/certs/authsamples-dev.ssl.p12' \
  --keystore-type 'pkcs12' \
  --keystore-password 'Password1' \
  --key-manager-password 'Password1'

#
# Prevent automatic terminal closure
#
read -n 1
