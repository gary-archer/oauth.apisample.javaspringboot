#!/bin/bash

################################################################
# A script to build the API's code, then run it locally over SSL
################################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Download development SSL certificates if required
#
./downloadcerts.sh
if [ $? -ne 0 ]; then
  exit
fi

#
# Restore the development configuration once the API is loaded
#
cp deployment/environments/dev/api.config.json ./api.config.json

#
# Build the API if needed
#
./gradlew clean && ./gradlew bootJar
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the API'
  exit
fi

#
# Ensure that log folders exist
#
if [ ! -d '../oauth.logs' ]; then
  mkdir '../oauth.logs'
fi
if [ ! -d '../oauth.logs/api' ]; then
  mkdir '../oauth.logs/api'
fi

#
# Run the API
# On Linux first ensure that you have first granted Java permissions to listen on port 446:
# - sudo setcap 'cap_net_bind_service=+ep' /usr/lib/jvm/zulu-17-amd64/bin/java
#
java -jar build/libs/sampleapi-0.0.1-SNAPSHOT.jar
if [ $? -ne 0 ]; then
  echo 'Problem encountered running the API'
  exit
fi

#
# Prevent automatic terminal closure on Linux
#
if [ "$(uname -s)" == 'Linux' ]; then
  read -n 1
fi
