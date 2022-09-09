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
# Check preconditions
#
if [ "$DOCKERHUB_ACCOUNT" == '' ]; then
  echo '*** The DOCKERHUB_ACCOUNT environment variable has not been configured'
  exit 1
fi

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
# Copy in the internal cluster root CA from the parent project, to be trusted within the container
#
cp ../certs/mycluster.ca.pem deployment/shared/trusted.ca.pem

#
# On Windows, fix problems with trailing newline characters in Docker scripts
#
if [ "$PLATFORM" == 'WINDOWS' ]; then
  sed -i 's/\r$//' deployment/shared/docker-init.sh
fi

#
# Build the Docker container
#
docker build --no-cache -f deployment/shared/Dockerfile --build-arg TRUSTED_CA_CERTS='deployment/shared/trusted.ca.pem' -t "$DOCKERHUB_ACCOUNT/finaljavaapi:v1" .
if [ $? -ne 0 ]; then
  echo '*** API docker build problem encountered'
  exit 1
fi

#
# Produce the final YAML using the envsubst tool
#
export API_DOMAIN_NAME='api.mycluster.com'
export API_DOCKER_IMAGE="$DOCKERHUB_ACCOUNT/finaljavaapi:v1"
envsubst < '../shared/api.yaml.template' > '../shared/api.yaml'
if [ $? -ne 0 ]; then
  echo '*** Problem encountered running envsubst to produce the final Kubernetes api.yaml file'
  exit 1
fi

#
# Push it to DockerHub
#
docker image rm -f "$DOCKERHUB_ACCOUNT/finaljavaapi:v1" 2>/dev/null
docker image push "$DOCKERHUB_ACCOUNT/finaljavaapi:v1"
if [ $? -ne 0 ]; then
  echo '*** API docker deploy problem encountered'
  exit 1
fi
