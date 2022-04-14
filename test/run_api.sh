#!/bin/bash

#############################################
# A script to run the API in a child terminal
#############################################

cd "$(dirname "${BASH_SOURCE[0]}")"
cd ..

#
# Build then run the API
#
mvn clean package -DskipTests
java -jar target/sampleapi-0.0.1-SNAPSHOT.jar
