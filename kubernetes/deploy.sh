#!/bin/bash

#
# Before running this script, deploy base certificate infrastructure to the cluster:
# - git clone https://github.com/gary-archer/oauth.developmentcertificates
# - cd kubernetes
# - ./deploy.sh

#
# Use the Minikube Docker Daemon rather than that of Docker Desktop for Mac
#
minikube profile api
eval $(minikube docker-env)

#
# Build the API's code
#
cd ..
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-13.jdk/Contents/Home;
mvn clean install -f pom.xml
if [ $? -ne 0 ]
then
  exit 1
fi

#
# Build the API's docker image
#
docker build --no-cache -f kubernetes/Dockerfile -t demoapi:v1 .
if [ $? -ne 0 ]
then
  exit 1
fi

#
# Issue an internal SSL certificate for the API and a secret for its private key password
# Files issued are then present in the data output of this command:
# - kubectl get certificate demoapi-svc-internal-cert -o yaml
#
kubectl delete secret demoapi-pkcs12-password 2>/dev/null
kubectl create secret generic demoapi-pkcs12-password --from-literal=password='Password1'
kubectl apply -f kubernetes/internal-cert.yaml
if [ $? -ne 0 ]
then
  exit 1
fi

#
# Deploy the API to the cluster
#
kubectl delete deploy/demoapi       2>/dev/null
kubectl delete service/demoapi-svc  2>/dev/null
kubectl apply -f kubernetes/service.yaml
if [ $? -ne 0 ]
then
  exit 1
fi

#
# Expose the API on the host developer PC
#
kubectl apply -f kubernetes/ingress.yaml
if [ $? -ne 0 ]
then
  exit 1
fi
