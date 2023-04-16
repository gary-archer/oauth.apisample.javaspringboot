
#!/bin/bash

################
# Deploy the API
################

#
# Ensure that we are in the folder containing this script
#
cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Manage environment specific differences and set up environment variables used by envsubst
#
if [ "$CLUSTER_TYPE" == 'local' ]; then
  
  ENVIRONMENT_FOLDER='kubernetes-local';
  export API_DOMAIN_NAME='api.mycluster.com'
  export API_DOCKER_IMAGE='finaljavaapi:1.0'

else

  if [ "$DOCKERHUB_ACCOUNT" == '' ]; then
    echo '*** The DOCKERHUB_ACCOUNT environment variable has not been configured'
    exit 1
  fi

  ENVIRONMENT_FOLDER='kubernetes-aws';
  export API_DOMAIN_NAME='api.authsamples-k8s.com'
  export API_DOCKER_IMAGE="$DOCKERHUB_ACCOUNT/finaljavaapi:1.0"
fi

#
# Create a configmap for the API's JSON configuration file
#
kubectl -n applications delete configmap api-config 2>/dev/null
kubectl -n applications create configmap api-config --from-file="../environments/$ENVIRONMENT_FOLDER/api.config.json"
if [ $? -ne 0 ]; then
  echo '*** Problem encountered creating the API configmap'
  exit 1
fi

#
# Produce the final YAML using the envsubst tool
#
envsubst < ./api-template.yaml > ./api.yaml
if [ $? -ne 0 ]; then
  echo '*** Problem encountered running envsubst to produce the final Kubernetes api.yaml file'
  exit 1
fi

#
# Trigger deployment of the API to the Kubernetes cluster
#
kubectl -n applications delete -f ./api.yaml 2>/dev/null
kubectl -n applications apply  -f ./api.yaml
if [ $? -ne 0 ]; then
  echo '*** API Kubernetes deployment problem encountered'
  exit 1
fi
