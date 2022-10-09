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
# Manage differences between local and cloud deployment
#
if [ "$CLUSTER_TYPE" != 'local' ]; then
  
  if [ "$DOCKERHUB_ACCOUNT" == '' ]; then
    echo '*** The DOCKERHUB_ACCOUNT environment variable has not been configured'
    exit 1
  fi

  DOCKER_IMAGE_NAME="$DOCKERHUB_ACCOUNT/finaljavaapi:v1"
else

  DOCKER_IMAGE_NAME='finaljavaapi:v1'
fi

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
cp ../certs/cluster.internal.ca.pem deployment/shared/trusted.ca.pem

#
# Build the Docker container
#
docker build --no-cache -f deployment/shared/Dockerfile --build-arg TRUSTED_CA_CERTS='deployment/shared/trusted.ca.pem' -t "$DOCKER_IMAGE_NAME" .
if [ $? -ne 0 ]; then
  echo '*** API docker build problem encountered'
  exit 1
fi

#
# Push the API docker image
#
if [ "$CLUSTER_TYPE" == 'local' ]; then
  kind load docker-image "$DOCKER_IMAGE_NAME" --name oauth
else
  docker image push "$DOCKER_IMAGE_NAME"
fi
if [ $? -ne 0 ]; then
  echo '*** API docker push problem encountered'
  exit 1
fi
