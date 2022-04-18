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
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo 'Problem encountered building the API'
    exit
fi

#
# Then start listening
#
java -jar target/sampleapi-0.0.1-SNAPSHOT.jar
if [ $? -ne 0 ]; then
    echo 'Problem encountered running the API'
    exit
fi