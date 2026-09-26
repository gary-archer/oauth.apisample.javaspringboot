#!/bin/bash

#####################################
# Use gradle to run integration tests
#####################################

cd "$(dirname "${BASH_SOURCE[0]}")"
./gradlew test
