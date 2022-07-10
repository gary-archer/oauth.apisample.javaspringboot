FROM azul/zulu-openjdk-alpine:17.0.2-jre

# Run a script from a runtime argument to do initialization of trusted certificates when required
ARG TRUSTED_CA_CERTS
COPY $TRUSTED_CA_CERTS /usr/local/share/certificates/trusted.ca.crt
COPY deployment/docker/docker-init.sh /tmp/
RUN chmod +x /tmp/docker-init.sh && /tmp/docker-init.sh

# Copy libraries and other files into our docker image
WORKDIR /usr/api
COPY build/libs/sampleapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY data/*                              /usr/api/data/

# Run the app as a low privilege user
RUN addgroup -g 1001 apigroup
RUN adduser -u 1001 -G apigroup -h /home/apiuser -D apiuser
USER apiuser
CMD ["java", "-jar", "/usr/api/sampleapi-0.0.1-SNAPSHOT.jar"]
