#
# The docker image for the OAuth secured sample API
# After building, files in the image can be viewed via the below command:
# - docker run -it finalapi:v1 sh
#

# Use the Open JDK 17 runtime image
FROM azul/zulu-openjdk-alpine:17.0.2-jre

# Set the API folder
WORKDIR /usr/api

# Copy the packaged jar and other files into our docker image
COPY build/libs/sampleapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY data/*                              /usr/api/data/

# Create a low privilege user to run the API
RUN addgroup -g 1001 apigroup
RUN adduser -u 1001 -G apigroup -h /home/apiuser -D apiuser

# Configure the Java runtime and Linux OS to trust the root certificate, to enable HTTPS calls inside the cluster
COPY trusted.ca.pem /usr/local/share/ca-certificates/trusted.ca.crt
RUN apk --no-cache add ca-certificates
RUN update-ca-certificates
RUN /usr/lib/jvm/zulu17-ca/bin/keytool -import -alias internalroot.ca -cacerts -file /usr/local/share/ca-certificates/trusted.ca.crt -storepass changeit -noprompt

# When a container is run with this image, run the API as the above user
USER apiuser
CMD ["java", "-jar", "/usr/api/sampleapi-0.0.1-SNAPSHOT.jar"]
