#
# Build the docker image from a JAR file with this command:
# docker build --build-arg jar_file=target/sampleapi-0.0.1-SNAPSHOT.jar -t javaapi .
#

# Depend on JDK 13
FROM adoptopenjdk/openjdk13:jdk-13.0.2_8
 
# Set the API folder
WORKDIR /usr/sampleapi

# Copy the packaged jar and other files into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/sampleapi/
COPY api.config.json                     /usr/sampleapi/
COPY certs/mycompany.ssl.pfx             /usr/sampleapi/certs/

# Execute the Java program
CMD ["java", "-jar", "/usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar"]
