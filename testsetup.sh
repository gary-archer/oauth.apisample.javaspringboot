#!/bin/bash

#############################################################
# A script to build and run the API with a test configuration
#############################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Copy down the test configuration, to point the API to a mock authorization server
#
cp deployment/environments/test/api.config.json ./api.config.json

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

#
# Indicate success
#
echo "Start tests via './gradlew test' or './gradlew loadtest' ..."
