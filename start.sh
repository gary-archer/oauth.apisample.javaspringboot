#!/bin/bash

#
# Download SSL certificates from a central repo if needed
#
if [ ! -d '.certs' ]; then
    git clone https://github.com/gary-archer/oauth.developmentcertificates ./.certs
fi

#
# Build the app
#
mvn clean install

#
# Then start listening
#
java -jar target/sampleapi-0.0.1-SNAPSHOT.jar