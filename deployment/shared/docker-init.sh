#!/bin/sh

############################################################
# A script to initialize infrastructure for the Docker image
############################################################

#
# Point to the trusted certificate bundle injected into the container
#
TRUSTED_CA_CERTS='/usr/local/share/certificates/trusted.ca.crt'


#
# Add the certificates tool
#
apk --no-cache add ca-certificates
if [ $? -ne 0 ]; then
  echo 'Problem encountered adding the ca-certificates package'
  exit 1
fi
  
#
# Configure operating system trust
#
update-ca-certificates
if [ $? -ne 0 ]; then
  echo 'Problem encountered updating root certificates'
  exit 1
fi

#
# Configure Java trust
#
/usr/lib/jvm/zulu17-ca/bin/keytool -import -alias internalroot.ca -cacerts -file /usr/local/share/certificates/trusted.ca.crt -storepass changeit -noprompt
if [ $? -ne 0 ]; then
  echo 'Problem encountered updating Java trust store'
  exit 1
fi
