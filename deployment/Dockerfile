#
# Build the docker image when invoked from the deployment script
#

# Use the Open JDK 13 image
FROM adoptopenjdk/openjdk13:jdk-13.0.2_8
 
# Set the API folder
WORKDIR /usr/sampleapi

# Copy the packaged jar and other files into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/sampleapi/
COPY deployment/api.config.json          /usr/sampleapi/
COPY certs/mycompany.ssl.pfx             /usr/sampleapi/certs/

# This will execute the Java program in the container
CMD ["java", "-jar", "/usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar"]
