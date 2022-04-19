#!/bin/bash

#############################################
# A script to run the API in a child terminal
#############################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Build the API code if needed
#
./gradlew clean && ./gradlew bootJar
if [ $? -ne 0 ]; then
    echo 'Problem encountered building the API'
    exit
fi

#
# Run the API's JAR file'
#
java -jar target/sampleapi-0.0.1-SNAPSHOT.jar
if [ $? -ne 0 ]; then
    echo 'Problem encountered running the API'
    exit
fi
