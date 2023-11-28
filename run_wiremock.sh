#!/bin/bash

##############################################
# A script to run Wiremock in a child terminal
##############################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Download the Wiremock JAR file
#
echo 'Downloading Wiremock standalone server ...'
WIREMOCK_JAR_FILE='wiremock-jre8-standalone-2.33.1.jar'
WIREMOCK_DOWNLOAD_URL="https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/2.33.1/$WIREMOCK_JAR_FILE"
rm $WIREMOCK_JAR_FILE 2>/dev/null
curl -O -s $WIREMOCK_DOWNLOAD_URL

#
# Run Wiremock over HTTPS in this terminal
# On Linux ensure that you have first granted Java the permissions to listen on port 447:
# - sudo setcap 'cap_net_bind_service=+ep' /usr/lib/jvm/zulu-21-amd64/bin/java
#
java -jar $WIREMOCK_JAR_FILE \
--https-port 447 \
--disable-http \
--https-keystore './certs/authsamples-dev.ssl.p12' \
--keystore-type 'pkcs12' \
--keystore-password 'Password1' \
--key-manager-password 'Password1'

#
# Prevent automatic terminal closure
#
read -n 1
