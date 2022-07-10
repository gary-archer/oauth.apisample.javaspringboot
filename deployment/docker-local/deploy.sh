#!/bin/bash

##############################################################
# A script to test Docker deployment on a development computer
##############################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Run the docker deployment
#
docker compose --project-name finalapi up --force-recreate --detach
if [ $? -ne 0 ]; then
  echo "Problem encountered running Docker image"
  exit 1
fi

#
# Wait for it to become available
#
echo 'Waiting for API to become available ...'
BASE_URL='https://api.authsamples-dev.com:446'
while [ "$(curl -k -s -o /dev/null -w ''%{http_code}'' "$BASE_URL/api/companies")" != '401' ]; do
  sleep 2
done
