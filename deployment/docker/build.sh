#!/bin/bash

##############################################################
# A script to test Docker deployment on a development computer
##############################################################

cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../..

#
# Create SSL certificates if required
#
./certs/create.sh
if [ $? -ne 0 ]; then
  exit
fi

#
# Build the code
#
./gradlew bootJar
if [ $? -ne 0 ]; then
  echo 'Java API build problem encountered'
  exit 1
fi

#
# Build the docker image
#
docker build -t finaljavaapi:latest .
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the API docker image'
  exit
fi
