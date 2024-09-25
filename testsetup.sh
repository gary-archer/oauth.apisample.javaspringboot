#!/bin/bash

#############################################################
# A script to build and run the API with a test configuration
#############################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Copy down the test configuration, to point the API to Wiremock rather than AWS Cognito
#
cp deployment/environments/test/api.config.json ./api.config.json

#
# Download development SSL certificates and configure Java to trust the root CA:
# - sudo $JAVA_HOME/bin/keytool -import -alias authsamples-dev -cacerts -file ./certs/authsamples-dev.ca.crt -storepass changeit -noprompt
#
./downloadcerts.sh
if [ $? -ne 0 ]; then
  read -n 1
  exit 1
fi

#
# Get the platform
#
case "$(uname -s)" in

  Darwin)
    PLATFORM='MACOS'
 	;;

  MINGW64*)
    PLATFORM='WINDOWS'
	;;

  Linux)
    PLATFORM="LINUX"
	;;
esac

#
# Then run the API and Wiremock in a child window
#
echo 'Running API ...'
if [ "$PLATFORM" == 'MACOS' ]; then

  open -a Terminal ./src/test/scripts/run_wiremock.sh
  open -a Terminal ./run_api.sh

elif [ "$PLATFORM" == 'WINDOWS' ]; then

  GIT_BASH='C:\Program Files\Git\git-bash.exe'
  "$GIT_BASH" -c ./src/test/scripts/run_wiremock.sh &
  "$GIT_BASH" -c ./run_api.sh &

elif [ "$PLATFORM" == 'LINUX' ]; then

  gnome-terminal -- ./src/test/scripts/run_wiremock.sh
  gnome-terminal -- ./run_api.sh
fi

#
# Wait for endpoints to become available
#
echo 'Waiting for Wiremock endpoints to come up ...'
WIREMOCK_URL='https://login.authsamples-dev.com:447/__admin/mappings'
while [ "$(curl -k -s -X GET -o /dev/null -w '%{http_code}' "$WIREMOCK_URL")" != '200' ]; do
  sleep 2
done

echo 'Waiting for API endpoints to come up ...'
API_URL='https://api.authsamples-dev.com:446/investments/companies'
while [ "$(curl -k -s -X GET -o /dev/null -w '%{http_code}' "$API_URL")" != '401' ]; do
  sleep 2
done

#
# Restore the development configuration once the API is loaded
#
cp deployment/environments/dev/api.config.json ./api.config.json

#
# Indicate success
#
echo "Start tests via './gradlew test' or './gradlew loadtest' ..."
