#!/bin/bash

#############################################
# A script to run the API in a child terminal
#############################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Download development SSL certificates if required
# Then configure Java to trust the root CA at certs/authsamples-dev.ssl.p12
# - sudo $JAVA_HOME/bin/keytool -import -alias authsamples-dev -cacerts -file ./certs/authsamples-dev.ca.pem -storepass changeit -noprompt
#
./downloadcerts.sh
if [ $? -ne 0 ]; then
  read -n 1
  exit 1
fi

#
# Build the API code if needed
#
./gradlew clean && ./gradlew bootJar
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
# Run the API's JAR file'
#
java -jar build/libs/sampleapi-0.0.1-SNAPSHOT.jar
if [ $? -ne 0 ]; then
  echo 'Problem encountered running the API'
  read -n 1
  exit 1
fi

#
# Prevent automatic terminal closure
read -n 1
