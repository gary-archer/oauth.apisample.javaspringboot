#!/bin/bash

###########################################################################
# A script to download SSL certificates, then build and run the API locally
###########################################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Restore the development configuration once the API is loaded
#
cp deployment/environments/dev/api.config.json ./api.config.json

#
# Run the previously built API
#
./run_api.sh
