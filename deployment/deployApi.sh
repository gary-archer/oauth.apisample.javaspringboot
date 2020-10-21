#!/bin/bash

#
# A MacOS script to deploy 2 instances of the Java API to a local PC Minikube Kubernetes cluster
#

#
# Build the Java JAR file
#
echo "Building Java Code ..."
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-13.0.1.jdk/Contents/Home;
mvn clean install -f ../pom.xml
if [ $? -ne 0 ]
then
  echo "*** Java build error ***"
  exit 1
fi

#
# Clean up any resources for the previously deployed version of the API
#
kubectl delete deploy/javaapi   2>/dev/null
kubectl delete svc/javaapi-svc  2>/dev/null
docker image rm -f javaapi      2>/dev/null

#
# Build the docker image, with the JAR file and other resources
#
echo "Building Java Docker Image ..."
cd ..
docker build -f deployment/Dockerfile -t javaapi .
if [ $? -ne 0 ]
then
  echo "*** Docker build error ***"
  exit 1
fi

#
# Deploy 2 instances of the local docker image to 2 Kubernetes pods
#
echo "Deploying Docker Image to Kubernetes ..."
cd deployment
kubectl create -f Kubernetes.yaml
if [ $? -ne 0 ]
then
  echo "*** Kubernetes deployment error ***"
  exit 1
fi

#
# Output the names of created PODs and indicate success
#
echo "Deployment completed successfully"
kubectl get pod -l app=javaapi
API_URL=$(minikube service --url javaapi-svc)/api/companies
echo $API_URL

#
# Expose the API to clients outside Kubernetes on port 443 with a custom host name
# We can then access the API at https://netcoreapi.mycompany.com/api/companies
#
kubectl apply -f ingress.yaml
echo "Deployment completed successfully"
