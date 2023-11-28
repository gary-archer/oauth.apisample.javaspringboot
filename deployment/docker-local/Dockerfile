FROM azul/zulu-openjdk-debian:21-jre-latest

# Configure trusted root certificate authorities
COPY certs/authsamples-dev.ca.pem /usr/local/share/certificates/trusted.ca.crt
RUN update-ca-certificates
RUN /usr/lib/jvm/zulu21/bin/keytool -import -alias internalroot.ca -cacerts -file /usr/local/share/certificates/trusted.ca.crt -storepass changeit -noprompt

# Copy libraries and other files into our docker image
WORKDIR /usr/api
COPY build/libs/sampleapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY data/*                              /usr/api/data/

# Run the app as a low privilege user
RUN groupadd --gid 10000 apiuser \
  && useradd --uid 10001 --gid apiuser --shell /bin/bash --create-home apiuser
USER 10001
CMD ["java", "-jar", "/usr/api/sampleapi-0.0.1-SNAPSHOT.jar"]
