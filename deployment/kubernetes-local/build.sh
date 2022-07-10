#!/bin/bash

##########################################
# Build the API's code into a Docker image
##########################################

#
# Ensure that we are in the root folder
#
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ..

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
# Ensure that we start clean
#
rm -rf finalapi

#
# Build the Java API
#
git clone https://github.com/gary-archer/oauth.apisample.javaspringboot finalapi
  if [ $? -ne 0 ]; then
    echo '*** Java API download problem encountered'
    exit 1
  fi

  cd finalapi
  ./gradlew bootJar
  if [ $? -ne 0 ]; then
    echo '*** Java API build problem encountered'
    exit 1
  fi

#
# Initialize extra trusted certificates to zero
#
touch docker/trusted.ca.pem

#
# On Windows, fix problems with trailing newline characters in Docker scripts
#
if [ "$PLATFORM" == 'WINDOWS' ]; then
  sed -i 's/\r$//' docker/docker-init.sh
fi

#
# Build the Docker container
#
docker build --no-cache -f docker/Dockerfile --build-arg TRUSTED_CA_CERTS='docker/trusted.ca.pem' -t finaljavaapi:v1 .
if [ $? -ne 0 ]; then
  echo '*** API docker build problem encountered'
  exit 1
fi

#
# Load it into kind's Docker registry
#
kind load docker-image finalnodejsapi:v1 --name oauth
if [ $? -ne 0 ]; then
  echo '*** API docker deploy problem encountered'
  exit 1
fi
