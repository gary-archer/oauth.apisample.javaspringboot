#!/bin/bash

#
# A MacOS script to build the Java API and deploy it to a minikube local PC Kubernetes cluster
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
# Use the minikube docker daemon rather than that of Docker Desktop for Mac
#
echo "Preparing Kubernetes ..."
eval $(minikube docker-env)

#
# Clean up any resources for the previously deployed version of the API
#
kubectl delete deploy/javaapi   2>/dev/null
kubectl delete svc/javaapi-svc  2>/dev/null
docker image rm -f javaapi      2>/dev/null

#
# Build the docker image, with the JAR file, configuration file and SSL certificate
#
echo "Building Docker Image from JAR file ..."
cd ..
docker build -f deployment/Dockerfile -t javaapi .
if [ $? -ne 0 ]
then
  echo "*** Docker build error ***"
  exit 1
fi

#
# Deploy the local docker image to multiple Kubernetes pods
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

#
# View logs from a POD like this if needed, in order to troubleshoot development errors
#
#kubectl logs --tail=100 pod/javaapi-74f57df659-2tjz5

#
# Connect to a POD like this if needed, to verify that deployed files are correct
#
#kubectl exec --stdin --tty pod/javaapi-74f57df659-2tjz5 -- /bin/sh
#ls -lr /usr/sampleapi

#
# Get the load balanced Kubernetes URL like this and try to call the service
#
# echo $(minikube service --url javaapi-svc)/api/companies
