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

  DOCKER_IMAGE_NAME="$DOCKERHUB_ACCOUNT/finaljavaapi:1.0"
else

  DOCKER_IMAGE_NAME='finaljavaapi:1.0'
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
# Build the Docker container
#
docker build --no-cache -f deployment/kubernetes/Dockerfile --build-arg -t "$DOCKER_IMAGE_NAME" .
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
