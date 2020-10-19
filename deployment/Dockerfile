#
# Build the docker image when invoked from the deployment script
#

# Use the Open JDK 13 image
# FROM adoptopenjdk/openjdk13:jdk-13.0.2_8
FROM adoptopenjdk/openjdk13:jre-13.0.2_8-alpine

# Set the API folder
WORKDIR /usr/sampleapi

# Copy the packaged jar and other files into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/sampleapi/
COPY deployment/api.config.json          /usr/sampleapi/
COPY certs/mycompany.ssl.pfx             /usr/sampleapi/certs/
COPY data/*                              /usr/sampleapi/data/

# When the container is run, this will execute the Java program
CMD ["java", "-jar", "/usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar"]

# This can be used to view the image's file structure before deploying it
# docker run -it javaapi sh
