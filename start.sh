#!/bin/bash

###########################################################################
# A script to download SSL certificates, then build and run the API locally
###########################################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Download SSL certificates from a central repo if needed
#
rm -rf ./resources
git clone https://github.com/gary-archer/oauth.developmentcertificates ./resources
if [ $? -ne 0 ]; then
    echo 'Problem encountered downloading development certificates'
    exit
fi

#
# Move authsamples-dev certificates to this folder
#
rm -rf certs
mv ./resources/authsamples-dev ./certs
rm -rf ./resources

#
# Build the app
#
mvn clean package
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