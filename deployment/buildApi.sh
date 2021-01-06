#!/bin/bash

#
# A script to build our API into a docker image
#

#
# Point to the minikube api profile
#
minikube profile api
eval $(minikube docker-env --profile api)

#
# Build the Java JAR file
#
echo "Building Java Code ..."
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-13.jdk/Contents/Home;
mvn clean install -f ../pom.xml
if [ $? -ne 0 ]
then
  echo "*** Java build error ***"
  exit 1
fi

#
# Build the docker image, with the JAR file and other resources
#
echo "Building Java Docker Image ..."
cd ..
docker build --no-cache -f deployment/Dockerfile -t javaapi:v1 .
if [ $? -ne 0 ]
then
  echo "*** Docker build error ***"
  exit 1
fi

#
# Indicate success
#
cd deployment
echo "Build completed successfully"
