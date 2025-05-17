#!/bin/bash

#############################################
# A script to run the API in a child terminal
#############################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Build the API code if needed
#
./gradlew clean && ./gradlew bootJar --warning-mode all
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the API'
  read -n 1
  exit 1
fi

#
# Ensure that the logs folder exists
#
if [ ! -d './logs' ]; then
  mkdir './logs'
fi

#
# Run the API's JAR file
# If required, first grant permissions to listen on a privileged port:
# - sudo setcap 'cap_net_bind_service=+ep' /usr/lib/jvm/zulu-24-amd64/bin/java
#
java -jar build/libs/finalapi-0.0.1-SNAPSHOT.jar
if [ $? -ne 0 ]; then
  echo 'Problem encountered running the API'
  read -n 1
  exit 1
fi

#
# Prevent automatic terminal closure
read -n 1
