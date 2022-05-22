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
# Build the API if needed
#
./gradlew clean && ./gradlew bootJar
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the API'
  exit
fi

#
# Run the API
# On Linux first ensure that you have first granted Node.js permissions to listen on port 445:
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
