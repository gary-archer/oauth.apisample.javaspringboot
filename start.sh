#!/bin/bash

###########################################################################
# A script to download SSL certificates, then build and run the API locally
###########################################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Restore the development configuration once the API is loaded
#
cp deployment/environments/dev/api.config.json ./api.config.json

#
# Create SSL certificates if required and then configure Java to trust the root CA:
# - sudo $JAVA_HOME/bin/keytool -import -alias authsamples-dev -cacerts -file ./certs/authsamples-dev.ca.crt -storepass changeit -noprompt
#
./certs/create.sh
if [ $? -ne 0 ]; then
  exit 1
fi

#
# Run the previously built API
#
./run_api.sh
