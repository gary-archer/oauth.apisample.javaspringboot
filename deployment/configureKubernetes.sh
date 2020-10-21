#!/bin/bash

#
# A MacOS script to prepare Kubernetes before deploying APIs
#

#
# Use the Minikube Docker Daemon rather than that of Docker Desktop for Mac
#
echo "Preparing Kubernetes ..."
eval $(minikube docker-env)

#
# Tell Kubernetes to trust our root certificate
#

#
# Deploy our SSL wildcard certificate to the Kubernetes cluster
#
kubectl delete secret mycompany-com-tls     2>/dev/null
kubectl create secret tls mycompany-com-tls --cert=../certs/mycompany.ssl.crt --key=../certs/mycompany.ssl.key

#
# Enable load balancing
#
minikube addons enable ingress
echo "Kubernetes configuration completed successfully"
