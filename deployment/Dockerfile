#
# Build the docker image when invoked from the deployment script
#

# Use the Open JDK 13 runtime image
FROM azul/zulu-openjdk-alpine:13.0.3-jre

# Install curl for troubleshooting purposes
#RUN apk --no-cache add curl

# Set the API folder
WORKDIR /usr/sampleapi

# Copy the packaged jar and other files into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/sampleapi/
COPY deployment/api.config.json          /usr/sampleapi/
COPY data/*                              /usr/sampleapi/data/

# Create a low privilege user to run the API
RUN addgroup -g 1001 samplegroup
RUN adduser -u 1001 -G samplegroup -h /home/sampleuser -D sampleuser

# Configure the Linux OS to trust the Charles Proxy Root Certificate from the Developer PC
#COPY deployment/charlesroot.pem /usr/local/share/ca-certificates
#RUN update-ca-certificates
#RUN keytool -keystore /usr/lib/jvm/zulu13-ca/lib/security/cacerts -storepass changeit -importcert -alias charlesroot -file /usr/local/share/ca-certificates/charlesroot.pem -noprompt

# When a container is run with this image, run the API as the above user
USER sampleuser
CMD ["java", "-jar", "/usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar"]

