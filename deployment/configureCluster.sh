#!/bin/bash

#
# A script to prepare the overall Kubernetes cluster
#

#
# Use the Minikube Docker Daemon rather than that of Docker Desktop for Mac
#
eval $(minikube docker-env)

#
# Deploy our SSL wildcard certificate
#
kubectl delete secret mycompany-com-tls     2>/dev/null
kubectl create secret tls mycompany-com-tls --cert=../certs/mycompany.ssl.pem --key=../certs/mycompany.ssl.key

#
# Enable load balancing
#
minikube addons enable ingress
echo "Cluster configuration completed successfully"
