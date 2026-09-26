#!/bin/bash

##############################
# Use gradle to run load tests
##############################

cd "$(dirname "${BASH_SOURCE[0]}")"
./gradlew loadtest
