#!/bin/bash

########################################################
# A script to run Wiremock in Docker in a child terminal
########################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Use Docker compose where volumes are easier on Windows
#
docker compose --project-name wiremock up --force-recreate
if [ $? -ne 0 ]; then
  echo 'Problem encountered deploying Wiremock'
  read -n 1
  exit 1
fi

#
# Prevent automatic terminal closure
#
read -n 1
