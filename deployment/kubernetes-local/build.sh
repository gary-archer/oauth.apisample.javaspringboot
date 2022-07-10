#!/bin/bash

##########################################
# Build the API's code into a Docker image
##########################################

#
# Ensure that we are in the root folder
#
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../..

#
# Get the platform
#
case "$(uname -s)" in

  Darwin)
    PLATFORM="MACOS"
 	;;

  MINGW64*)
    PLATFORM="WINDOWS"
	;;

  Linux)
    PLATFORM="LINUX"
	;;
esac

#
# Build the Java API
#
./gradlew bootJar
if [ $? -ne 0 ]; then
  echo '*** Java API build problem encountered'
  exit 1
fi

#
# Initialize extra trusted certificates to zero
#
touch deployment/kubernetes-local/trusted.ca.pem

#
# On Windows, fix problems with trailing newline characters in Docker scripts
#
if [ "$PLATFORM" == 'WINDOWS' ]; then
  sed -i 's/\r$//' deployment/docker/docker-init.sh
fi

#
# Build the Docker container
#
docker build --no-cache -f deployment/docker/Dockerfile --build-arg TRUSTED_CA_CERTS='deployment/kubernetes-local/trusted.ca.pem' -t finaljavaapi:v1 .
if [ $? -ne 0 ]; then
  echo '*** API docker build problem encountered'
  exit 1
fi

#
# Load it into kind's Docker registry
#
kind load docker-image finaljavaapi:v1 --name oauth
if [ $? -ne 0 ]; then
  echo '*** API docker deploy problem encountered'
  exit 1
fi
