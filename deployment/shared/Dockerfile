FROM azul/zulu-openjdk-debian:17.0.4-jre

# Configure trusted root certificate authorities
ARG TRUSTED_CA_CERTS
COPY $TRUSTED_CA_CERTS /usr/local/share/certificates/trusted.ca.crt
RUN update-ca-certificates
RUN /usr/lib/jvm/zulu17/bin/keytool -import -alias internalroot.ca -cacerts -file /usr/local/share/certificates/trusted.ca.crt -storepass changeit -noprompt

# Copy libraries and other files into our docker image
WORKDIR /usr/api
COPY build/libs/sampleapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY data/*                              /usr/api/data/

# Run the app as a low privilege user
RUN adduser --disabled-password --home /home/apiuser --gecos '' apiuser
USER apiuser
CMD ["java", "-jar", "/usr/api/sampleapi-0.0.1-SNAPSHOT.jar"]
