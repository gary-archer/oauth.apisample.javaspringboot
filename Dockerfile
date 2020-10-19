#
# Build Java code to a release library with this command:
# mvn clean install
#
# Then build the docker image with this command:
# docker build --build-arg jar_file=target/sampleapi-0.0.1-SNAPSHOT.jar -t sampleapi .
#

# We will use openjdk 13 with alpine as it is a very small linux distro
FROM adoptopenjdk/openjdk13:jdk-13.0.2_8
 
# Set the API folder
WORKDIR /usr/sampleapi

# copy the packaged jar file into our docker image
COPY target/sampleapi-0.0.1-SNAPSHOT.jar /usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar
 
# Execute the Java program
CMD ["java", "-jar", "/usr/sampleapi/sampleapi-0.0.1-SNAPSHOT.jar"]
