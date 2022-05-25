#!/bin/bash

##############################################################
# A script to test Docker deployment on a development computer
##############################################################

cd "$(dirname "${BASH_SOURCE[0]}")"
cd ..

#
# Build the code
#
./gradlew bootJar
if [ $? -ne 0 ]; then
  echo '*** Java API build problem encountered'
  exit 1
fi

#
# Prepare root CA certificates that the Docker container will trust
#
cp ./certs/authsamples-dev.ca.pem docker/trusted.ca.pem

#
# Build the docker image
#
docker build -f docker/Dockerfile --build-arg TRUSTED_CA_CERTS='docker/trusted.ca.pem' -t finalapi:v1 .
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the OAuth Agent docker image'
  exit
fi

#
# Run the docker deployment
#
docker compose --file docker/docker-compose.yml --project-name finalapi up --force-recreate --detach
if [ $? -ne 0 ]; then
  echo "Problem encountered running Docker image"
  exit 1
fi

#
# Wait for it to become available
#
echo 'Waiting for API to become available ...'
BASE_URL='https://api.authsamples-dev.com:445'
while [ "$(curl -k -s -o /dev/null -w ''%{http_code}'' "$BASE_URL/api/companies")" != '401' ]; do
  sleep 2
done
