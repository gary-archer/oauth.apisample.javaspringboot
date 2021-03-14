#
# Build the docker image when invoked from the deployment script
# After building, files in the image can be viewed via the below commands
# - eval $(minikube docker-env --profile api)
# - docker run -it demoapi:v1 sh
#

# Use the Open JDK 13 runtime image
FROM azul/zulu-openjdk-alpine:13.0.3-jre

# Install tools for troubleshooting purposes
RUN apk --no-cache add curl
RUN apk --no-cache add openssl

# Set the API folder
WORKDIR /usr/api

# Copy the packaged jar and other files into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY kubernetes/api.config.json          /usr/api/
COPY data/*                              /usr/api/data/

# Create a low privilege user to run the API
RUN addgroup -g 1001 apigroup
RUN adduser -u 1001 -G apigroup -h /home/apiuser -D apiuser

# Configure the Linux OS to trust the Cluster root certificate, to enable HTTPS calls inside the cluster
# An example is calling a Kubernetes deployed Authorization Server at https://login.mycompany.com
COPY certs/kubernetes/internal/svc.default.cluster.local.ca.pem /usr/local/share/ca-certificates
RUN update-ca-certificates
RUN keytool -keystore /usr/lib/jvm/zulu13-ca/lib/security/cacerts -storepass changeit -importcert -alias clusterroot -file /usr/local/share/ca-certificates/svc.default.cluster.local.ca.pem -noprompt

# When a container is run with this image, run the API as the above user
USER apiuser
CMD ["java", "-jar", "/usr/api/sampleapi-0.0.1-SNAPSHOT.jar"]

