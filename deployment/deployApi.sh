#!/bin/bash

#
# A script to deploy our API docker image to the Kubernetes cluster
#

#
# Point to the minikube api profile
#
minikube profile api
eval $(minikube docker-env --profile api)

#
# Clean up any resources for the previously deployed version of the API
#
echo "Preparing Kubernetes ..."
kubectl delete deploy/javaapi
kubectl delete service/javaapi-svc

#
# Deploy 2 instances of the local docker image to 2 Kubernetes pods
#
echo "Deploying Docker Image to Kubernetes ..."
kubectl apply -f Kubernetes.yaml
if [ $? -ne 0 ]
then
  echo "*** Kubernetes deployment error ***"
  exit 1
fi

#
# Expose the API to clients outside Kubernetes on port 443 with a custom host name
# We can then access the API at https://javaapi.mycompany.com/api/companies
#
kubectl apply -f Ingress.yaml
echo "Deployment completed successfully"
