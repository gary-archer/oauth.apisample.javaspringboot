FROM azul/zulu-openjdk-debian:25-jre-latest

WORKDIR /usr/api
COPY --chown=10001:10000 build/libs/finalapi-0.0.1-SNAPSHOT.jar /usr/api/
COPY --chown=10001:10000 data/*                                 /usr/api/data/

RUN groupadd --gid 10000 apiuser \
  && useradd --uid 10001 --gid apiuser --shell /bin/bash --create-home apiuser
USER 10001

CMD ["java", "-jar", "/usr/api/finalapi-0.0.1-SNAPSHOT.jar"]
